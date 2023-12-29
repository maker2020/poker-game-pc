package game.poker;

import game.poker.common.PokerColorEnum;
import game.poker.common.PokerValueEnum;
import java.io.Serializable;
import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poker implements Serializable{
    private PokerColorEnum colorEnum;
    private PokerValueEnum valueEnum;

    public static class PokerComparator implements Comparator<Poker>{
        @Override
        public int compare(Poker o1, Poker o2) {
            if(o1.getValueEnum().getWeight()>o2.getValueEnum().getWeight()) return -1;
            if(o1.getValueEnum().getWeight()<o2.getValueEnum().getWeight()) return 1;
            return 0;
        }
    }

    /**
     * 重写Poker的equals目的为了Map使用，但业务逻辑局限导致hash冲突，暂无可避免。
     */
    @Override
    public boolean equals(Object p){
        if(p instanceof Poker poker){
            return poker.valueEnum==this.valueEnum;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return valueEnum.getCode();
    }

    @Override
    public String toString(){
        return colorEnum.toString()+valueEnum.toString();
    }
}
