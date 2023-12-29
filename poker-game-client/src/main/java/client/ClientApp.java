package client;

import game.poker.Poker;
import game.room.Room;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import util.SuitInputStream;
import util.SuitOutputStream;
import java.util.stream.Collectors;

/**
 * ClientApp
 */
public class ClientApp {

    private Scanner input;
    private Socket req;
    private SuitOutputStream writer;
    private SuitInputStream reader;
    
    public static String username="";

    public ClientApp() throws Exception{
        input=new Scanner(System.in);
    }

    public static void main(String[] args) {
        try {
            ClientApp clientApp=new ClientApp();
            int mode=clientApp.initMenu();
            if(mode==1){
                System.out.println("请输入用户名：");
                String name=clientApp.input.next();
                ClientApp.username=name;
                System.out.println("正在匹配，请等待...");
                clientApp.loadGameOne(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGameOne(String name) throws Exception{
        req=new Socket(InetAddress.getLocalHost().getHostAddress(), 8080);
        writer=new SuitOutputStream(req.getOutputStream());
        reader=new SuitInputStream(req.getInputStream());
        writer.write("mode1");
        writer.write(name);
        System.out.println("随机匹配中...");
        String resp=reader.read();
        System.out.println("server:"+resp);

        Room room=(Room)reader.readObj();
        System.out.println("房间信息："+room.toString());
        System.out.println("操作选项：1.准备；2.离开");
        int op=input.nextInt();
        if(op==2) return;
        if(op==1) writer.write("ready");
        System.out.println("等待全部玩家到齐、准备后即可自动开始，请等待...");
        getInitPokers(reader);
        control();
        // wait
        input.nextInt();
    }

    private void control() throws Exception{
        boolean firstCall="isFirstCall".equals(reader.read());
        String opMenu=reader.read();
        System.out.println(opMenu);
        String op=input.next();
        writer.write(op);
        if("1".equals(op)&&firstCall){
            opMenu=reader.read();
            System.out.println(opMenu);
            op=input.next();
            writer.write(op);
        }
        // 展示 叫地主的情况
        String res=reader.read();
        System.out.println("地主是："+res);
        Object[] pokerBossArr=(Object[])reader.readObj();
        List<Poker> pokerBossList=Arrays.asList(pokerBossArr).stream().map((t)->{return (Poker)t;}).collect(Collectors.toList());
        System.out.print("地主牌：");
        for(Poker poker:pokerBossList){
            System.out.print(poker.getColorEnum().getColor()+poker.getValueEnum().getValue()+" ");
        }
        System.out.println();
        if(ClientApp.username.equals(res)){
            Object[] pokers=(Object[]) reader.readObj();
            List<Poker> pokerList=Arrays.asList(pokers).stream().map((t)->{return (Poker)t;}).collect(Collectors.toList());
            System.out.println("您的手牌：");
            for(Poker poker:pokerList){
                System.out.print(poker.getColorEnum().getColor()+poker.getValueEnum().getValue()+" ");
            }
            System.out.println();
        }
    }

    private void getInitPokers(SuitInputStream reader) throws Exception{
        Object[] pokerObjArr=(Object[])reader.readObj();
        List<Poker> pokerList=Arrays.asList(pokerObjArr).stream().map((t)->{return (Poker)t;}).collect(Collectors.toList());
        System.out.println("您的手牌：");
        for(Poker poker:pokerList){
            System.out.print(poker.getColorEnum().getColor()+poker.getValueEnum().getValue()+" ");
        }
        System.out.println();
    }

    private int initMenu(){
        System.out.println("欢迎来到Samay多人斗地主");
        System.out.println("模式选择：");
        System.out.println("1.快速开始（随机匹配）");
        System.out.println("...");
        System.out.println("请输入：");
        return input.nextInt();
    }
}