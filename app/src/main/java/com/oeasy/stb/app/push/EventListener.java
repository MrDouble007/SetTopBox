package com.oeasy.stb.app.push;

/**
 * Created by Steely on 2016/6/21.
 */
public interface EventListener {
    void onConnected();

    void onExceptionCaught(Throwable cause);

    void onMessageSent(Object message);

    //message 正常只有可能是RequestData 或者 ResponseData
    void onMessageReceived(Object message);

    void onDisConnected();

    void onLoginSuccess(Object message);
}
