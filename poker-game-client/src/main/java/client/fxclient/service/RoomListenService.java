package client.fxclient.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import client.fxclient.service.listener.RoomInitListener;
import game.Game;
import game.GameHolder;
import game.player.Player;
import game.player.PlayerHolder;
import game.poker.Poker;
import game.room.Room;
import game.room.RoomHolder;
import game.utils.lock.GameReadyLock;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import util.SuitStream;

public class RoomListenService extends Service<Void> {

    private SuitStream stream;

    public RoomListenService(SuitStream stream,Label roomIDLabel,Label waitTip,Label pLeftNameLabel,Label pRightNameLabel,Pane pokerPane,Label pLeftTurnLabel,Label pRightTurnLabel){
        this.stream=stream;
        messageProperty().addListener(new RoomInitListener(roomIDLabel, waitTip, pLeftNameLabel, pRightNameLabel, pokerPane,pLeftTurnLabel,pRightTurnLabel));
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task=new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // 由于Room被根据id重定义了eq和hs。更新room不能用room对象来判断触发钩子方法，用updateMessage辅助实现
                String updateFlag="";
                // 这是新房间
                if(RoomHolder.room==null){
                    // 利用短路机制
                    while (RoomHolder.room==null || RoomHolder.room.getPlayers().size()<3) {
                        try {
                            String roomJsonStr=stream.readStr();
                            RoomHolder.room=resolveRoom(roomJsonStr);
                            updateFlag=updateFlag+" ";
                            this.updateMessage(updateFlag);
                        } catch (Exception e) {
                            // 房间更新异常
                            e.printStackTrace();
                        }
                    }    
                }else if(!RoomHolder.room.getStatus().equals("restart")){
                    for(int i=0;i<RoomHolder.room.getPlayers().size();i++){
                        String xxReady=stream.readStr();
                        this.updateMessage("ready:"+xxReady);
                        // 给UI刷新缓冲时间
                        Thread.sleep(500);
                    }
                }
                RoomHolder.room.setStatus("start");
                this.updateMessage("clearReady");
                // 房间初始化完成，获取手牌
                Object[] pokerArr=(Object[])stream.readObject();
                List<Poker> listPoker=Arrays.asList(pokerArr).stream().map((t)->{return (Poker)t;}).collect(Collectors.toList());
                PlayerHolder.player.addAllPoker(listPoker);
                updateFlag=updateFlag+" ";
                this.updateMessage(updateFlag);
                // 通知gameListen开始运行
                synchronized(GameReadyLock.lock){
                    GameReadyLock.lock.notify();
                }
                return null;
            }
        };
        return task;
    }

    /**
     * 由于players是CopyOnWriteArrayList，json数据是这样的$.ref:xxx<p>
     * 因此部分需要<b>手动解析</b>。
     * @param jsonString
     * @return
     */
    private Room resolveRoom(String jsonString){
        Room room=JSON.parseObject(jsonString, Room.class);
        JSONObject jobj=JSON.parseObject(jsonString);
        room.setPlayers(jobj.getJSONArray("players").toJavaList(Player.class));
        Game game=GameHolder.game;
        game.setOver(jobj.getJSONObject("game").getBoolean("over"));
        game.setHandOut(jobj.getJSONObject("game").getBoolean("handOut"));
        game.setPlayers(jobj.getJSONObject("game").getJSONArray("players").toJavaList(Player.class));
        room.setGame(game);
        return room;
    }
    
}
