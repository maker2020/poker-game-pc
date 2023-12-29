package util;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class SuitOutputStream {
    
    private OutputStream outputStream;
    private ObjectOutputStream objWriter;

    private SuitInputStream reader;

    public SuitOutputStream(OutputStream outputStream) throws Exception{
        this.objWriter=new ObjectOutputStream(outputStream);
        this.outputStream=outputStream;
    }

    public void setReader(SuitInputStream reader) {
        this.reader = reader;
    }

    // 默认内置的字符编码集可能是ISO-xxx，最好手动设置UTF系列全球通用字符编码集
    public void write(String str) throws Exception{
        outputStream.write((str+"\r\n").getBytes());
        outputStream.flush();
    }

    /**
     * 写对象（序列化）
     * 每次读obj之前都会阻塞等到input流传来的flag标识才会开始读。
     * @param obj
     * @throws Exception
     */
    public void writeObj(Object obj) throws Exception{
        reader.read(); // flag done
        objWriter.writeObject(obj);
        objWriter.flush();
    }

}
