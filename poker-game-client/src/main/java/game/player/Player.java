package game.player;

import game.poker.Poker;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 仅用id做hashcode和equals关键
@EqualsAndHashCode(of="name")
public class Player implements Serializable{
    private String name;
    private List<Poker> pokers=new ArrayList<>();
    private Long restMillions;
    private Long money;
    private int reqTimes=0;
    private int reqIndex=0;
    /**
     * 是否拒绝过当地主
     */
    private boolean refuseBoss=false;
    private boolean firstCall=false;
    private boolean boss=false;
    private boolean pass;
    private boolean ready;

    public Player(String name){
        this.name=name;
    }

    public void addPoker(Poker poker){
        pokers.add(poker);
    }

    public void addAllPoker(List<Poker> listPoker){
        pokers.addAll(listPoker);
    }

    public void reqBoss(){
        reqTimes++;
    }

    @Override
    public String toString(){
        return "name:"+name+" money:"+money;
    }
}
