package client.fxclient.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 应用(System)加载器-自定义加载器
 * 它用来给ServiceLoader提供加载器，寻找类以及读取加载的办法。
 * 在本例中仅是一个示范，将lib中的加密class加载到jvm
 */
public class SmyClassLoader extends ClassLoader{

    /**
     * 不破坏双亲委派模型
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException{
        Class<?> c=null;
        // 此处设计不完善，未关联findClass和getData
        byte[] classData=getData();
        if(classData!=null){
            // 字节码转 实例Class<?>
            c=defineClass(name, classData, 0, classData.length);
        }
        return c;
    }

    /**
     * 读取
     * 方式有很多，可以从jar读，压缩包，文件夹等格式读取（此处是指定文件）
     * 注：defineClass只能识别java字节码。(因此不能是java代码)
     * 
     * 考虑到打包成jar的情况，Vscode默认打包普通项目，其中lib不能直接作为URL中的路径。
     * @return
     */
    private byte[] getData() {
        try {
            InputStream in=getClass().getResourceAsStream("/native-spi/BootSample.clazz");
            // ByteArrayOutputStream构造无输出终端，输出终端为byte[]内存
            ByteArrayOutputStream out = null;
            try {
                out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = in.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
    
                    e.printStackTrace();
                }
            }
            byte[] data=out.toByteArray();
            return data;   
        }catch (Exception e) {
            System.out.println("异常：spi实现加载失败");
            return null;
        }
    }

}