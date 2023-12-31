package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import game.player.Player;
import game.poker.Poker;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏抽象层
 */
@Data
@NoArgsConstructor
public abstract class Game implements Serializable {
    
    private static final long serialVersionUID=1L;

    private Collection<Poker> pokerCollector;
    private Collection<Poker> pokerBossCollector;
    private List<Player> players=new ArrayList<>();
    private boolean handOut=false;
    private boolean over=false;

    public abstract void init();
    protected abstract void handOutPokers();
    public abstract Player getBossInstantly();

    public void addPlayer(Player player){
        players.add(player);
    }

}
