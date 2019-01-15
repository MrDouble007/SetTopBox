package com.oeasy.stb.app.push;

/**
 * Created by steely on 2016/6/21.
 * 这个类没有具体的实现,只是为了让具体的实现类只关心自己想要重写的方法就行了
 */
public class BaseEventListener implements EventListener {

    @Override
    public void onConnected() {

    }

    @Override
    public void onExceptionCaught(Throwable cause) {

    }

    @Override
    public void onMessageSent(Object message) {

    }

    @Override
    public void onMessageReceived(Object message) {

    }

    @Override
    public void onDisConnected() {

    }

    @Override
    public void onLoginSuccess(Object message) {

    }
}
