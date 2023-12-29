package client.fxclient;

import client.fxclient.bootstrap.Bootstrap;
import client.fxclient.bootstrap.FXDLLFileLoader;
import javafx.application.Application;

/**
 * 本应用基于FX的UI框架<p>
 * 其中涉及的问题：获得数据后需要sleep后再update，否则未知阻塞<p>
 * 另外，不会javaFX，因此使用上有所冗余，没有使用组件选择器，传参过多导致可读性差
 */
public class FXClientApp {
    public static void main(String[] args) throws Exception{
        FXDLLFileLoader.loadDLL();
        Bootstrap.boot();
        Application.launch(FXEntrance.class);
    }
}
