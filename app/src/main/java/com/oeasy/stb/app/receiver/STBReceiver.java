package com.oeasy.stb.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.oeasy.stb.app.service.STBService;
import com.oeasy.stb.mvp.base.BaseApplication;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;

/**
 * @author Mr.Double
 * @data 2018/8/14-17:19
 * Description:
 */
public class STBReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        OeLog.i(TAG, "onReceive() enter.");
        if (null == intent) {
            OeLog.e(TAG, "onReceive() intent is null.");
            return;
        }

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            OeLog.e(TAG, "onReceive() action is null.");
            return;
        }

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                OeLog.i(TAG, "onReceive() ACTION_BOOT_COMPLETED enter.");
                startStbService(BaseApplication.getAppContext());
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                OeLog.i(TAG, "onReceive() CONNECTIVITY_ACTION enter.");
                updateFirstPageUi(context);
                break;
            default:
                OeLog.e(TAG, "onReceive() invalid action.");
                break;
        }
    }

    private void startStbService(Context context) {
        OeLog.i(TAG, "startStbService() enter.");
        Intent intent = new Intent(context, STBService.class);
        intent.setAction(Constants.ACTION_STBSERVICE);
        context.startService(intent);
    }

    private void updateFirstPageUi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            OeLog.e(TAG, "updateFirstPageUi() cm is null.");
            return;
        }

        NetworkInfo info = cm.getActiveNetworkInfo();

        boolean isConnected = null != info && info.isConnected();
        OeLog.i(TAG, "updateFirstPageUi() info.isConnected: " + isConnected);
        Intent intent = new Intent(Constants.ACTION_UPDATE_UI);
        intent.putExtra(Constants.KEY_UPDATE_UI, isConnected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
