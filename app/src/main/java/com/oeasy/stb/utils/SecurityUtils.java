package com.oeasy.stb.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Mr.Double
 * @data 2018/7/31-19:48
 * Description:
 */
public class SecurityUtils {
    private static final Object PARTNER_KEY = "219F223CB59B183D3A80708E8553AFA71b49";
    private static final Object PARTNER_KEY_STORE = "db426a9829e4b49a0dcac7b4162da6b6";

    public static String signParams(Map<String, Object> params, int keyType) {
        LinkedList<KeyValue> paramList = sortParam(params);
        StringBuilder sb = new StringBuilder();

        for (KeyValue pair : paramList) {
            sb.append(pair.getKey()).append("=").append(pair.getValue()).append("&");
        }

        sb.append("key=").append(keyType == 0 ? PARTNER_KEY : PARTNER_KEY_STORE);

        String sign = getMessageDigest(sb.toString().getBytes());
        return sign;
    }

    public static LinkedList<KeyValue> sortParam(Map<String, Object> params) {
        LinkedList<KeyValue> pairs = new LinkedList<KeyValue>();
        ArrayList<String> temp = new ArrayList<String>();
        for (String key : params.keySet()) {
            temp.add(key);
        }
        temp.trimToSize();
        Collections.sort(temp);
        Gson gson = new Gson();
        for (String string : temp) {
            Object valueObj = params.get(string);
            String value = "";
            if (valueObj != null) {
                if (valueObj instanceof Integer || valueObj instanceof String || valueObj instanceof Double || valueObj instanceof Float
                        || valueObj instanceof Long || valueObj instanceof Boolean || valueObj instanceof Byte || valueObj instanceof Character) {
                    value = valueObj.toString();
                } else {
                    value = gson.toJson(valueObj);
                }
            }
            pairs.add(new KeyValue(string, value));
        }
        return pairs;
    }

    public final static String getMessageDigest(byte[] buffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            byte[] md = mdTemp.digest(buffer);
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "V1.0.0";
        }
    }

    public static class KeyValue {
        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
