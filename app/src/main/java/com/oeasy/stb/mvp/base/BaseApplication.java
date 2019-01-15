package com.oeasy.stb.mvp.base;

import android.app.Application;
import android.content.Context;

import com.oeasy.stb.BuildConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * @author Mr.Double
 * @data 2018/7/25-9:38
 * Description:
 */
public class BaseApplication extends Application {

    private RefWatcher mRefWatcher;

    private static Context mContext;

    /**
     * 这里会在 {@link BaseApplication#onCreate} 之前被调用,可以做一些较早的初始化
     * 常用于 MultiDex 以及插件化框架的初始化
     *
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //LeakCanary 内存泄露检查
        initLeakDetect();
    }

    private void initLeakDetect() {
        mRefWatcher = BuildConfig.DEBUG ? LeakCanary.install(this) : RefWatcher.DISABLED;
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (null != mRefWatcher) {
            mRefWatcher = null;
        }

        if (null != mContext) {
            mContext = null;
        }
    }
}
