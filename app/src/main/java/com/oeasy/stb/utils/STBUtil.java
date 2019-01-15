package com.oeasy.stb.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Mr.Double
 * @data 2018/7/31-15:36
 * Description:
 */
public class STBUtil {
    private static final String TAG = "STBUtil";

    private static final String SYSTEMPROPERTIES = "android.os.SystemProperties";
    private static final String KEY_SN = "ro.serialno";
    private static final String KEY_MAC = "persist.sys.net.mac";

    /**
     * @return 设备唯一ID
     */
    public static String getDeviceMac() {
        OeLog.i(TAG, "getDeviceMac() enter.");
        return getAndroidOsSystemProperties(KEY_MAC).replace(":", "");
    }

    public static String getDeviceSn() {
        OeLog.i(TAG, "getDeviceSn() enter.");
        return getAndroidOsSystemProperties(KEY_SN);
    }

    /**
     * @return 操作码
     */
    public static String getOperateCode() {
        OeLog.i(TAG, "getOperateCode() enter.");
        return "Oeasy_STB_OperateCode";
    }

    public static String getDateByLongByPattern(long time, String pattern) {
        Date date = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        return simpleDateFormat.format(date);
    }

    @SuppressLint("PrivateApi")
    private static String getAndroidOsSystemProperties(String key) {
        String ret = null;
        try {
            Method get = Class.forName(SYSTEMPROPERTIES).getMethod("get", String.class);
            ret = (String) get.invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
            OeLog.e(TAG, "getOperateCode() Exception occured e: " + e.getMessage());
        }

        return ret;
    }

    /**
     * 将字符串装换为对象
     *
     * @param t   object
     * @param str object to json
     * @return object
     */
    public static <T> T toEntity(T t, String str) {

        JSONObject json = null;
        try {
            json = new JSONObject(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _toEntity(t, json);
    }

    /**
     * 将json转换为对象
     *
     * @param t    object
     * @param json object
     * @return object
     */
    private static <T> T _toEntity(T t, JSONObject json) {
        Class clazz = t.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Method[] methods = clazz.getMethods();

        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                Object value = json.get(fieldName);
                if (value == null) continue;
                for (Method method : methods) {
                    String methodName = method.getName();
                    if (methodName.startsWith("set") && methodName.toLowerCase().equals("set" + fieldName.toLowerCase())) {
                        try {
                            if ("Date".equals(field.getType())) {
                                if (!"".equals(value)) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                                    Date date = formatter.parse(value.toString());
                                    method.invoke(t, date);
                                    break;
                                }
                            } else if ("Integer".equals(field.getType())) {
                                method.invoke(t, Integer.parseInt(value.toString()));
                                break;
                            } else {
                                method.invoke(t, value);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (org.json.JSONException e) {
                //   LogHelper.log("error==========>>>>>>>>" + e.toString());
            }
        }
        return t;
    }

    /**
     * 对网络连接状态进行判断
     *
     * @return true, 可用； false， 不可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connManager) {
            OeLog.e(TAG, "isNetworkAvailable() connManager is null.");
            return false;
        }

        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }
}
