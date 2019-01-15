package com.oeasy.stb.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * @author Mr.Double
 * @data 2018/8/9-9:47
 * Description: Service基类
 */
public abstract class BaseService extends Service {
    protected CompositeDisposable mCompositeDisposable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //解除订阅
        clearDisposable();
    }

    protected void addDisposable(Disposable disposable) {
        if (null == mCompositeDisposable) {
            mCompositeDisposable = new CompositeDisposable();
        }
        //将所有subscription放入,集中处理
        mCompositeDisposable.add(disposable);
    }

    protected void clearDisposable() {
        if (null != mCompositeDisposable) {
            //保证service结束时取消所有正在执行的订阅
            mCompositeDisposable.clear();
        }
    }

    /**
     * 初始化
     */
    abstract public void init();
}
