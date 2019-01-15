package com.oeasy.stb.app.service;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oeasy.stb.R;
import com.oeasy.stb.app.push.EventListener;
import com.oeasy.stb.app.push.PushData;
import com.oeasy.stb.app.push.PushManager;
import com.oeasy.stb.app.push.ServerConfig;
import com.oeasy.stb.mvp.base.BaseApplication;
import com.oeasy.stb.mvp.model.api.ApiFactory;
import com.oeasy.stb.mvp.model.api.RetryWithDelay;
import com.oeasy.stb.mvp.model.api.bean.BaseHttpConsumer;
import com.oeasy.stb.mvp.model.api.bean.BasePushAction;
import com.oeasy.stb.mvp.model.api.bean.BaseResponse;
import com.oeasy.stb.mvp.model.entity.IncallPushInfo;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.mvp.model.manager.ResolverManager;
import com.oeasy.stb.mvp.ui.widget.InCallDialog;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.STBUtil;
import com.oeasy.stb.utils.SafeHandler;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Mr.Double
 * @data 2018/8/9-10:00
 * Description:
 */
public class STBService extends BaseService implements EventListener, InCallDialog.ClickListenerInterface {
    private final String TAG = this.getClass().getSimpleName();

    private static final int MSG_START_REGISTER_PUSH_CENTER = 2001;
    private static final int MSG_SHOW_INCALL_DIALOG = 2002;
    private static final int MSG_UPDATE_INCALL_PIC = 2003;
    private static final int MSG_SHOW_TOAST_MSG = 2004;
    private static final int MSG_DISMISS_INCALL_DIALOG = 2005;

    private static final int DELAY_TIME_DIALOG_DISMISS = 55 * 1000; // 55s取消弹窗

    private final String KEY_LOCATION = "location";
    private final String KEY_PIC_URL = "pic_url";
    private final String KEY_TOAST_MSG = "toast_msg";

    private StbHandler mHandler;
    private InCallDialog mInCallDialog;
    private IncallPushInfo mPushInfo;
    private Boolean mSelfDismiss = false; // 是否是己方操作导致mInCallDialog消失

