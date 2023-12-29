package game.mode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 支持热加载
 * 若要修改Game实现，新增java类，在resources/config/rule.json下修改
 * 
 * 扩展：同步客户端修改代码，动态获取mode再选择(实现模式热更新)
 */
public class GameModeLoader {

    public static List<String> classNameList=new ArrayList<>();

    /**
     * 返回Game的java实现类的字节<p>
     * 注：将该byte[]转string不要进行编码
     */
    public static byte[] loadClass() throws Exception {
        File file = new File("src/main/resources/config/mode.json");
        FileInputStream fin = new FileInputStream(file);
        byte[] dat = fin.readAllBytes();
        fin.close();
        String ruleConfig = new String(dat, "UTF-8");
        JSONObject jobj = JSON.parseObject(ruleConfig);
        JSONArray classNameArray = jobj.getJSONArray("modes");
        List<byte[]> clazzList=new ArrayList<>();
        for(int i=0;i<classNameArray.size();i++){
            classNameList.add(classNameArray.getString(i));
            clazzList.add(readFromBin(classNameArray.getString(i)));
        }
        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        ObjectOutputStream ot=new ObjectOutputStream(bout);
        ot.writeObject(clazzList.toArray());
        return bout.toByteArray();
    }

    protected static byte[] readFromBin(String name) throws Exception{
        String[] packageSplit=name.split("\\.");
        StringBuilder builder=new StringBuilder();
        String simpleName=packageSplit[packageSplit.length-1];
        for(int i=0;i<packageSplit.length-1;i++){
            builder.append(packageSplit[i]+"/");
        }
        File file=new File("bin/"+builder.toString()+simpleName+".class");
        FileInputStream fin=new FileInputStream(file);
        byte[] data=fin.readAllBytes();
        fin.close(); 
        return data;
    }

}
