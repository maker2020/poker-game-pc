package client.fxclient.bootstrap;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * SPI
 * 这是一个基于Spi思想设计的类
 */
public class Bootstrap {
    
    /**
     * 加载类的时候要注意的是：<p>
     * 不同类加载器之间即便相同也不认可为相同。（在类型转换时会错误）<p>
     * 在Java SPI机制的jdk包中ServiceLoader可以实现service provider interface
     * 它让provider和consumer解耦，但java常用接口如jdbc在核心库，由bootstrap类加载器加载，而双亲委派限制，1.2后jdk引入
     * ThreadContextClassLoader，它破坏了双亲委派模型的设计，从而在接口层通过xxx可以访问到系统类加载器。<p>
     * 具体ServiceLoader参考api，这里指出resources/META-INF/services下的是它的配置路径。<p>
     * 
     * 本客户端用Spi加载boot实现，meta-inf指出的实现也在本客户端中。<p>
     * 而实际中，meta-inf是由各实现方写好，并打包。开发者引入jar后，jdk核心库调用spi寻找实现。（这里三方会使用上下文加载器，从而使核心层spi可以加载实现类）
     */
    public static void boot() {
        try {
            SmyClassLoader smyClassLoader=new SmyClassLoader();
            // 这里不去像jdkSPI的形式实现，因为jdk是通用环境，第三方可以直接使用jdk接口。这个Boot接口引入再弄很麻烦。注释以下即可
            // 所以这里使用本地直接获取实现就好了。不然此处加载器用Thread.getContextClassLoader，第三方再set它自己的加载器即可。
            // 意思就是，这里省略了第三方和线程上下文类加载器。直接用自定义类加载器加载实现即可。
            ServiceLoader<Boot> serviceLoader=ServiceLoader.load(Boot.class,smyClassLoader);
            Iterator<Boot> it=serviceLoader.iterator();
            while (it.hasNext()) {
                Boot boot=it.next();
                boot.info();
            }
        } catch (Exception e) {
            // do nothing
        }
    }

}
