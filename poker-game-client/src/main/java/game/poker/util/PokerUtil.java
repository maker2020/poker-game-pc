package game.poker.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.poker.Poker;
import game.poker.Poker.PokerComparator;

public class PokerUtil {
    
    private static PokerComparator comparator=new Poker.PokerComparator();

    /**
     * 一般排序（从大到小排）
     * @param list
     */
    public static void sort(List<Poker> list){
        Collections.sort(list, comparator);
    }
    
    /**
     * 出牌统一排序规则（示例：按一般排序，结果为6333；该排序的结果为3336）
     * @param list
     */
    public static void sortPutType(List<Poker> list){
        // 记录各牌出现次数
        Map<Poker,Integer> countMap=new HashMap<>();
        for(Poker p:list){
            if(!countMap.containsKey(p)){
                countMap.put(p, 1);
            }else{
                countMap.put(p, countMap.get(p)+1);
            }
        }
        // 出现次数多的牌放在前面
        Collections.sort(list,(p1,p2)->{
            if(countMap.get(p1)>countMap.get(p2)) return -1;
            else if(countMap.get(p1)<countMap.get(p2)) return 1;
            else{
                if(p1.getValueEnum().getWeight()>p2.getValueEnum().getWeight()) return 1;
                else if(p1.getValueEnum().getWeight()<p2.getValueEnum().getWeight()) return -1;
                else return 0;
            }
        });
    }
}
