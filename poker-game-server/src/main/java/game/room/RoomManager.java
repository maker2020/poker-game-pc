package game.room;

import game.Game;
import game.player.Player;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomManager {
    
    private final AtomicInteger id=new AtomicInteger(1);

    protected final static Set<Room> rooms=Collections.synchronizedSet(new HashSet<>());

    /**
     * 根据模式匹配房间
     * @param mode
     * @return
     */
    private Room getSuitRoom(String mode){
        for(Room room:rooms){
            String gameTypeName=room.getGame().getClass().getName();
            if(room.getPlayers().size()<3 && gameTypeName.equals(mode)) return room;
        }
        return null;
    }

    private Room newRoom(String mode) throws Exception{
        Room room=new Room();
        room.setGame(loadModeGame(mode));
        room.setId(id.getAndIncrement());
        rooms.add(room);
        return room;
    }

    private Game loadModeGame(String mode) throws Exception{
        Class<?> clazz=Class.forName(mode);
        Game game=(Game)clazz.getConstructor().newInstance();
        return game;
    }

    /**
     * if the server exist room,then return that,else return a new room.
     * @param player
     * @return
     */
    public Room casualJoinRoom(Player player,String mode) throws Exception{
        Room room=getSuitRoom(mode);
        if(room==null) room=newRoom(mode);
        // 等同于 添加监听者
        room.addPlayer(player);
        // room变化，充当事件源，需向监听者(Player)更新
        room.notifyPlayer();
        return room;
    }
    
}
