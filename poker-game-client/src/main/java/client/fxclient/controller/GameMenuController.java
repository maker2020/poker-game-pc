package client.fxclient.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import client.fxclient.FXEntrance;
import game.player.Player;
import game.player.PlayerHolder;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import util.SuitStream;

public class GameMenuController {

    @FXML
    private Pane loginPane;

    @FXML
    private Pane menuPane;

    @FXML
    private void startGame(){
        menuPane.setVisible(false);
        loginPane.setVisible(true);
    }

    private void init(){
        try {
            // 从 [游戏文件夹的配置文件] 中读取服务器地址（方便改动）
            Map<String,String> config=getConfig();
            PlayerHolder.req=new Socket(config.get("ip"), Integer.parseInt(config.get("port")));
            PlayerHolder.stream=new SuitStream(PlayerHolder.req.getInputStream(), PlayerHolder.req.getOutputStream());                
        } catch (Exception e) {
            System.out.println("Init Connection Exception");
            e.printStackTrace();
        }
    }

    @FXML
    private void onEntered(Event event) throws IOException {
        if(event instanceof KeyEvent){
            KeyEvent keyEvent=(KeyEvent)event;
            if(keyEvent.getCode()==KeyCode.ENTER){
                PlayerHolder.player=new Player(((TextField)keyEvent.getSource()).getText());
                init();
                FXEntrance.setRoot("game_mode");
            }
        }
    }

    private Map<String,String> getConfig() throws Exception{
        Map<String,String> map=new HashMap<>();
        try {
            File file=new File("config/server.txt");
            FileInputStream fin=new FileInputStream(file);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fin));
            map.put("ip",reader.readLine());  
            map.put("port", reader.readLine()); 
            reader.close();
        } catch (Exception e) {
            map.put("ip", InetAddress.getLocalHost().getHostAddress());
            map.put("port", 8080+"");
        }
        System.out.println("服务器:"+map.get("ip"));
        return map;
    }
}
