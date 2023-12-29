package game.room;

import game.Game;
import game.player.Player;
import java.io.Serializable;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room implements Serializable {

    /**
     * 该类版本变动了，要和客户端保持一致的serailizeUID，否则异常
     */
    private static final long serialVersionUID = 1L;
    

    private int id;
    private String status = "ready";
    private List<Player> players = new CopyOnWriteArrayList<>();
    private Game game;
    private final transient AtomicInteger turnCallIndex = new AtomicInteger(0);

    private transient CountDownLatch latch = new CountDownLatch(3);
    private transient CountDownLatch latch2 = new CountDownLatch(3);
    private final transient Semaphore finishLastCall = new Semaphore(0);
    private final transient CyclicBarrier barrier = new CyclicBarrier(3);
    private Player boss;

    @Override
    public String toString() {
        return "id:" + id + " " + players.toString();
    }
}
