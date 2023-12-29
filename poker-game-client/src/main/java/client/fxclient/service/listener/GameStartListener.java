package client.fxclient.service.listener;

import java.util.ArrayList;
import java.util.List;

import client.fxclient.service.common.util.CommonUtils;
import game.player.Player;
import game.player.PlayerHolder;
import game.poker.Poker;
import game.poker.common.PokerColorEnum;
import game.poker.common.PokerValueEnum;
import game.room.RoomHolder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class GameStartListener implements ChangeListener<String> {

    private Button callPass;
    private Button callPut;
    private Pane rightPutPane;
    private Pane leftPutPane;
    private Pane putPane;
    private Label pLeftTurnLabel;
    private Label pRightTurnLabel;
    private Label pRightNameLabel;
    private Label pLeftNameLabel;
    private Text overText;
    private Button readyBtn;

    public GameStartListener(Button callPass, Button callPut, Pane rightPutPane, Pane leftPutPane, Pane putPane,
            Label pLeftTurnLabel, Label pRightTurnLabel, Label pLeftNameLabel, Label pRightNameLabel,Text overText,Button readyBtn) {
        this.callPass = callPass;
        this.callPut = callPut;
        this.rightPutPane = rightPutPane;
        this.leftPutPane = leftPutPane;
        this.putPane = putPane;
        this.pLeftTurnLabel = pLeftTurnLabel;
        this.pRightTurnLabel = pRightTurnLabel;
        this.pLeftNameLabel = pLeftNameLabel;
        this.pRightNameLabel = pRightNameLabel;
        this.overText=overText;
        this.readyBtn=readyBtn;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (newValue.startsWith("turn:")) {
            String playerName = newValue.substring(5);
            CommonUtils.showOpTip(playerName, "正在出牌...", pLeftNameLabel, pRightNameLabel, pLeftTurnLabel,
                    pRightTurnLabel, true);
            if (PlayerHolder.player.getName().equals(playerName)) {
                callPass.setVisible(true);
                callPut.setVisible(true);
            }
        }
        if (newValue.startsWith("put:")) {
            int splitIndex = newValue.indexOf("/");
            String playerName = newValue.substring(4, splitIndex);
            try {
                String pokerStr=newValue.substring(splitIndex + 1);
                String[] pokerStrArr = pokerStr.split(" ");
                List<Poker> putPokerList = new ArrayList<>();
                if(!pokerStr.equals("")){
                    for (int i = 0; i < pokerStrArr.length; i++) {
                        Poker poker = new Poker(PokerColorEnum.getByColor(pokerStrArr[i].substring(0, 1)),
                                PokerValueEnum.getByValue(pokerStrArr[i].substring(1)));
                        putPokerList.add(poker);
                    }
                }
                displayPutPane(playerName, putPokerList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(newValue.startsWith("winner:")){
            String winner=newValue.substring(7);
            if(!RoomHolder.room.getBoss().getName().equals(winner)){
                // 农民胜利，多名展示
                String other="";
                for(Player p:RoomHolder.room.getPlayers()){
                    if(!p.getName().equals(RoomHolder.room.getBoss().getName())){
                        other=p.getName();
                    }
                }
                winner=winner+" "+other;
            }
            overText.setText("游戏结束，本局赢家："+winner);
            overText.setVisible(true);
            callPut.setVisible(false);
            callPass.setVisible(false);
            readyBtn.setVisible(true);
        }
    }

    /**
     * 显示Player的Poker<p>
     * 内部附带了对RoomHolder.lastPokerList的更新
     * @param playerName
     * @param pokerList
     */
    private void displayPutPane(String playerName, List<Poker> pokerList) {
        // remove Last put pokers
        if (pLeftNameLabel.getText().equals(playerName)) {
            leftPutPane.getChildren().clear();
        } else if (pRightNameLabel.getText().equals(playerName)) {
            rightPutPane.getChildren().clear();
        } else {
            putPane.getChildren().clear();
        }
        if(pokerList.size()!=0){
            // 在知道LastPutPokers之前清除缓存。
            RoomHolder.lastPokerList.clear();
            for (int i = 0; i < pokerList.size(); i++) {
                ImageView view = new ImageView(new Image("img/" + pokerList.get(i).toString() + ".png"));
                // layoutX，Y不设置初始，会显示不出来
                view.setFitWidth(65);
                view.setFitHeight(96);
                view.setLayoutX(14);
                view.setLayoutY(6);
                if (pLeftNameLabel.getText().equals(playerName)) {
                    int size = leftPutPane.getChildren().size();
                    if (size != 0) {
                        view.setLayoutX(leftPutPane.getChildren().get(size - 1).getLayoutX() + 16);
                    }
                    leftPutPane.getChildren().add(view);
                    // 该处(left以及下面的right)记录lastPokerList
                    RoomHolder.lastPokerList.add(pokerList.get(i));
                } else if (pRightNameLabel.getText().equals(playerName)) {
                    int size = rightPutPane.getChildren().size();
                    if (size != 0) {
                        view.setLayoutX(rightPutPane.getChildren().get(size - 1).getLayoutX() + 16);
                    }
                    rightPutPane.getChildren().add(view);
                    //
                    RoomHolder.lastPokerList.add(pokerList.get(i));
                } else {
                    int size = putPane.getChildren().size();
                    if (size != 0) {
                        view.setLayoutX(putPane.getChildren().get(size - 1).getLayoutX() + 16);
                    }
                    putPane.getChildren().add(view);
                }
            }
        }else{
            // 提示pass信息
            Text text=new Text("PASS");
            text.setLayoutX(14);
            text.setLayoutY(6);
            text.setStyle("-fx-font-size: 25;");
            if (pLeftNameLabel.getText().equals(playerName)) {
                leftPutPane.getChildren().add(text);
            } else if (pRightNameLabel.getText().equals(playerName)) {
                rightPutPane.getChildren().add(text);
            } else {
                putPane.getChildren().add(text);
            }
        }
    }

}
