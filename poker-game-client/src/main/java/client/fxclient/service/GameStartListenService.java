package client.fxclient.service;

import java.util.List;

import com.alibaba.fastjson.JSON;

import client.fxclient.service.listener.GameStartListener;
import game.GameHolder;
import game.poker.Poker;
import game.utils.lock.GameStartLock;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import util.SuitStream;

public class GameStartListenService extends Service<Void>{

    private SuitStream stream;
    
    public GameStartListenService(SuitStream stream,Button callPass,Button callPut,Pane rightPutPane,Pane leftPutPane,Pane putPane,Label pLeftTurnLabel,Label pRightTurnLabel,Label pLeftNameLabel,Label pRightNameLabel,Text overText,Button readyBtn){
        this.stream=stream;
        messageProperty().addListener(new GameStartListener(callPass, callPut, rightPutPane, leftPutPane, putPane, pLeftTurnLabel, pRightTurnLabel, pLeftNameLabel, pRightNameLabel,overText,readyBtn));
    }

    @Override
    protected Task<Void> createTask() {
        Task<Void> task=new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                synchronized(GameStartLock.lock){
                    GameStartLock.lock.wait();
                }
                String msg="";
                String winner="";
                while (!GameHolder.game.isOver()) {
                    String turn=stream.readStr();
                    msg=turn;
                    this.updateMessage(msg);
                    String whoPut=stream.readStr();
                    
                    /**
                     * 这里开始注意，不建议使用ObjectInputStream了。
                     * 因为SuitStream平行模型的readObject()无法让server、client、ui三个程序很好的交互。
                     * 会造成数据不匹配，从而造成冲突的异常。所以改用string的形式，即JSON/XML等。
                     */
                    String putPokerListJson=stream.readStr();
                    if(putPokerListJson.contains("(over)/")) {
                        int splitIndex=putPokerListJson.indexOf("(over)/");
                        GameHolder.game.setOver(true);
                        winner=putPokerListJson.substring(splitIndex+7);
                        putPokerListJson=putPokerListJson.substring(0, splitIndex);
                    }
                    List<Poker> putPokerList=JSON.parseArray(putPokerListJson, Poker.class);
                    
                    // 以message形式给ui线程，拼接结果：put:player/pokers
                    StringBuilder builder=new StringBuilder();
                    builder.append(whoPut);// 添加putFlag标识
                    builder.append("/");
                    for(Poker p:putPokerList){
                        builder.append(p.toString()+" ");
                    }
                    msg=builder.toString().stripTrailing();
                    this.updateMessage("put:"+msg);
                    // fx-ui bug,msg无法触发onchanged，加了这个就给fx时间刷新ui调用onchanged了
                    Thread.sleep(1000);
                }
                // 显示赢家，重置资源
                this.updateMessage("winner:"+winner);
                return null;
            }
        };
        return task;
    }
    
}
