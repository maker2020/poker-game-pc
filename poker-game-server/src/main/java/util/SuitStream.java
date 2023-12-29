package util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 输入输出流
 * 使用注意：
 * 封装reader和writer，属于平行模型
 * 两端同时readObj会抛出异常，这种场景推荐用json/xml作为载体
 */
public class SuitStream {
    
    private SuitInputStream reader;
    private SuitOutputStream writer;

    public SuitStream(InputStream in,OutputStream out) throws Exception{
        writer=new SuitOutputStream(out);
        reader=new SuitInputStream(in);
        reader.setWriter(writer);
        writer.setReader(reader);
    }

    /**
     * 写字符串(自动换行)
     * @param str
     * @throws Exception
     */
    public void writeStr(String str) throws Exception{
        writer.write(str);
    }

    /**
     * 写对象。（block，等待标识传过来取消阻塞）
     * @param object
     * @throws Exception
     */
    public void writeObject(Object object) throws Exception{
        writer.writeObj(object);
    }

    /**
     * 读字符串。（行读）
     * @return
     * @throws Exception
     */
    public String readStr() throws Exception{
        return reader.read();
    }

    /**
     * 读对象。（会传递标识给对方的ObjectInputStream）
     * @return
     * @throws Exception
     */
    public Object readObject() throws Exception{
        return reader.readObj();
    }

}
