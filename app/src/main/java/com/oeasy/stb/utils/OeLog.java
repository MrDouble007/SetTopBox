package com.oeasy.stb.utils;

import android.util.Log;

public class OeLog {
    private static final String TAG = "OESTB";
    private static final boolean DEBUG = true;

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(TAG, tag + ": " + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG, tag + ": " + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG, tag + ": " + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(TAG, tag + ": " + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(TAG, tag + ": " + msg);
        }
    }
}
