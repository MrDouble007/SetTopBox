package com.oeasy.stb.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.oeasy.stb.mvp.base.BaseApplication;

/**
 * @author Mr.Double
 * @data 2018/8/2-9:18
 * Description: 保存SharedPreferences数据
 */
public class SpfUtil {
    private static final String NAME_SHAREDP_LIST = "ostb_sp_list";
    private static final String SPS_KEY_UNITID = "dev_unitid";
    private static final String SPS_KEY_BULIDINGCODE = "dev_bulidingCode";
    private static final String SPS_KEY_ROOMCODE = "dev_roomCode";

    private static SpfUtil mInstance;
    private SharedPreferences mSps;

    private SpfUtil() {
        mSps = BaseApplication.getAppContext().getSharedPreferences(NAME_SHAREDP_LIST, Context.MODE_PRIVATE);
    }

    private static SpfUtil getInstance() {
        if (null == mInstance) {
            synchronized (SpfUtil.class) {
                if (null == mInstance) {
                    mInstance = new SpfUtil();
                }
            }
        }
        return mInstance;
    }

    public static int getUnitId() {
        return getInstance().mSps.getInt(SPS_KEY_UNITID, 0);
    }

    public static void setUnitId(int value) {
        getInstance().mSps.edit().putInt(SPS_KEY_UNITID, value).apply();
    }

    public static String getBulidingCode() {
        return getInstance().mSps.getString(SPS_KEY_BULIDINGCODE, "");
    }

    public static void setBulidingCode(String value) {
        getInstance().mSps.edit().putString(SPS_KEY_BULIDINGCODE, value).apply();
    }

    public static String getRoomCode() {
        return getInstance().mSps.getString(SPS_KEY_ROOMCODE, "");
    }

    public static void setRoomCode(String value) {
        getInstance().mSps.edit().putString(SPS_KEY_ROOMCODE, value).apply();
    }

}
