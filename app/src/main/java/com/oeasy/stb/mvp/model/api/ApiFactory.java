package com.oeasy.stb.mvp.model.api;

import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.CustomInterceptor;
import com.oeasy.stb.utils.OeLog;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Mr.Double
 * @data 2018/7/31-18:16
 * Description:
 */
public class ApiFactory {
    private static final String TAG = "ApiFactory";

    private static DeviceService mDeviceService;
    private static OkHttpClient mOkHttpClient;
    private static OkHttpClient mOkHttpClientWithSign;

    public static DeviceService getDeviceService() {
        OeLog.i(TAG, "getDeviceService() enter.");
        if (null == mDeviceService) {
            OeLog.i(TAG, "getDeviceService() create DeviceService.");
            String baseUrl = Constants.URL_DEVICE_BASE_URL_PRODUCT;
            mDeviceService = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(getOkHttpClientWithSign())
                    .build()
                    .create(DeviceService.class);
        }
        return mDeviceService;
    }

    private static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
        }
        return mOkHttpClient;
    }

    private static OkHttpClient getOkHttpClientWithSign() {
        if (mOkHttpClientWithSign == null) {
            mOkHttpClientWithSign = new OkHttpClient().newBuilder()
                    .addInterceptor(new CustomInterceptor())
                    .build();
        }
        return mOkHttpClientWithSign;
    }

}
