package com.oeasy.stb.mvp.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Message;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oeasy.stb.R;
import com.oeasy.stb.app.service.STBService;
import com.oeasy.stb.mvp.base.BaseActivity;
import com.oeasy.stb.mvp.contract.FirstPageContract;
import com.oeasy.stb.utils.SpfUtil;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.mvp.model.manager.ResolverManager;
import com.oeasy.stb.mvp.presenter.FirstPagePresenter;
import com.oeasy.stb.mvp.model.IUpdateNotiListener;
import com.oeasy.stb.mvp.ui.widget.CommonLoadingDialog;
import com.oeasy.stb.mvp.ui.widget.ConfirmDialog;
import com.oeasy.stb.mvp.ui.widget.DefaultBadgeView;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.SafeHandler;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * @author Mr.Double
 * @data 2018/7/26-9:50
 * Description: 首页
 */
public class FirstPageActivity extends BaseActivity implements FirstPageContract.View,
        View.OnClickListener, ConfirmDialog.ClickListenerInterface, IUpdateNotiListener {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.im_qr_code)
    ImageView mQrImage;
    @BindView(R.id.iv_head_icon)
    ImageView mOwnerHead;
    @BindView(R.id.tv_device_status)
    TextView mBindStatus;
    @BindView(R.id.tv_device_owner_name)
    TextView mBindName;
    @BindView(R.id.tv_device_status_des)
    TextView mBindAddress;
    @BindView(R.id.ll_device_noti)
    LinearLayout mDevNoti;
    @BindView(R.id.ll_device_watch)
    LinearLayout mDevWatch;
    @BindView(R.id.tv_device_noti)
    TextView mTvNoti;

    private static final int MSG_START_STBSERVICE = 1001;
    private static final int MSG_GET_DEVICE_STATUS = 1002;
    private static final int MSG_GET_NOTICE_LIST = 1003;
    private static final int MSG_UPDATE_UNREAD_COUNT = 1004;
    private final int REQUESTCODE_FIRST_PAGE = 1;

    private FirstPageContract.Presenter mPresenter;
    private ConfirmDialog mConfirmDialog;
    private CommonLoadingDialog mLoadingDialog;
    private DefaultBadgeView mBadgeView;
    private FirstPageHandler mHandler;
    private FirstPageReceiver mBindReceiver;
    private boolean mIsDeviceBind; // 设备是否绑定
    private int mUnReadCount; // 未读通知数量

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        OeLog.i(TAG, "initView() enter.");
        return R.layout.activity_first_page;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        OeLog.i(TAG, "initData() enter.");
        // 注册推送监听
        ResolverManager.getManager().registUpdateNotiListener(this);
        mPresenter = new FirstPagePresenter();
        mPresenter.attachView(this);
        mHandler = new FirstPageHandler(this);
        mDevNoti.setOnClickListener(this);
        mDevWatch.setOnClickListener(this);
        initLoadingDialog();
        mPresenter.getQrContent();
        getDeviceStatus();
        mHandler.sendEmptyMessageDelayed(MSG_START_STBSERVICE, 1000);
        mBindReceiver = new FirstPageReceiver();
        registerReceiver();
    }

    private void registerReceiver() {
        OeLog.i(TAG, "registerReceiver() enter.");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_UPDATE_UI);
        filter.addAction(Constants.ACTION_BIND_DEVICE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBindReceiver, filter);
    }

    private void unregisterReceiver() {
        OeLog.i(TAG, "unregisterReceiver() enter.");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBindReceiver);
    }

    @Override
    protected void onDestroy() {
        OeLog.i(TAG, "onDestroy() enter.");
        if (null != mPresenter) {
            mPresenter.detachView();
        }

        if (null != mConfirmDialog) {
            mConfirmDialog.dismiss();
            mConfirmDialog = null;
        }

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        OeLog.i(TAG, "onClick() enter.");
        switch (v.getId()) {
            case R.id.ll_device_noti:
                OeLog.i(TAG, "onClick() ll_device_noti.");
                handleNotiClick();
                break;
            case R.id.ll_device_watch:
                OeLog.i(TAG, "onClick() ll_device_watch.");
                showConfirmDialog(this,
                        getString(R.string.residential_watch_dog_des),
                        getString(R.string.residential_unbind_confirm),
                        null,
                        false);
                break;
            default:
                OeLog.e(TAG, "onClick() invalid id.");
                break;
        }
    }

    private void handleNotiClick() {
        OeLog.i(TAG, "handleNotiClick() enter.");
        if (mIsDeviceBind) {
            // 绑定成功才获取本地数据库通知数据
            mPresenter.getDbNotiList();
        } else {
            showConfirmDialog(this,
                    getString(R.string.residential_unbind_tip),
                    getString(R.string.residential_unbind_confirm),
                    null,
                    false);
        }
    }

    private void showConfirmDialog(Context context, String titleMsg, String confirmMsg, String cancelMsg, boolean isBothIcon) {
        OeLog.i(TAG, "showConfirmDialog() enter.");
        if (null == context) {
            OeLog.e(TAG, "showConfirmDialog() context is null.");
            return;
        }

        mConfirmDialog = new ConfirmDialog(context, titleMsg, confirmMsg, cancelMsg, isBothIcon);
        mConfirmDialog.setClickListenerInterface(this);
        mConfirmDialog.show();
    }

    private void initLoadingDialog() {
        OeLog.i(TAG, "initLoadingDialog() enter.");
        mLoadingDialog = new CommonLoadingDialog(this, R.style.CommonDialogTheme, getString(R.string.loading_tip_common));
        mLoadingDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void doConfirm() {
        OeLog.i(TAG, "doConfirm() enter.");
        mConfirmDialog.dismiss();
    }

    @Override
    public void doCancel() {
        OeLog.i(TAG, "doCancel() enter.");
        mConfirmDialog.dismiss();
    }

    /**
     * 展示获取到的二维码信息
     *
     * @param qrBitmap 二维码信息生成的Bitmap
     */
    @Override
    public void showQrCode(Bitmap qrBitmap) {
        OeLog.i(TAG, "showQrCode() enter.");
        if (null == qrBitmap) {
            OeLog.e(TAG, "showQrCode() qrBitmap is null, show default.");
            mQrImage.setImageResource(R.mipmap.bg_qr_no_net);
            return;
        }
        mQrImage.setImageBitmap(qrBitmap);
    }

    /**
     * 展示设备状态
     *
     * @param isBind  是否绑定成功
     * @param name    业主名称或暂无
     * @param address 业主房屋或未绑定描述
     */
    @Override
    public void showDeviceStatus(boolean isBind, String name, String address) {
        OeLog.i(TAG, "showDeviceStatus() isBind：" + isBind + ", name: " + name + ", address: " + address);
        mIsDeviceBind = isBind;
        if (mIsDeviceBind) {
            mOwnerHead.setImageResource(R.mipmap.icon_head_bind);
            mBindStatus.setText(getString(R.string.device_status_bind));
            mBindStatus.setTextColor(getResources().getColor(R.color.colorTextContentGreen));
            mBindStatus.setBackgroundResource(R.drawable.rectangle_shape_green);
            mBindName.setText(TextUtils.isEmpty(name) ? getString(R.string.device_status_unbind_name) : getString(R.string.device_status_bind_name, name));
            // 绑定成功才显示未读消息以及获取服务器端通知列表
            mBadgeView = new DefaultBadgeView(this, mTvNoti);
            mPresenter.getNotiUnReadCount();
            mPresenter.getNetNotiList();
        } else {
            mOwnerHead.setImageResource(R.mipmap.icon_head_unbind);
            mBindStatus.setText(getString(R.string.device_status_unbind));
            mBindStatus.setTextColor(getResources().getColor(R.color.colorTextTitleGray));
            mBindStatus.setBackgroundResource(R.drawable.rectangle_shape_gray);
            mBindName.setText(name);
        }
        mBindAddress.setText(address);
    }

    /**
     * 展示通知消息
     *
     * @param notiInfoList 获取到的通知列表数据
     */
    @Override
    public void showNotiList(ArrayList<NotiItemInfo> notiInfoList) {
        OeLog.i(TAG, "showNotiList() enter.");
        if (null == notiInfoList || 0 == notiInfoList.size()) {
            showConfirmDialog(this,
                    getString(R.string.residential_no_noti_tip),
                    getString(R.string.residential_unbind_confirm),
                    null,
                    false);
            OeLog.e(TAG, "showNotiList() notiInfoList size is 0.");
            return;
        }
        startNotiListActivityForResult(notiInfoList);
    }

    /**
     * 展示未读通知的条数
     *
     * @param count 未读通知的条数
     */
    @Override
    public void setNotiUnReadCount(int count) {
        OeLog.i(TAG, "setNotiUnReadCount() count: " + count);
        if (count < 0) {
            mUnReadCount = 0;
        } else {
            mUnReadCount = count;
        }
        mHandler.sendEmptyMessage(MSG_UPDATE_UNREAD_COUNT);

    }

    private void setNotiUnReadCount() {
        OeLog.i(TAG, "setNotiUnReadCount() mUnReadCount: " + mUnReadCount);
        if (!mIsDeviceBind) {
            OeLog.i(TAG, "setNotiUnReadCount() device not bind.");
            mBadgeView.setCount(0);
            return;
        }
        mBadgeView.setCount(mUnReadCount);
    }

    /**
     * 继续获取绑定状态
     */
    @Override
    public void getDeviceStatusLoop() {
        OeLog.i(TAG, "getDeviceStatusLoop() enter.");
        if (mHandler.hasMessages(MSG_GET_DEVICE_STATUS)) {
            mHandler.removeMessages(MSG_GET_DEVICE_STATUS);
        }
        mHandler.sendEmptyMessageDelayed(MSG_GET_DEVICE_STATUS, 5 * 1000);
    }

    @Override
    public void getNetNotiList() {
        OeLog.i(TAG, "getNetNotiList() enter.");
        mHandler.sendEmptyMessageDelayed(MSG_GET_NOTICE_LIST, 10 * 1000);
    }

    @Override
    public void showLoading() {
        OeLog.i(TAG, "showLoading() enter.");
        mLoadingDialog.show();
    }

    @Override
    public void hideLoading() {
        OeLog.i(TAG, "hideLoading() enter.");
        mLoadingDialog.dismiss();
    }

    /**
     * 获取设备绑定状态
     */
    private void getDeviceStatus() {
        OeLog.i(TAG, "getDeviceStatus() enter.");
        mPresenter.getDeviceStatus();
    }

    private void startNotiListActivityForResult(ArrayList<NotiItemInfo> notiInfoList) {
        OeLog.i(TAG, "startNotiListActivityForResult() enter.");

        Intent intent = new Intent(this, NotiListActivity.class);
        intent.putExtra(Constants.KEY_NOTI_LIST_INFO, notiInfoList);
        intent.putExtra(Constants.KEY_NOTI_UNREAD_COUNT, mUnReadCount);
        startActivityForResult(intent, REQUESTCODE_FIRST_PAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        OeLog.i(TAG, "onActivityResult() requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (REQUESTCODE_FIRST_PAGE == requestCode && Constants.RESULTCODE_OK == resultCode) {
            if (null == data) {
                OeLog.e(TAG, "onActivityResult() data is null.");
                return;
            }
            setNotiUnReadCount(data.getIntExtra(Constants.KEY_NOTI_UNREAD_COUNT, 0));
        }
    }

    @Override
    public void updateNoti(NotiItemInfo info) {
        OeLog.i(TAG, "updateNoti() enter.");
        mUnReadCount++;
        mHandler.sendEmptyMessage(MSG_UPDATE_UNREAD_COUNT);
    }

    private static class FirstPageHandler extends SafeHandler<FirstPageActivity> {
        private final String TAG = this.getClass().getSimpleName();

        public FirstPageHandler(FirstPageActivity objs) {
            super(objs);
        }

        @Override
        public void handlerMessageAction(Message msg) {
            if (null == msg) {
                OeLog.e(TAG, "handlerMessageAction() msg is null.");
                return;
            }

            switch (msg.what) {
                case MSG_START_STBSERVICE:
                    OeLog.i(TAG, "handlerMessageAction() MSG_START_STBSERVICE.");
                    getObj().startStbService();
                    break;
                case MSG_GET_DEVICE_STATUS:
                    OeLog.i(TAG, "handlerMessageAction() MSG_GET_DEVICE_STATUS.");
                    getObj().getDeviceStatus();
                    break;
                case MSG_GET_NOTICE_LIST:
                    OeLog.i(TAG, "handlerMessageAction() MSG_GET_NOTICE_LIST.");
                    getObj().mPresenter.getNetNotiList();
                    break;
                case MSG_UPDATE_UNREAD_COUNT:
                    OeLog.i(TAG, "handlerMessageAction() MSG_UPDATE_UNREAD_COUNT.");
                    getObj().setNotiUnReadCount();
                    break;
                default:
                    OeLog.e(TAG, "handlerMessageAction() invalid msg.");
                    break;
            }
        }
    }

    private void startStbService() {
        OeLog.i(TAG, "startStbService() enter.");
        Intent intent = new Intent(this.getApplicationContext(), STBService.class);
        intent.setAction(Constants.ACTION_STBSERVICE);
        startService(intent);
    }

    private class FirstPageReceiver extends BroadcastReceiver {

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
                case Constants.ACTION_BIND_DEVICE:
                    OeLog.i(TAG, "onReceive() ACTION_BIND_DEVICE.");
                    String bindType = intent.getStringExtra(Constants.KEY_BIND);
                    handleBindType(bindType);
                    break;
                case Constants.ACTION_UPDATE_UI:
                    boolean isConnected = intent.getBooleanExtra(Constants.KEY_UPDATE_UI, false);
                    OeLog.i(TAG, "onReceive() ACTION_UPDATE_UI, isConnected: " + isConnected);
                    if (isConnected) {
                        getDeviceStatus();
                    }
                    mPresenter.getQrContent();
                    break;
                default:
                    OeLog.e(TAG, "onReceive() invalid action.");
                    break;
            }
        }
    }

    private void handleBindType(String type) {
        if (TextUtils.isEmpty(type)) {
            OeLog.e(TAG, "handleBindType() type is empty.");
            return;
        }

        OeLog.i(TAG, "handleBindType() type: " + type);
        if (Constants.VALUE_BIND_ROOM.equals(type)) {
            // 收到绑定设备先清除一下之前的数据再重新绑定
            clearDeviceStatus();
            getDeviceStatus();
        } else if (Constants.VALUE_UNWRAP_ROOM.equals(type)) {
            clearDeviceStatus();
        } else {
            OeLog.e(TAG, "handleBindType() invalid type.");
        }
    }

    private void clearDeviceStatus() {
        OeLog.e(TAG, "clearDeviceStatus() enter.");
        showDeviceStatus(false, getString(R.string.device_status_unbind_name), getString(R.string.device_status_unbind_des));
        SpfUtil.setBulidingCode("");
        SpfUtil.setUnitId(0);
        if (0 != mUnReadCount) {
            setNotiUnReadCount(0);
        }
        // 清除数据库
        mPresenter.clearDbData();
    }
}