    @Override
    public void init() {
        OeLog.i(TAG, "init() enter.");
        // 设置服务为前台进程，保活
        startForeground(2, new Notification());
        mHandler = new StbHandler(this);
        mHandler.sendEmptyMessage(MSG_START_REGISTER_PUSH_CENTER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OeLog.i(TAG, "onStartCommand() enter.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OeLog.i(TAG, "onDestroy() enter.");
        clearDisposable();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    private void startRegisterPushCenter() {
        OeLog.i(TAG, "startRegisterPushCenter() enter.");
        if (STBUtil.isNetworkAvailable(BaseApplication.getAppContext())) {
            registerPushCenter();
        } else {
            OeLog.e(TAG, "startRegisterPushCenter() network is unReachable.");
            mHandler.sendEmptyMessageDelayed(MSG_START_REGISTER_PUSH_CENTER, 3 * 1000);
        }
    }

    @Override
    public void onConnected() {
        OeLog.i(TAG, "onConnected() enter.");
    }

    @Override
    public void onExceptionCaught(Throwable cause) {
        OeLog.e(TAG, "onExceptionCaught() cause: " + cause.getMessage());
    }

    @Override
    public void onMessageSent(Object message) {
        OeLog.i(TAG, "onMessageSent() enter.");
    }

    @Override
    public void onMessageReceived(Object message) {
        OeLog.i(TAG, "onMessageReceived() enter.");
        if (message instanceof PushData) {
            handlePushMessage((PushData) message);
        } else {
            OeLog.e(TAG, "onMessageReceived() invalid message.");
        }
    }

    @Override
    public void onDisConnected() {
        OeLog.i(TAG, "onDisConnected() enter.");
    }

    @Override
    public void onLoginSuccess(Object message) {
        OeLog.i(TAG, "onLoginSuccess() enter.");
        if (message instanceof PushData) {
            handlePushMessage((PushData) message);
        } else {
            OeLog.e(TAG, "onLoginSuccess() invalid message.");
        }
    }

    @Override
    public void doConfirm() {
        OeLog.i(TAG, "doConfirm() enter.");
        mSelfDismiss = true;
        openDoor();
    }

    @Override
    public void doCancel() {
        OeLog.i(TAG, "doCancel() enter.");
        mSelfDismiss = true;
        rejectOpenDoor();
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_INCALL_DIALOG, 2 * 1000);
    }

    private static class StbHandler extends SafeHandler<STBService> {
        private final String TAG = this.getClass().getSimpleName();

        public StbHandler(STBService objs) {
            super(objs);
        }

        @Override
        public void handlerMessageAction(Message msg) {
            if (null == msg) {
                OeLog.e(TAG, "handlerMessageAction() msg is null.");
                return;
            }

            switch (msg.what) {
                case MSG_START_REGISTER_PUSH_CENTER:
                    OeLog.i(TAG, "handlerMessageAction() MSG_START_REGISTER_PUSH_CENTER.");
                    getObj().startRegisterPushCenter();
                    break;
                case MSG_SHOW_INCALL_DIALOG:
                    OeLog.i(TAG, "handlerMessageAction() MSG_SHOW_INCALL_DIALOG.");
                    getObj().showInCallDialog(getObj(), msg.getData().getString(getObj().KEY_LOCATION));
                    // 弹窗后55s消失
                    if (hasMessages(MSG_DISMISS_INCALL_DIALOG)) {
                        removeMessages(MSG_DISMISS_INCALL_DIALOG);
                    }
                    sendEmptyMessageDelayed(MSG_DISMISS_INCALL_DIALOG, DELAY_TIME_DIALOG_DISMISS);
                    break;
                case MSG_UPDATE_INCALL_PIC:
                    OeLog.i(TAG, "handlerMessageAction() MSG_UPDATE_INCALL_PIC.");
                    if (null == getObj().mInCallDialog) {
                        OeLog.e(TAG, "handlerMessageAction() mInCallDialog is null.");
                        return;
                    }
                    getObj().mInCallDialog.setPicUrl(msg.getData().getString(getObj().KEY_PIC_URL));
                    break;
                case MSG_SHOW_TOAST_MSG:
                    OeLog.i(TAG, "handlerMessageAction() MSG_SHOW_TOAST_MSG.");
                    getObj().showToast(getObj().getApplicationContext(), msg.getData().getString(getObj().KEY_TOAST_MSG));
                    break;
                case MSG_DISMISS_INCALL_DIALOG:
                    OeLog.i(TAG, "handlerMessageAction() MSG_DISMISS_INCALL_DIALOG.");
                    getObj().dismissDialog();
                    break;
                default:
                    OeLog.e(TAG, "handlerMessageAction() invalid msg.");
                    break;
            }
        }
    }

    private void registerPushCenter() {
        OeLog.i(TAG, "registerPushCenter() enter.");
        Disposable disposable = ApiFactory.getDeviceService().getPushAccount(STBUtil.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse<ServerConfig>>() {
                    @Override
                    public void onSuccessedCall(BaseResponse<ServerConfig> response) {
                        OeLog.i(TAG, "registerPushCenter() onSuccessedCall enter.");
                        ServerConfig config = response.getData();
                        if (null == config) {
                            OeLog.e(TAG, "registerPushCenter() config is null.");
                            return;
                        }

                        OeLog.i(TAG, "registerPushCenter() config: " + config.toJson());
                        PushManager pushManager = PushManager.getInstance(STBService.this, config);
                        pushManager.registerListener(STBService.this);
                    }

                    @Override
                    public void onFailedCall(BaseResponse<ServerConfig> response) {
                        OeLog.e(TAG, "registerPushCenter() onFailedCall response: " + response.toJson());
                        mHandler.sendEmptyMessageDelayed(MSG_START_REGISTER_PUSH_CENTER, 5 * 1000);
                    }

                    @Override
                    public void onCompletedCall(BaseResponse<ServerConfig> response) {
                        OeLog.i(TAG, "registerPushCenter() onCompletedCall enter.");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "registerPushCenter() accept enter: " + throwable.getMessage());
                    }
                });
        addDisposable(disposable);
    }

    private void handlePushMessage(PushData pushData) {
        OeLog.i(TAG, "handlePushMessage() enter.");
        String action = pushData.getAction();
        if (TextUtils.isEmpty(action)) {
            OeLog.e(TAG, "handlePushMessage() action is empty.");
            return;
        }

        OeLog.i(TAG, "handlePushMessage() action: " + action);
        JSONObject object = null;
        try {
            object = new JSONObject(action);
        } catch (JSONException e) {
            OeLog.e(TAG, "handlePushMessage() JSONException occured.");
        }

        if (null == object) {
            OeLog.e(TAG, "handlePushMessage() object is null.");
            return;
        }

        String type;
        BasePushAction pushAction = null;
        IncallPushInfo pushInfo = null;
        if (object.has("msgType")) {
            pushAction = new Gson().fromJson(action, BasePushAction.class);
            type = pushAction.getMsgType();
            OeLog.i(TAG, "handlePushMessage() pushAction: " + pushAction.toJson());
        } else if (object.has("typeC")) {
            pushInfo = new Gson().fromJson(action, IncallPushInfo.class);
            type = pushInfo.getTypeC();
            OeLog.i(TAG, "handlePushMessage() pushInfo: " + pushInfo.toJson());
        } else {
            OeLog.e(TAG, "handlePushMessage() invalid push type.");
            return;
        }
        switch (type) {
            case Constants.PUSH_TYPE_NOTICE:
                OeLog.i(TAG, "handlePushMessage() PUSH_TYPE_NOTICE.");
                if (null == pushAction) {
                    OeLog.e(TAG, "handlePushMessage() pushAction is null.");
                    return;
                }
                handleNoticeType(pushAction);
                break;
            case Constants.PUSH_TYPE_INCALL:
                OeLog.i(TAG, "handlePushMessage() PUSH_TYPE_INCALL.");
                if (null == pushInfo) {
                    OeLog.e(TAG, "handlePushMessage() pushInfo is null.");
                    return;
                }
                handleIncallType(pushInfo);
                break;
            default:
                OeLog.e(TAG, "handlePushMessage() invalid push type.");
                break;
        }
    }

    private void showInCallDialog(Context context, String address) {
        OeLog.i(TAG, "showInCallDialog() enter.");
        if (null == context) {
            OeLog.e(TAG, "showInCallDialog() context is null.");
            return;
        }

        if (null != mInCallDialog && mInCallDialog.isShowing()) {
            mInCallDialog.dismiss();
        }

        mInCallDialog = new InCallDialog(context, address);
        // 设置成系统级对话框
        mInCallDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        mInCallDialog.setClickListenerInterface(this);
        mInCallDialog.show();
    }

    private void handleIncallType(IncallPushInfo pushInfo) {
        OeLog.i(TAG, "handleIncallType() enter, cmd: " + pushInfo.getCmd());
        switch (pushInfo.getCmd()) {
            // 呼叫命令；1=对讲申请呼叫；2=图片更新；3=对讲挂断；4=呼叫被人接听
            case 1:
                mPushInfo = pushInfo;
                // 先告知服务器收到推送
                notifiServerGetIncall(pushInfo);
                // 再获取呼叫过来的门口机名称
                getCallDoorName(pushInfo);
                break;
            case 2:
                String picUrl = pushInfo.getImage();
                if (TextUtils.isEmpty(picUrl)) {
                    OeLog.e(TAG, "handleIncallType() picUrl is empty.");
                    return;
                }

                if (null == mInCallDialog) {
                    OeLog.e(TAG, "handleIncallType() mInCallDialog is null.");
                    return;
                }
                Message msg = mHandler.obtainMessage(MSG_UPDATE_INCALL_PIC);
                Bundle bundle1 = new Bundle();
                bundle1.putString(KEY_PIC_URL, picUrl);
                msg.setData(bundle1);
                msg.sendToTarget();
                break;
            case 3:
                if (null == mInCallDialog) {
                    OeLog.e(TAG, "handleIncallType() mInCallDialog is null.");
                    return;
                }

                if (!mSelfDismiss) {
                    Message msg2 = mHandler.obtainMessage(MSG_SHOW_TOAST_MSG);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString(KEY_TOAST_MSG, getString(R.string.incall_visitor_hangup));
                    msg2.setData(bundle2);
                    msg2.sendToTarget();
                }
                mHandler.sendEmptyMessage(MSG_DISMISS_INCALL_DIALOG);
                break;
            case 4:
                if (null == mInCallDialog) {
                    OeLog.e(TAG, "handleIncallType() mInCallDialog is null.");
                    return;
                }

                if (!mSelfDismiss) {
                    Message msg3 = mHandler.obtainMessage(MSG_SHOW_TOAST_MSG);
                    Bundle bundle3 = new Bundle();
                    bundle3.putString(KEY_TOAST_MSG, pushInfo.getContent());
                    msg3.setData(bundle3);
                    msg3.sendToTarget();
                }
                mHandler.sendEmptyMessage(MSG_DISMISS_INCALL_DIALOG);
                break;
            default:
                OeLog.e(TAG, "handleIncallType() invalid push cmd.");
                break;
        }
    }

    private void handleNoticeType(BasePushAction pushAction) {
        String cmd = pushAction.getCmd();
        if (TextUtils.isEmpty(cmd)) {
            OeLog.e(TAG, "handleNoticeType() cmd is empty.");
            return;
        }

        OeLog.i(TAG, "handleNoticeType() cmd: " + cmd);
        switch (cmd) {
            case Constants.VALUE_PUSH_NOTICE:
                OeLog.i(TAG, "handleNoticeType() VALUE_PUSH_NOTICE.");
                updateNotice(pushAction);
                break;
            case Constants.VALUE_BIND_ROOM:
                OeLog.i(TAG, "handleNoticeType() VALUE_BIND_ROOM.");
                Intent bindIntent = new Intent(Constants.ACTION_BIND_DEVICE);
                bindIntent.putExtra(Constants.KEY_BIND, Constants.VALUE_BIND_ROOM);
                LocalBroadcastManager.getInstance(this).sendBroadcast(bindIntent);
                break;
            case Constants.VALUE_UNWRAP_ROOM:
                OeLog.i(TAG, "handleNoticeType() VALUE_UNWRAP_ROOM.");
                Intent unbindIintent = new Intent(Constants.ACTION_BIND_DEVICE);
                unbindIintent.putExtra(Constants.KEY_BIND, Constants.VALUE_UNWRAP_ROOM);
                LocalBroadcastManager.getInstance(this).sendBroadcast(unbindIintent);
                break;
            default:
                OeLog.e(TAG, "handleNoticeType() invalid type.");
                break;
        }
    }

    private void updateNotice(BasePushAction pushAction) {
        OeLog.i(TAG, "updateNotice() enter.");
        try {
            // 因后台历史遗留问题，此处不能直接用NotiItemInfo去接收，只能解析出来
            JSONObject object = new JSONObject(pushAction.getNoticeInfo().toString());
            NotiItemInfo info = new NotiItemInfo();
            info.setId(object.getInt("announcementId"));
            info.setAreaid(object.getInt("unitId"));
            info.setContent(object.getString("content"));
            info.setTitle(object.getString("title"));
            info.setNoticeBeginTime(object.getLong("noticeBeginTime"));
            ContentValues values = new ContentValues();
            ResolverManager.getManager().updateAndSaveToDb(info, values);
        } catch (JSONException e) {
            OeLog.e(TAG, "updateNotice() occured: " + e.getMessage());
        }
    }

    private void showToast(Context context, String msg) {
        OeLog.i(TAG, "showToast() enter.");
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    // 告知服务器收到推送
    private void notifiServerGetIncall(IncallPushInfo pushInfo) {
        OeLog.i(TAG, "notifiServerGetIncall() enter.");
        String callId = pushInfo.getCallId();
        if (TextUtils.isEmpty(callId)) {
            OeLog.e(TAG, "notifiServerGetIncall() callId is empty.");
            return;
        }

        String callAccount = pushInfo.getSipAccount();
        if (TextUtils.isEmpty(callAccount)) {
            OeLog.e(TAG, "notifiServerGetIncall() callAccount is empty.");
            return;
        }
        Disposable disposable = ApiFactory.getDeviceService().notifiServerGetIncall(callId, callAccount, Constants.TYPE_STB)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse>() {
                    @Override
                    public void onSuccessedCall(BaseResponse response) {
                        OeLog.i(TAG, "notifiServerGetIncall() onSuccessedCall enter.");
                    }

                    @Override
                    public void onFailedCall(BaseResponse response) {
                        OeLog.i(TAG, "notifiServerGetIncall() onFailedCall: " + response.toJson());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "notifiServerGetIncall() accept enter: " + throwable.getMessage());
                    }
                });
        addDisposable(disposable);
    }

