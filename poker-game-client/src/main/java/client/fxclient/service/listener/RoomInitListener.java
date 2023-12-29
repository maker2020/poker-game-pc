package client.fxclient.service.listener;

import client.fxclient.service.common.util.CommonUtils;
import game.player.Player;
import game.player.PlayerHolder;
import game.room.Room;
import game.room.RoomHolder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class RoomInitListener implements ChangeListener<String> {

    private Label roomIDLabel;
    private Label waitTip;
    private Label pLeftNameLabel;
    private Label pRightNameLabel;
    private Pane pokerPane;
    private Label pLeftTurnLabel;
    private Label pRightTurnLabel;

    public RoomInitListener(Label roomIDLabel,Label waitTip,Label pLeftNameLabel,Label pRightNameLabel,Pane pokerPane,Label pLeftTurnLabel,Label pRightTurnLabel){
        this.roomIDLabel=roomIDLabel;
        this.waitTip=waitTip;
        this.pLeftNameLabel=pLeftNameLabel;
        this.pRightNameLabel=pRightNameLabel;
        this.pokerPane=pokerPane;
        this.pLeftTurnLabel=pLeftTurnLabel;
        this.pRightTurnLabel=pRightTurnLabel;
    }

    @Override
    public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
        if(arg1.startsWith("ready:")){
            String xxReady=arg1.substring(6);
            if(pLeftNameLabel.getText().equals(xxReady)){
                pLeftTurnLabel.setText("已准备");
            }else if(pRightNameLabel.getText().equals(xxReady)){
                pRightTurnLabel.setText("已准备");
            }
        }else{
            Room room=RoomHolder.room;
            roomIDLabel.setText("房间号："+room.getId());
            for(Player p:room.getPlayers()){
                if(p.getName().equals(PlayerHolder.player.getName())){
                    if(PlayerHolder.player.getPokers().size()>0){
                        CommonUtils.displayPoker(PlayerHolder.player.getPokers(),pokerPane);
                        waitTip.setVisible(false);
                    }
                    continue;
                }
                if("".equals(pLeftNameLabel.getText())) pLeftNameLabel.setText(p.getName());
                else pRightNameLabel.setText(p.getName());
            }
        }
        if(arg1.startsWith("clearReady")){
            pLeftTurnLabel.setText("");
            pRightTurnLabel.setText("");
        }
    }
    
}
