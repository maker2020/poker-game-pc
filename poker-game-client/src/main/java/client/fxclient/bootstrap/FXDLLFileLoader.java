package client.fxclient.bootstrap;

public class FXDLLFileLoader{
    
    public static void loadDLL() throws Exception{
        // 这里直接修改路径就可以,根据打包成jar路径，最终整合exe，这里的路径应该为bin，否则vscode为：javafx-sdk-11.0.2/bin
        System.setProperty("java.library.path", "javafx-sdk-11.0.2/bin");
    }

}
