package server;

import java.util.Map;

import game.player.Player;
import game.room.RoomManager;
import util.SuitStream;

public class ServerHolder {

    public static RoomManager roomManager;
    public static Map<Player,SuitStream> playerStreamManager;

}
