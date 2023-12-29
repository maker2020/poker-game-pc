package game.utils.classloader;

import java.util.ArrayList;
import java.util.List;

/**
 * 专门加载从网络传输过来的Game实现
 */
public class GameClassLoader extends ClassLoader{

    public Class<?> findClass(String name,byte[] classDat) throws ClassNotFoundException{
        Class<?> c=null;
        if(classDat!=null){
            c=defineClass(name, classDat, 0, classDat.length);
        }
        // init
        resolveClass(c);
        return c;
    }

    /**
     * 
     * @param classDataList
     * @return
     */
    public List<Class<?>> findAllClass(List<byte[]> classDataList,List<String> classNameList) throws Exception{
        List<Class<?>> classList=new ArrayList<>();
        for(int i=0;i<classDataList.size();i++){
            classList.add(findClass(classNameList.get(i),classDataList.get(i)));
        }
        return classList;
    }

}
