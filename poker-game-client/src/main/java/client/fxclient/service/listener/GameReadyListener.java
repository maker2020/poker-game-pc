package client.fxclient.service.listener;

import java.util.ArrayList;
import java.util.List;

import client.fxclient.service.common.util.CommonUtils;
import game.player.Player;
import game.player.PlayerHolder;
import game.poker.Poker;
import game.poker.common.PokerColorEnum;
import game.poker.common.PokerValueEnum;
import game.poker.util.PokerUtil;
import game.room.RoomHolder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class GameReadyListener implements ChangeListener<String>{
    
    private Label pLeftTurnLabel;
    private Label pRightTurnLabel;
    private Label pLeftNameLabel;
    private Label pRightNameLabel;
    private Button callY;
    private Button callN;
    private Pane bossPokerPane;
    private Pane pokerPane;
    private Button hiddenButton;

    public GameReadyListener(Label pLeftTurnLabel,Label pRightTurnLabel,Label pLeftNameLabel,Label pRightNameLabel,Button callY,Button callN,Pane bossPokerPane,Pane pokerPane,Button hiddenButton){
        this.pLeftTurnLabel=pLeftTurnLabel;
        this.pRightTurnLabel=pRightTurnLabel;
        this.pLeftNameLabel=pLeftNameLabel;
        this.pRightNameLabel=pRightNameLabel;
        this.callY=callY;
        this.callN=callN;
        this.bossPokerPane=bossPokerPane;
        this.pokerPane=pokerPane;
        this.hiddenButton=hiddenButton;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if("rehandOut".equals(newValue)){
            // 触发重新开局按钮
            Event.fireEvent(hiddenButton,new MouseEvent(MouseEvent.MOUSE_CLICKED,
            1,1,1,1, MouseButton.PRIMARY, 1,
            true, true, true, true,
            true, true, true,
            true, true, true, null));
            return;
        }
        if("over".equals(newValue)){
            pLeftTurnLabel.setVisible(false);
            pRightTurnLabel.setVisible(false);
            return;
        }
        if(newValue.startsWith("bossPokers:")){
            displayBossPokers(newValue);
        }
        if(newValue.startsWith("boss:")){
            String boss=newValue.substring(5);
            RoomHolder.room.setBoss(new Player(boss));
            if(pLeftNameLabel.getText().equals(boss)){
                pLeftNameLabel.setStyle("-fx-text-fill: red;-fx-font-size: 30;");
            }else if(pRightNameLabel.getText().equals(boss)){
                pRightNameLabel.setStyle("-fx-text-fill: red;-fx-font-size: 30;");
            }
        }
        if(newValue.startsWith("callY")||newValue.startsWith("callN")){
            String choiceStr=newValue.startsWith("callY")?"叫地主":"不叫";
            String playerName=newValue.substring(6);
            CommonUtils.showOpTip(playerName,choiceStr, pLeftNameLabel, pRightNameLabel, pLeftTurnLabel, pRightTurnLabel,false);
        }else if(newValue.startsWith("askY")||newValue.startsWith("askN")
                ||newValue.startsWith("callStillY")||newValue.startsWith("callStillN")){
            String choiceStr=(newValue.startsWith("askY")||newValue.startsWith("callStillY"))?"抢地主":"不抢";
            String playerName;
            if(newValue.contains("ask")) playerName=newValue.substring(5);
            else playerName=newValue.substring(11);
            CommonUtils.showOpTip(playerName, choiceStr, pLeftNameLabel, pRightNameLabel, pLeftTurnLabel, pRightTurnLabel,false);
        }
        if(newValue.startsWith("turn")){
            String turnWho=newValue.substring(5);
            // 显示的逻辑
            CommonUtils.showOpTip(turnWho,"正在思考...", pLeftNameLabel, pRightNameLabel, pLeftTurnLabel, pRightTurnLabel,false);
        }
        if(newValue.startsWith("callBoss")||newValue.startsWith("askBoss")||newValue.startsWith("callStill:")){
            String opMenu=newValue;
            if("askBoss".equals(opMenu)){
                callY.setText("抢");
                callN.setText("不抢");   
            }else if("callStill:".equals(opMenu)){
                callY.setText("我还抢");
                callN.setText("不抢了");
            }else{
                callY.setText("叫地主");
                callN.setText("不叫");
            }
            // 展示按钮
            callY.setVisible(true);
            callN.setVisible(true);
        }
    }  

    private void displayBossPokers(String listStr){
        listStr=listStr.substring(11);
        String[] names=listStr.split("[\\[, \\]]");
        List<Poker> bossPoker=new ArrayList<>();
        for(int i=0;i<names.length/2;i++){
            String pokerStr=names[i*2+1];
            ImageView view=new ImageView();
            view.setImage(new Image("img/"+pokerStr+".png"));
            view.setFitHeight(108.0);
            view.setFitWidth(70.0);
            view.setLayoutX(14.0+85*i);
            view.setLayoutY(11.0);
            bossPokerPane.getChildren().add(view);
            bossPoker.add(new Poker(PokerColorEnum.getByColor(pokerStr.substring(0,1)), PokerValueEnum.getByValue(pokerStr.substring(1))));
        }
        if(PlayerHolder.player.isBoss()){
            PlayerHolder.player.addAllPoker(bossPoker);
            PokerUtil.sort(PlayerHolder.player.getPokers());
            CommonUtils.displayPoker(PlayerHolder.player.getPokers(),pokerPane);
        }
    }

}
