package com.oeasy.stb.utils;

import com.oeasy.stb.mvp.base.BaseApplication;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Mr.Double
 * @data 2018/7/31-18:16
 * Description:
 */
public class CustomInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().url().toString();

        Request.Builder builder = request.newBuilder();
        if ("GET".equals(request.method())) {
            if (url.contains("?")) {
                url = url + "&v=" + SecurityUtils.getVersionName(BaseApplication.getAppContext()) + "&device=android&ttid=" + Constants.TID_ANDROID;
            } else {
                url = url + "?v=" + SecurityUtils.getVersionName(BaseApplication.getAppContext()) + "&device=android&ttid=" + Constants.TID_ANDROID;
            }
            HashMap hashMap = new HashMap();
            String param = url.split("[?]")[1];
            String[] strs = param.split("&");
            for (String str : strs) {
                String[] keyvalue = str.split("=");
                if (2 == keyvalue.length) {
                    hashMap.put(keyvalue[0], URLDecoder.decode(keyvalue[1]));
                } else {
                    hashMap.put(keyvalue[0], null);
                }
            }
            String signedUrl = url + "&sign=" + SecurityUtils.signParams(hashMap, 1);
            OeLog.i("CustomInterceptor", "intercept() signedUrl: " + signedUrl);
            builder.url(signedUrl);
        }

        try {
            return chain.proceed(builder.build());
        } catch (Throwable throwable) {
            throw new IOException("requestFailed " + url, throwable);//增加接口的封装，如果接口挂了并且没有做异常处理，则可以知道哪里错误 by wp.nine
        }
    }

}
