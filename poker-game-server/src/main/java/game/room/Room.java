package game.room;

import game.Game;
import game.player.Player;
import game.poker.Poker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import util.SuitStream;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import server.ServerHolder;

/**
 * 观察者模式中的Subject（被观察类，变化类）
 * 要特别注意该类中的线程安全性问题。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room implements Serializable {

    // 若需求不变且不改动类,class文件不变则，序列化id不需要自己设置。

    /**
     * 客户端分离后的改动
     */
    private static final long serialVersionUID = 1L;

    private int id; // 房间id 事实不变对象。
    private String status = "ready";
    private List<Player> players = new CopyOnWriteArrayList<>(); // 安全对象且封闭Player。
    private Game game; // 注意该对象的安全性。（细粒度）
    private final transient AtomicInteger turnCallIndex = new AtomicInteger(0); // 地主请求的序号.

    // 闭锁 用于玩家房间内的准备开始 (transient阻止该字段序列化)
    private transient CountDownLatch latch = new CountDownLatch(3);
    // 用于辅助 叫地主过程的一轮下来的 最后一步
    private transient CountDownLatch latch2 = new CountDownLatch(3);
    // 其他线程都在等 第一个玩家 “确定抢不抢” 的信号量
    private final transient Semaphore finishLastCall = new Semaphore(0);

    // 栅栏控制线程统一到达，作用广泛，此处用于防止while损耗
    private final transient CyclicBarrier barrier = new CyclicBarrier(3);
    // 辅助引用，用于线程通信
    private Player boss;

    // 房间的锁：替代sync简单示例
    // private final transient Lock lock=new ReentrantLock();

    /**
     * 多个玩家的 连接线程，同时进入该方法
     */
    public void serverForRoom(final Player player, final SuitStream stream)
            throws Exception {
        latch.await();
        System.out.println("room[" + id + "]:user[" + player.getName() + "] is playing game...");
        // 下发玩家各自的手牌
        game.init(); // 只用一个线程执行该方法即可
        // 序列化操作，将List转为Array不会丢失数据。而内置List会自动处理。
        stream.writeObject(player.getPokers().toArray());
        // 等待各个writeObj完成后统一进入(不允许某个线程抢先完成）。
        // 栅栏或闭锁都可以，为了增加熟悉度，采用(Cyclic)Barrier
        barrier.await();
        askForBoss(stream, player);
    }

    /**
     * 当房间玩家数量发生变化时会被调用，通知客户端视图更新
     * 本应该是服务器持有控制客户端页面变化的对象，这样就省去了传输到客户端让客户端自己更新的麻烦。
     * 但是，由于客户端与服务端保持连接通信是靠Socket的网络传输流（介于应用层与传输层之间的抽象接口），client,server是两个不同的程序，内存数据也不同。
     * 它们之间的数据通过传输后，效果类似复制，因此服务器只持有Socket对象，无法像单机那样的便捷，如直接获取界面对象并更新（省去了传输的麻烦）。
     * 
     * @throws Exception
     */
    public void notifyPlayer() throws Exception {
        for (Player p : players) {
            // Room r = (Room) this.deepClone();
            // ObjectOutstream传输序列化对象，而传的对象为this会丢失数据，因此此处深克隆。
            // if (r == null)
                // throw new Exception("房间克隆失败");
            // room包含Game(热更新对象)，适合传JSON字符串（有"结构"和值）
            // 若通过oout写对象，readObj使用的类加载器默认AppClassLoader无法识别Game造成not found
            // 用写序列化对象包含define和value,受类加载器限制，不能整体传输，用json传值即可，客户端加载类再赋值。
            ServerHolder.playerStreamManager.get(p).writeStr(JSON.toJSONString(this));
            // Deprecated Work
            // ServerHolder.playerStreamManager.get(p).writeObject(r);
        }
    }

    public Object deepClone() {
        // 字节数组 输出流不需要构造参数，可用于深克隆，其他可能需要文件作为媒介
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
                out.writeObject(this);
            }
            try (InputStream bin = new ByteArrayInputStream(bout.toByteArray())) {
                ObjectInputStream in = new ObjectInputStream(bin);
                return in.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求当地主的方法
     * 随机的、最先进方法的线程将优先获得叫地主机会
     * 后续可以控制线程(玩家) 顺序
     */
    private void askForBoss(SuitStream stream, Player player) throws Exception {
        /**
         * 此处可用Lock接口实现代替，相比sync，它可以显式、跨代码块acquire和realease锁。
         * 于此同时用Lock生成的Condition接口实例，它提供await和signal用于增强wait和notify。
         * 因此，更加灵活。但需要手动获取锁和释放锁相较于sync隐式的方式来说，繁琐一些，且多一个对象。
         */
        // lock.lock(); // 若没得到锁想分支做其他事情则Lock.tryLock() { } else { }
        synchronized (this) {
            player.setReqIndex(turnCallIndex.get());
            // 通知监听者(Player)
            for (int i = 0; i < players.size(); i++) {
                ServerHolder.playerStreamManager.get(players.get(i)).writeStr("turn:" + player.getName());
            }
            switch (turnCallIndex.get()) {
                case 0 -> order(stream, player, true);
                default -> {
                    boolean noFirstCall = true;
                    for (int i = 0; i < players.size(); i++) {
                        Player p = players.get(i);
                        if (!p.getName().equals(player.getName()) && p.isFirstCall()) {
                            noFirstCall = false;
                        }
                    }
                    if (noFirstCall) {
                        order(stream, player, true);
                    } else {
                        order(stream, player, false);
                    }
                }
            }
            turnCallIndex.incrementAndGet();
            latch2.countDown();
        }
        // lock.unlock();
        // 完成最后的处理
        latch2.await();
        // 当最多call次数的玩家只有一名时，那么他就是地主。
        boss = game.getBossInstantly();
        if (boss == null) {
            // 3个refuse则重新发牌... return
            // 栈内不共享，因此无线程安全问题
            int count=0;
            for(Player p:players){
                if(p.isRefuseBoss()) count++;
            }
            if(count==3){
                System.out.println("room["+id+"]"+" is rehandout pokers...");
                stream.writeStr("rehandOut");
                resetServer();
                /**
                 * 重置后，需利用栅栏统一从这里开始穿过闭锁，否则
                 * 后面的线程调用resetServer()会重置latch，从而失效。
                 */
                barrier.await();
                latch.countDown();
                serverForRoom(player, stream);
                return;
            }
            // 说明有两名不相上下的玩家，需要给第一个叫地主的多一次机会抢。
            if (player.isFirstCall()) {
                // 刷新监听者(Player)，轮到player操作
                notifyTurnPlayer("turn:", player);
                stream.writeStr("callStill:");
                String op = stream.readStr();
                if ("1".equals(op)) {
                    // notify
                    player.reqBoss();
                    notifyTurnPlayer("callStillY:", player);
                    boss = player;
                } else {
                    // notify
                    player.refuseBoss();
                    notifyTurnPlayer("callStillN:", player);
                    // 上一个人要的人当boss
                    end:while (turnCallIndex.decrementAndGet()>=0) {
                        for(int i=0;i<players.size();i++){
                            Player p=players.get(i);
                            if(p.getReqIndex()==turnCallIndex.get() && !p.isRefuseBoss()){
                                boss=p;
                                break end;
                            }
                        }
                    }
                }
                // 释放（两个）信号量，让其他玩家结束阻塞。
                finishLastCall.release(players.size() - 1);
            } else {
                finishLastCall.acquire();
            }
        }
        stream.writeStr("over");
        boss.setBoss(true);
        stream.writeStr(boss.getName());
        Object[] pokerBoss = game.getPokerBossCollector().toArray();
        stream.writeObject(pokerBoss);
        if (player == boss) {
            // 添加地主牌
            for (Object p : pokerBoss) {
                boss.addPoker((Poker) p);
            }
        }
        // 游戏逻辑控制
        turnCallIndex.set(boss.getReqIndex());
        while (!game.isOver()) {
            // 栅栏可复用，等待线程全部到来，避免循环产生消耗
            barrier.await();
            control(stream, player);
            barrier.await();
        }
        System.out.println("room["+id+"]:game is over");
        // 重置服务
        resetServer();
        // 是否重开(继续)
        // 准备按钮发来的玩家id，意味重新开始按钮
        String name=stream.readStr();
        latch.countDown();
        // 通知准备
        for(Player p:players){
            ServerHolder.playerStreamManager.get(p).writeStr(name);
        }
        serverForRoom(player, stream);
        return;
    }

    private void resetServer(){
        game.getPokerCollector().clear();
        game.getPokerBossCollector().clear();
        game.setHandOut(false);
        game.setOver(false);
        for(Player p:players){
            p.getPokers().clear();
            p.setBoss(false);
            p.setFirstCall(false);
            p.setRefuseBoss(false);
            p.setReqTimes(0);
        }
        latch=new CountDownLatch(3);
        latch2=new CountDownLatch(3);
        turnCallIndex.set(0);
    }
    
    /**
     * 游戏正常进行的时候是一个重复的过程控制（仅细微差别，如：order）。
     * @param order 第一次为地主的顺序为基准，后面为player的reqIndex顺序（保留了叫地主过程的顺序）
     */
    public synchronized void control(SuitStream stream, Player player)
            throws Exception {
        if (turnCallIndex.get() % 3 == player.getReqIndex()) {
            notifyTurnPlayer("turn:", player);
            String putPokerListJson=stream.readStr();
            if(putPokerListJson.contains("(over)/")) game.setOver(true);
            notifyPutPokers(player, putPokerListJson);
            turnCallIndex.incrementAndGet();
        }
        return;
    }

    /**
     * 更新player的出牌
     * @param player
     * @param list
     */
    private void notifyPutPokers(Player player,String putPokerListJson){
        for (int i = 0; i < players.size(); i++) {
            try {
                ServerHolder.playerStreamManager.get(players.get(i)).writeStr(player.getName());    
                ServerHolder.playerStreamManager.get(players.get(i)).writeStr(putPokerListJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
        game.addPlayer(player);
        // room里的playerList是线程安全的
        if (getPlayers().size() > 2) {
            setStatus("start");
        }
    }

    /**
     * 通知该轮到 <code>player</code> 操作了
     * 
     * @param player
     */
    private void notifyTurnPlayer(String type, Player player) {
        for (int i = 0; i < players.size(); i++) {
            try {
                ServerHolder.playerStreamManager.get(players.get(i)).writeStr(type + player.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 请求叫地主/抢地主
     * 
     * @param call 叫/抢
     */
    private void order(SuitStream stream, Player player, boolean call) throws Exception {
        if (call)
            stream.writeStr("callBoss");
        else
            stream.writeStr("askBoss");
        String op = stream.readStr();
        if ("1".equals(op)) {
            // notify
            for (int i = 0; i < players.size(); i++) {
                String type = call ? "callY:" : "askY:";
                ServerHolder.playerStreamManager.get(players.get(i)).writeStr(type + player.getName());
            }
            player.reqBoss();
            if (call)
                player.setFirstCall(true);
        } else {
            // notify
            for (int i = 0; i < players.size(); i++) {
                String type = call ? "callN:" : "askN:";
                ServerHolder.playerStreamManager.get(players.get(i)).writeStr(type + player.getName());
            }
            player.refuseBoss();
        }
    }

    @Override
    public String toString() {
        return "id:" + id + " " + players.toString();
    }
}