    // 告知服务器拒绝开门
    private void rejectOpenDoor() {
        OeLog.i(TAG, "rejectOpenDoor() enter.");
        if (null == mPushInfo) {
            OeLog.i(TAG, "rejectOpenDoor() enter.");
            return;
        }
        String callId = mPushInfo.getCallId();
        if (TextUtils.isEmpty(callId)) {
            OeLog.e(TAG, "rejectOpenDoor() callId is empty.");
            return;
        }

        String callAccount = mPushInfo.getSipAccount();
        if (TextUtils.isEmpty(callAccount)) {
            OeLog.e(TAG, "rejectOpenDoor() callAccount is empty.");
            return;
        }
        Disposable disposable = ApiFactory.getDeviceService().rejectOpendoor(callId, callAccount, Constants.TYPE_STB)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse>() {
                    @Override
                    public void onSuccessedCall(BaseResponse response) {
                        OeLog.i(TAG, "rejectOpenDoor() onSuccessedCall enter.");
                    }

                    @Override
                    public void onFailedCall(BaseResponse response) {
                        OeLog.i(TAG, "rejectOpenDoor() onFailedCall: " + response.toJson());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "rejectOpenDoor() accept enter: " + throwable.getMessage());
                    }
                });
        addDisposable(disposable);
    }

    // 开门
    private void openDoor() {
        OeLog.i(TAG, "openDoor() enter.");
        if (null == mPushInfo) {
            OeLog.i(TAG, "openDoor() mPushInfo is null.");
            return;
        }
        String callId = mPushInfo.getCallId();
        if (TextUtils.isEmpty(callId)) {
            OeLog.e(TAG, "openDoor() callId is empty.");
            return;
        }

        String callAccount = mPushInfo.getSipAccount();
        if (TextUtils.isEmpty(callAccount)) {
            OeLog.e(TAG, "openDoor() callAccount is empty.");
            return;
        }
        Disposable disposable = ApiFactory.getDeviceService().opendoor(callId, callAccount, Constants.TYPE_STB)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse>() {
                    @Override
                    public void onSuccessedCall(BaseResponse response) {
                        OeLog.i(TAG, "openDoor() onSuccessedCall enter.");
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_INCALL_DIALOG, 2 * 1000);
                    }

                    @Override
                    public void onFailedCall(BaseResponse response) {
                        OeLog.i(TAG, "openDoor() onFailedCall: " + response.toJson());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "openDoor() accept enter: " + throwable.getMessage());
                    }
                });
        addDisposable(disposable);
    }

    // 获取呼叫过来的门口机的名称
    private void getCallDoorName(IncallPushInfo pushInfo) {
        String sipAccount = pushInfo.getSipAccount();
        if (TextUtils.isEmpty(sipAccount)) {
            OeLog.e(TAG, "getCallDoorName() sipAccount is empty.");
            showIncallDialog(pushInfo.getRoomCode());
            return;
        }

        OeLog.i(TAG, "getCallDoorName() sipAccount: " + sipAccount);
        Disposable disposable = ApiFactory.getDeviceService().getCallDoorName(sipAccount)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse<String>>() {
                    @Override
                    public void onSuccessedCall(BaseResponse<String> response) {
                        OeLog.i(TAG, "getCallDoorName() onSuccessedCall response: " + response.toJson());
                        showIncallDialog(response.getData());
                    }

                    @Override
                    public void onFailedCall(BaseResponse<String> response) {
                        OeLog.e(TAG, "getCallDoorName() onFailedCall response: " + response.toJson());
                        showIncallDialog("");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "getCallDoorName() accept response: " + throwable.getMessage());
                        showIncallDialog("");
                    }
                });
        addDisposable(disposable);
    }

    private void showIncallDialog(String deviceName) {
        OeLog.i(TAG, "showIncallDialog() enter.");
        Message message = mHandler.obtainMessage(MSG_SHOW_INCALL_DIALOG);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_LOCATION, deviceName);
        message.setData(bundle);
        message.sendToTarget();
    }

    private void dismissDialog() {
        OeLog.i(TAG, "dismissDialog() enter.");
        if (null == mInCallDialog) {
            OeLog.e(TAG, "dismissDialog() mInCallDialog is null.");
            return;
        }

        mInCallDialog.dismiss();
        if (null != mPushInfo) {
            mPushInfo = null;
        }
        // 重置该值
        mSelfDismiss = false;
        mInCallDialog = null;
    }

}
