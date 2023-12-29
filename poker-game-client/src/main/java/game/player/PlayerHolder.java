package game.player;

import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import game.poker.Poker;
import util.SuitStream;

public class PlayerHolder {

    public static Player player;
    
    public static Socket req;
    public static SuitStream stream;

    /**
     * 选中的牌<p>
     * 这里LinkedList和ArrayList性能差距不大，都行。<p>
     * 但它线程不一定安全，用同步list<p>
     * 为了牌校验方便，没有指定为final
     */
    public static List<Poker> putPokerList=Collections.synchronizedList(new LinkedList<>());

}
