package game.poker.util;

import java.util.Collections;
import java.util.List;

import game.poker.Poker;
import game.poker.Poker.PokerComparator;

public class PokerUtil {
    
    private static PokerComparator comparator=new Poker.PokerComparator();

    public static void sort(List<Poker> list){
        Collections.sort(list, comparator);
    }

}
