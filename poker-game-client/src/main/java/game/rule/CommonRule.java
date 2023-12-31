package game.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import game.player.PlayerHolder;
import game.poker.Poker;
import game.poker.common.PokerColorEnum;
import game.poker.common.PokerValueEnum;
import game.room.RoomHolder;

/**
 * 通用斗地主规则判断<p>
 * 
 * PECS原则（上界仅get/生产，下界仅add/消费。)
 */
public class CommonRule implements GameRule{

    /**
     * 当前选中的牌（已排好序）<p>
     * 排序规则依次：1.从大到小 2.出现次数最多<p>
     * 示例：3336(√),6333(x)，98765(√)，887766(√)。<p>
     * 基于这个排序，验证规则写起来更简单，可以采用硬编码即：get(1),get(2)
     */
    private Collection<? extends Poker> putPokers=PlayerHolder.putPokerList;
    /**
     * 需要压制的Pokers
     */
    // @SuppressWarnings("unused")
    private Collection<? extends Poker> lastPutPokers=RoomHolder.lastPokerList;

    @Override
    public boolean valid() {
        return inputValid() && compareValid();
    }

    private boolean inputValid(){
        return single()||doublePut()||three()||threeWithOne()||
                threeWithTwo()||planeAlone()||planeWithTwo()||
                planeWithFour()||fourWithTwo()||fourWithFour()||
                singleStraights()||doubleStraights()||boom();
    }

    private boolean compareValid(){
        if(lastPutPokers.size()==0) return true;
        Collection<? extends Poker> backup=putPokers;
        putPokers=lastPutPokers;
        // 验证lastPutPokers后还原引用
        // 前卫式
        if(single()){
            putPokers=backup;
            return bySingle();
        }
        if(doublePut()){
            putPokers=backup;
            return byDoublePut();
        }
        if(three()){
            putPokers=backup;
            return byThree();
        }
        if(threeWithOne()){
            putPokers=backup;
            return byThreeWithOne();
        }
        if(threeWithTwo()){
            putPokers=backup;
            return byThreeWithTwo();
        }
        if(planeAlone()){
            putPokers=backup;
            return byPlaneAlone();
        }
        if(planeWithTwo()){
            putPokers=backup;
            return byPlaneWithTwo();
        }
        if(planeWithFour()){
            putPokers=backup;
            return byPlaneWithFour();
        }
        if(fourWithTwo()){
            putPokers=backup;
            return byFourWithTwo();
        }
        if(fourWithFour()){
            putPokers=backup;
            return byFourWithFour();
        }
        if(singleStraights()){
            putPokers=backup;
            return bySingleStraights();
        }
        if(doubleStraights()){
            putPokers=backup;
            return byDoubleStraights();
        }
        if(boom()){
            putPokers=backup;
            return byBoom();
        }
        putPokers=backup;
        return true;
    }

