package client.fxclient.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import client.fxclient.service.listener.GameReadyListener;
import game.player.PlayerHolder;
import game.poker.Poker;
import game.room.RoomHolder;
import game.utils.lock.GameReadyLock;
import game.utils.lock.GameStartLock;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import util.SuitStream;

public class GameReadyListenService extends Service<Void>{

    private SuitStream stream;

    public GameReadyListenService(SuitStream stream,Label pLeftTurnLabel,Label pRightTurnLabel,Label pLeftNameLabel,Label pRightNameLabel,Button callY,Button callN,Pane bossPokerPane,Pane pokerPane,Button hiddenButton){
        this.stream=stream;
        messageProperty().addListener(new GameReadyListener(pLeftTurnLabel, pRightTurnLabel, pLeftNameLabel, pRightNameLabel, callY, callN, bossPokerPane, pokerPane,hiddenButton));
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task=new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String updateFlag="";
                // 避免过度消耗资源造成循环阻塞，从而失效。 sleep或者采用java wait-notify 机制
                synchronized(GameReadyLock.lock){
                    // 最好不要拿可能用的对象来 等待
                    GameReadyLock.lock.wait();
                }
                String turnWhoData=stream.readStr();
                updateFlag=turnWhoData;
                this.updateMessage(updateFlag);
                // 每个玩家4次read(3次turn信息，一次choice)
                // 后新增：每个玩家的choice状态的获取即3次
                for(int i=0;i<6;i++){
                    String unknown=stream.readStr();
                    updateFlag=unknown;
                    this.updateMessage(updateFlag);
                    // weak 1: 调试阻塞下无bug，加sleep等待无bug，可能fx(UI框架)的updateMessage更新数据的机制有关（在合适的情况下更新,粒度没这么细）。
                    Thread.sleep(100);
                }
                // 最后一次特殊read和flush(即firstCall当call时有最后的decision)
                String unknown=stream.readStr();
                // unknown可能是重发牌，可能是over（地主已选出）
                if("rehandOut".equals(unknown)){
                    RoomHolder.room.setStatus("restart");
                    this.updateMessage("rehandOut");
                    return null;
                }
                updateFlag=unknown;
                this.updateMessage(updateFlag);
                if(!"over".equals(unknown)){
                    // turn:xxx
                    if(unknown.substring(5).equals(PlayerHolder.player.getName())){
                        PlayerHolder.player.setFirstCall(true);
                    }
                    // 说明没有结束，还有第一位的最后的decision
                    if(PlayerHolder.player.isFirstCall()){
                        String opMenu=stream.readStr();
                        updateFlag=opMenu;
                        this.updateMessage(updateFlag);
                    }
                    String lastChoiceRes=stream.readStr();
                    updateFlag=lastChoiceRes;
                    this.updateMessage(updateFlag);
                    String over=stream.readStr();
                    updateFlag=over;
                    this.updateMessage(updateFlag);
                }
                // 注意交互（与上述通过read行读和write行写，Server与Client交替交互，特别是连续读str和obj，稍有不慎就有bug）
                // 否则就出现不是一个频道的情况，必须要处理。
                // 解决一些交互方法就是，客户端完成一段逻辑后，向服务端发送完成的标识。（最好不通过sleep碰运气且不严谨的方式）
                
                // 地主 及 地主牌
                String bossPlayer=stream.readStr();
                updateFlag=bossPlayer;
                if(PlayerHolder.player.getName().equals(bossPlayer)){
                    PlayerHolder.player.setBoss(true);
                }
                // weak 1:updateFlag数据没问题，但断点在updateMessage()就有问题，与fx有关
                Thread.sleep(100);
                this.updateMessage("boss:"+updateFlag);
                Object[] bossPokerArr=(Object[])stream.readObject();
                List<Poker> bossPokers=Arrays.asList(bossPokerArr)
                        .stream().map((t)->{return (Poker)t;}).collect(Collectors.toList());
                String bossPokersData=bossPokers.toString();
                updateFlag=bossPokersData;
                this.updateMessage("bossPokers:"+updateFlag);
                synchronized(GameStartLock.lock){
                    GameStartLock.lock.notify();
                }
                return null;
            }
        };
        return task;
    }
    
}
