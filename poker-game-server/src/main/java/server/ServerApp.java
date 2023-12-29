package server;

import game.mode.GameModeLoader;
import game.player.Player;
import game.room.Room;
import game.room.RoomManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import util.SuitStream;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本应用的序列化传输 可用 JSON/XML框架 代替（区别在速度上、内存上、解析复杂等方面有差异）
 * 应知晓：
 * 1.明确Socket网络流的数据默认是append(true)模式。
 * 2.封装的SuitIO流的write/read方法都为<行操作>。
 * 3.Socket流与文件流不同的是，Socket流没有eof（End of File)，shutdownXXX即eof标识
 * 
 * <b>实际中会是游戏引擎或框架，它们封装了底层，约定了协议，从而更好地实现客户端和服务器的交互</b>
 * 
 * <p>
 * 为了实现开闭原则、热更新等（如游戏规则/模式等），服务端和客户端进行了拆分。
 */
public class ServerApp {

    public static void main(String[] args) {
        try {
            try (ServerSocket connection = new ServerSocket(8080)) {
                init();
                ExecutorService exec = Executors.newFixedThreadPool(50);
                while (true) {
                    Socket req = connection.accept();
                    try {
                        exec.execute(() -> {
                            try {
                                handleRequest(req);
                            } catch (Exception e) {
                                // 服务器处理异常
                                e.printStackTrace();
                            } finally {
                                try {
                                    req.close();
                                } catch (IOException e1) {
                                    System.out.println("Request未正常关闭");
                                }
                            }
                        });   
                    } catch (Exception e) {
                        // 线程池异常
                        System.out.println("线程池异常");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void init() {
        ServerHolder.roomManager = new RoomManager();
        ServerHolder.playerStreamManager = new HashMap<>();
        System.out.println("""
                room manager has been loaded.
                player manager has been loaded.
                server is running with port:8080.
                """);
    }

    private static void handleRequest(Socket request) throws Exception {
        Player player=null;
        try {
            System.out.println("ip:" + request.getInetAddress().toString() + " logged");
            SuitStream stream = new SuitStream(request.getInputStream(), request.getOutputStream());
            // 给客户端加载模式
            stream.writeObject(GameModeLoader.loadClass());
            stream.writeObject(GameModeLoader.classNameList.toArray());
            String mode = stream.readStr();
            String name = stream.readStr();
    
            System.out.println("username[" + name + "] is matching game:" + mode + "...");
            player = new Player(name);
            ServerHolder.playerStreamManager.put(player, stream);
            Room room = ServerHolder.roomManager.casualJoinRoom(player, mode);
            player.setReady(true);
            room.getLatch().countDown();
            room.serverForRoom(player, stream);   
        } catch (Exception e) {
            // 清除，释放长期持有的ServerHolder的playerManager中的内存。否则泄露
            ServerHolder.playerStreamManager.remove(player);
            System.out.println("玩家<"+player.getName()+">内存清理完毕");
            throw e;
        }
    }

}