    private boolean bySingle(){
        if(boom()) return true;
        if(!single()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byDoublePut(){
        if(boom()) return true;
        if(!doublePut()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byThree(){
        if(boom()) return true;
        if(!three()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byThreeWithOne(){
        if(boom()) return true;
        if(!threeWithOne()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byThreeWithTwo(){
        if(boom()) return true;
        if(!threeWithTwo()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byPlaneAlone(){
        if(boom()) return true;
        if(!planeAlone()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byPlaneWithTwo(){
        if(boom()) return true;
        if(!planeWithTwo()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byPlaneWithFour(){
        if(boom()) return true;
        if(!planeWithFour()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byFourWithTwo(){
        if(boom()) return true;
        if(!fourWithTwo()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byFourWithFour(){
        if(boom()) return true;
        if(!fourWithFour()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean bySingleStraights(){
        if(boom()) return true;
        if(!singleStraights()) return false;
        if(lastPutPokers.size()!=putPokers.size()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byDoubleStraights(){
        if(boom()) return true;
        if(!doubleStraights()) return false;
        if(lastPutPokers.size()!=putPokers.size()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    private boolean byBoom(){
        if(!boom()) return false;
        return putPokers.iterator().next().getValueEnum().getWeight()>lastPutPokers.iterator().next().getValueEnum().getWeight();
    }

    /**
     * single、doublePut、threeWithOne、threeWithTwo、
     * planeAlone、planeWithTwo、planeWithFour、
     * fourWithTwo、fourWithFour、
     * singleStraights、doubleStraights、
     * boom
     */
    
    private boolean single(){
        return putPokers.size()==1;
    }

    private boolean doublePut(){
        if(putPokers.size()!=2) return false;
        Iterator<? extends Poker> it=putPokers.iterator();
        return it.next().getValueEnum().equals(it.next().getValueEnum());
    }

    private boolean three(){
        if(putPokers.size()!=3) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        return list.get(0).equals(list.get(1))&&list.get(1).equals(list.get(2));
    }

    private boolean threeWithOne(){
        if(putPokers.size()!=4) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        boolean f1=list.get(0).equals(list.get(1))&&list.get(1).equals(list.get(2));
        boolean f2=!list.get(2).equals(list.get(3));
        return f1&&f2;
    }

    private boolean threeWithTwo(){
        if(putPokers.size()!=5) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        putPokers=list.subList(0, 4);
        boolean f1=threeWithOne();
        boolean f2=list.get(4).equals(list.get(3));
        // 判断完后必须还原引用指向
        putPokers=list;
        return f1&&f2;
    }

    private boolean planeAlone(){
        if(putPokers.size()!=6) return false;
        Iterator<? extends Poker> it=putPokers.iterator();
        Poker last=it.next();
        int countSame=0;
        while (it.hasNext()) {
            Poker now=it.next();
            if(now.getValueEnum()==last.getValueEnum()){
                countSame++;
            }else{
                if(countSame!=2) countSame=0;
            }
            last=now;
        }
        return countSame==4;
    }

    private boolean planeWithTwo(){
        if(putPokers.size()!=8) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        putPokers=list.subList(0, 6);
        boolean f1=planeAlone();
        boolean f2=!list.get(5).equals(list.get(6));
        putPokers=list;
        return f1&&f2;
    }

    private boolean planeWithFour(){
        if(putPokers.size()!=10) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        putPokers=list.subList(0, 6);
        boolean f1=planeAlone();
        List<? extends Poker> subList1=list.subList(6, 8);
        List<? extends Poker> subList2=list.subList(8, 10);
        putPokers=subList1;
        boolean f2=doublePut();
        putPokers=subList2;
        boolean f3=doublePut();
        boolean f4=!subList1.get(0).equals(subList2.get(0));
        putPokers=list;
        return f1&&f2&&f3&&f4;
    }

    private boolean fourWithTwo(){
        if(putPokers.size()!=6) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        Collection<? extends Poker> backup=putPokers;
        putPokers=list.subList(0, 4);
        boolean f1=boom();
        putPokers=list.subList(4, 6);
        boolean f2=!list.get(3).equals(list.get(4)) && !list.get(3).equals(list.get(5));
        putPokers=backup;
        return f1&&f2;
    }

    /**
     * @author 蒋能
     * @return
     */
    private boolean fourWithFour(){
        if(putPokers.size()!=8) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        int i = 0;
        while(i < list.size()){
            if(i < list.size() - 4){
                for( int a = 0; a < 3; a++){
                    if(!list.get(a).equals(list.get(a+1))) return false;
                }
                i = i + 4;
            }else if(i == 4){
                if(!list.get(i).equals(list.get(i + 1))) return false;
                if(list.get(i).equals(list.get(i + 2))) return false;
                i = i + 2;
            }else{
                if(!list.get(i).equals(list.get(i + 1))) return false;
                i = i + 2;
            }
        }
        return true;
    }

    private boolean singleStraights(){
        if(putPokers.size()<5) return false;
        Iterator<? extends Poker> it=putPokers.iterator();
        int lastWeight=it.next().getValueEnum().getWeight();
        while (it.hasNext()) {
            int nowWeight=it.next().getValueEnum().getWeight();
            if(lastWeight+1!=nowWeight) return false;
            lastWeight=nowWeight;
        }
        return true;
    }

    /**
     * @author 蒋能
     * @return
     */
    private boolean doubleStraights(){
        if(putPokers.size()<5 || putPokers.size() % 2 != 0) return false;
        List<? extends Poker> list=(List<? extends Poker>) putPokers;
        int i = 0;
        for( int j =0 ; j < list.size() ; j++ ){
            if(list.get(j).getValueEnum().getWeight() == 13) return false;
        }
        while(i < list.size()){
            if (i < list.size() - 2) {
                if (list.get(i).equals(list.get(i + 1)) && list.get(i).getValueEnum().getWeight() + 1 == list.get(i + 2).getValueEnum().getWeight()  ) {
                    i = i + 2;
                }else{
                    return false;
                }
            }
            else if( i == list.size() - 2){
                if(!list.get(i).equals(list.get(i+1))) return false;
                i = i + 2;
            }
        }
        return true;
    }

    private boolean boom(){
        if(putPokers.size()==2){
            return putPokers.contains(new Poker(PokerColorEnum.BLACK_HEART, PokerValueEnum.Queen)) 
                    && putPokers.contains(new Poker(PokerColorEnum.RED_HEART, PokerValueEnum.King));    
        }else{
            if(putPokers.size()>3){
                Iterator<? extends Poker> it=putPokers.iterator();
                Poker last=it.next();
                while (it.hasNext()) {
                    Poker now=it.next();
                    if(now.getValueEnum()!=last.getValueEnum()){
                        return false;       
                    }
                    last=now;
                }
                return true;
            }
        }
        return false;
    }

}
