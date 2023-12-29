package client.fxclient.service.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import game.player.PlayerHolder;
import game.poker.Poker;
import game.poker.util.PokerUtil;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class CommonUtils {
    
    /**
     * 这是一个不好的封装（标记bad）。
     */
    public static void showOpTip(String playerName,String text,Label pLeftNameLabel,Label pRightNameLabel,Label pLeftTurnLabel,Label pRightTurnLabel,boolean flush){
        // 显示的逻辑
        if(pLeftNameLabel.getText().equals(playerName)){
            pLeftTurnLabel.setText(text);
            pLeftTurnLabel.setVisible(true);
            if(flush) pRightTurnLabel.setVisible(false);
        }else if(pRightNameLabel.getText().equals(playerName)){
            pRightTurnLabel.setText(text);
            pRightTurnLabel.setVisible(true);
            if(flush) pLeftTurnLabel.setVisible(false);
        }else{
            if(flush){
                pLeftTurnLabel.setVisible(false);
                pRightTurnLabel.setVisible(false);
            }
        }
    }

    /**
     * 从Pane移除view(node)，或许要将list转view，或重写hashCode等（但扩展过于单一）
     * 因此，先使用双重循环(非最优T.T，待优化)
     * 额外地，要将移除后的牌向左边缩进（待完善）
     * @param list
     * @param pokerPane
     */
    public static void removePokerPane(List<Poker> list,Pane pokerPane){
        for(int i=0;i<pokerPane.getChildren().size();i++){
            ImageView pokerView=(ImageView)pokerPane.getChildren().get(i);
            for(int j=0;j<list.size();j++){
                if(list.get(j).toString().equals(pokerView.getId())){
                    pokerPane.getChildren().remove(i--);
                }
            }
        }
    }

    /**
     * 出牌不合规后的复位(不必要)<p>
     * 类似removePokerPane(List,Pane)
     * @param list
     * @param pokerPane
     */
    public static void recoverPokerPane(List<Poker> list,Pane pokerPane){
        for(int i=0;i<pokerPane.getChildren().size();i++){
            ImageView pokerView=(ImageView)pokerPane.getChildren().get(i);
            for(int j=0;j<list.size();j++){
                if(list.get(j).toString().equals(pokerView.getId())){
                    pokerView.setLayoutY(15.0);
                    pokerView.setUserData(false);
                }
            }
        }
        list.clear();
    }

    /**
     * 更新客户端UI操作
     * @param pokerList
     */
    public static void displayPoker(List<Poker> pokerList,Pane pokerPane){
        // 先清除，后添加
        pokerPane.getChildren().clear();
        Collection<ImageView> pokerViewCollect=new ArrayList<>();
        double initX=14.0;
        for(Poker p:pokerList){
            ImageView view=new ImageView(new Image("img/"+p.toString()+".png"));
            view.setId(p.toString());
            // <ImageView fitHeight="108.0" fitWidth="70.0" layoutX="22.0" layoutY="15.0" pickOnBounds="true" preserveRatio="true" />
            view.setLayoutX(initX);
            initX+=35;
            view.setLayoutY(15.0);
            view.setFitHeight(108.0);
            view.setFitWidth(70.0);
            view.setUserData(false);
            view.setOnMouseClicked(new EventHandler<Event>() {
                @Override
                public void handle(Event arg0) {
                    if(arg0 instanceof MouseEvent e){
                        if(e.getEventType()==MouseEvent.MOUSE_CLICKED){
                            boolean selected=(boolean)view.getUserData();
                            if(!selected) {
                                view.setLayoutY(5.0);
                                view.setUserData(true);
                                // 添加到Holder里
                                PlayerHolder.putPokerList.add(p);
                            }else{
                                view.setLayoutY(15.0);
                                view.setUserData(false);
                                // 添加到Holder里
                                PlayerHolder.putPokerList.remove(p);
                            }
                            // 这里统一排序，封装好。（客户端对效率影响不大，为了方便）
                            PokerUtil.sortPutType(PlayerHolder.putPokerList);
                        }
                    }
                }
            });
            pokerViewCollect.add(view);
        }
        pokerPane.getChildren().addAll(pokerViewCollect);
    }

}
