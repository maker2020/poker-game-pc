package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

public class SuitInputStream {
    
    private BufferedReader reader;
    private ObjectInputStream objReader;

    private SuitOutputStream writer;

    /**
     * 初始化两个对象
     * 1.缓存字符输入流。
     * 2.对象输入流（阻塞直到该输入流的输出端ObjectOutputStream构造后，才会取消阻塞去构造。）
     * 所以使用时，要注意这点，确保输出已被构造。
     * @param inputStream
     * @throws Exception
     */
    public SuitInputStream(InputStream inputStream) throws Exception{
        this.objReader=new ObjectInputStream(inputStream);
        this.reader=new BufferedReader(new InputStreamReader(inputStream));
    }

    public void setWriter(SuitOutputStream writer) {
        this.writer = writer;
    }

    /**
     * 注意：这里与文件等输入流等不同，网络Socket传输流没有结束标识(即EOF：false)，因此readAllbytes会无限阻塞。
     * 因此，自定义标识符EOF。
     * 
     * 或用 缓存区流，它们提供行读。
     * 但注意，不能反复通过共同对象inputStream来构造新的Reader，因为无法让多个Reader共用inputStream。
     * 
     * 另外还需注意，写入流中的数据字节是以什么字符集编码的，就应该保持一致。但Client和Server都未设置，则IDE等内置环境因素必须统一。
     * @return
     * @throws Exception
     */
    public String read() throws Exception{
        return reader.readLine();
    }

    /**
     * 读取对象
     * 每次读取前会发送flag通知reader，以取消它的阻塞。
     * 
     * 原因：objectIO和网络io一起使用，当字符串后接obj读取会阻塞（未知）
     * @return
     * @throws Exception
     */
    public Object readObj() throws Exception{
        writer.write("done"); //flag done
        try {
            return objReader.readObject();
        } catch (Exception e) {
            throw new Exception("exception:C/S获取object冲突");
        }
    }

}
