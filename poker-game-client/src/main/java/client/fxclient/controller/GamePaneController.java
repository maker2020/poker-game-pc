package client.fxclient.controller;

import com.alibaba.fastjson.JSON;

import client.fxclient.service.GameReadyListenService;
import client.fxclient.service.GameStartListenService;
import client.fxclient.service.RoomListenService;
import client.fxclient.service.common.util.CommonUtils;
import game.GameHolder;
import game.player.PlayerHolder;
import game.room.RoomHolder;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import util.SuitStream;

public class GamePaneController {
    
    private SuitStream stream;
    
    @FXML
    private Label waitTip;
    @FXML
    private Button readyBtn;
    @FXML
    private Button callY;
    @FXML
    private Button callN;
    @FXML
    private Pane pokerPane;
    @FXML
    private Pane bossPokerPane;
    @FXML
    private Label roomIDLabel;
	@FXML
    private Label pLeftNameLabel;
    @FXML
    private Label pLeftTurnLabel;
    @FXML
    private Label pRightNameLabel;
    @FXML
    private Label pRightTurnLabel;

    @FXML
    private Button callPass;
    @FXML
    private Button callPut;
    @FXML
    private Pane rightPutPane;
    @FXML
    private Pane leftPutPane;
    @FXML
    private Pane putPane;

    @FXML
    private Button startHidden;

    @FXML
    private Text overText;

    /**
     * 这里顺序要颠倒，因为前者需要提前进入并被锁限制等待被通知唤醒。
     * @throws Exception
     */
    private void init() throws Exception{
        try {
            // 游戏开始：出牌，控制。
            gameStart();
            // 初始化手牌、地主、地主牌
            gameInit();   
            // 初始化房间信息（玩家、房间号）
            roomInit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void gameStart(){
        Service<Void> gameService=new GameStartListenService(stream, callPass, callPut, rightPutPane, leftPutPane, putPane, pLeftTurnLabel, pRightTurnLabel, pLeftNameLabel, pRightNameLabel,overText,readyBtn);
        gameService.start();
    }

    private void gameInit(){
        // enhance(增强): 监听其他玩家选择
        Service<Void> readyService=new GameReadyListenService(stream, pLeftTurnLabel, pRightTurnLabel, pLeftNameLabel, pRightNameLabel, callY, callN, bossPokerPane, pokerPane,startHidden);
        readyService.start();
    }

    private void roomInit(){
        // 监听房间信息 的后台线程 来获取实时数据 (并实时更新UI——需要在FX App Thread主UI线程中操作,而Service实现了这种)
        Service<Void> roomService=new RoomListenService(stream, roomIDLabel, waitTip, pLeftNameLabel, pRightNameLabel, pokerPane,pLeftTurnLabel,pRightTurnLabel);
        roomService.start();
    }

    @FXML
    private void onCallPut(){
        try {
            if(!GameHolder.rule.valid()){
                // 复位选中的凸起的牌
                CommonUtils.recoverPokerPane(PlayerHolder.putPokerList, pokerPane);
                return;
            }
            PlayerHolder.player.getPokers().removeAll(PlayerHolder.putPokerList);
            String over=""; // 将over标识通过最后出的牌一起发送
            if(PlayerHolder.player.getPokers().size()==0) over="(over)/"+PlayerHolder.player.getName();
            stream.writeStr(JSON.toJSONString(PlayerHolder.putPokerList)+over);
            // 以下顺序不能任意改变
            CommonUtils.removePokerPane(PlayerHolder.putPokerList, pokerPane);       
            PlayerHolder.putPokerList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        callPut.setVisible(false);
        callPass.setVisible(false);
    }

    @FXML
    private void onCallPass(){
        try {
            if(RoomHolder.lastPokerList==null||RoomHolder.lastPokerList.size()==0) return;
            stream.writeStr("[]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        callPut.setVisible(false);
        callPass.setVisible(false);
    }

    @FXML
    private void onCallY(){
        try {
            stream.writeStr("1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        callY.setVisible(false);
        callN.setVisible(false);
    }

    @FXML
    private void onCallN(){
        try {
            stream.writeStr("0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        callN.setVisible(false);
        callY.setVisible(false);
    }

    @FXML
    private void onReady(){
        try {
            waitTip.setText("已准备");
            readyBtn.setVisible(false);
            stream=PlayerHolder.stream;
            stream.writeStr(PlayerHolder.player.getName());
            driveStart();
        } catch (Exception e) {
            e.printStackTrace();
            waitTip.setText("服务器异常，请退出后重试！");
        }
    }

    @FXML
    private void driveStart() throws Exception{
        // 重置视图
        viewReset();
        // 重置数据
        dataReset();
        // 初始化游戏开始前的数据
        init();
    }

    private void viewReset(){
        pokerPane.getChildren().clear();
        bossPokerPane.getChildren().clear();
        putPane.getChildren().clear();
        rightPutPane.getChildren().clear();
        leftPutPane.getChildren().clear();
        pLeftTurnLabel.setText("");
        pRightTurnLabel.setText("");
        pLeftNameLabel.setStyle("-fx-text-fill: black;-fx-font-size: 16;");
        pRightNameLabel.setStyle("-fx-text-fill: black;-fx-font-size: 16;");
        overText.setVisible(false);
    }

    private void dataReset(){
        RoomHolder.lastPokerList.clear();
        PlayerHolder.putPokerList.clear();
        PlayerHolder.player.setBoss(false);
        PlayerHolder.player.getPokers().clear();
        GameHolder.game.setOver(false);
    }



}
