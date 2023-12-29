package game.room;

import java.util.LinkedList;
import java.util.List;

import game.poker.Poker;

public class RoomHolder {
    
    public static Room room;
    
    /**
     * 记录需要压制的牌
     */
    public static List<Poker> lastPokerList=new LinkedList<>();

}
