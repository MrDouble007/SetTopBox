package com.oeasy.stb.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by wp.nine on 2015/7/14.
 */
public abstract class SafeHandler<T> extends Handler {
    WeakReference<T> mObj;
    public SafeHandler(T objs){
        this.mObj = new WeakReference<T>(objs);
    }

    public T getObj(){
        return mObj.get();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(!isObjectExist()){
            return;
        }
        handlerMessageAction(msg);
    }

    public abstract void handlerMessageAction(Message msg);

    public boolean isObjectExist(){
        if(mObj == null || mObj.get() == null){
            return false;
        }
        return true;
    }
}