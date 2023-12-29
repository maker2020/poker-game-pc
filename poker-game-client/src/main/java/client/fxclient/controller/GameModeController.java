package client.fxclient.controller;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import client.fxclient.FXEntrance;
import game.Game;
import game.GameHolder;
import game.player.PlayerHolder;
import game.utils.classloader.GameClassLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import util.SuitStream;

public class GameModeController {
    
    @FXML
    private Pane modePane;
    @FXML
    private Button loadModeBtn;

    private SuitStream stream=PlayerHolder.stream;    

    @FXML
    private void onClick(){
        try {
            loadModeBtn.setVisible(false); 
            // 读取服务器供给的模式
            byte[] data=(byte[])stream.readObject();
            Object[] classNameArr=(Object[])stream.readObject();
            List<String> classNameList=Arrays.asList(classNameArr)
                    .stream().map((t)->{return (String)t;}).collect(Collectors.toList());
            resolveMode(data,classNameList);
        } catch (Exception e) {
            System.out.println("游戏模式加载异常");
            e.printStackTrace();
        }
    }

    private void resolveMode(byte[] data,List<String> classNameList) throws Exception{
        byte[] bdat=data;
        ByteArrayInputStream bin=new ByteArrayInputStream(bdat);
        ObjectInputStream oin=new ObjectInputStream(bin);
        Object[] objArr=(Object[])oin.readObject();
        List<byte[]> gameModeClassData=Arrays.asList(objArr)
                        .stream().map((t)->{return (byte[])t;}).collect(Collectors.toList());
        GameClassLoader gameLoader=new GameClassLoader();
        List<Class<?>> gameModeClass=gameLoader.findAllClass(gameModeClassData,classNameList);
        for(int i=0;i<gameModeClass.size();i++){
            // <Button layoutX="54.0" layoutY="36.0" mnemonicParsing="false" prefHeight="37.0" prefWidth="92.0" text="Button" />
            Class<?> clazz=gameModeClass.get(i);
            Button button=new Button(clazz.getSimpleName());
            button.setLayoutX(63.0);
            button.setLayoutY(36.0);
            button.setPrefHeight(43.0);
            button.setPrefWidth(176.0);
            if(modePane.getChildren().size()>0){
                int size=modePane.getChildren().size();
                button.setLayoutY(modePane.getChildren().get(size-1).getLayoutY()+70);
            }
            button.setOnMouseClicked((event)->{
                try {
                    stream.writeStr(clazz.getName());
                    GameHolder.game=(Game)clazz.getConstructor().newInstance();
                    FXEntrance.setRoot("game_pane");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("mode output exception");
                }
            });
            modePane.getChildren().add(button);
        }
    }

}
