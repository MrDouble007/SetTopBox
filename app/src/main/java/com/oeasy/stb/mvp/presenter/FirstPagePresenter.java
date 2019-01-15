package com.oeasy.stb.mvp.presenter;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.oeasy.stb.R;
import com.oeasy.stb.mvp.base.BaseApplication;
import com.oeasy.stb.mvp.contract.FirstPageContract;
import com.oeasy.stb.mvp.model.api.RetryWithDelay;
import com.oeasy.stb.utils.SpfUtil;
import com.oeasy.stb.mvp.model.api.ApiFactory;
import com.oeasy.stb.mvp.model.api.bean.BaseHttpConsumer;
import com.oeasy.stb.mvp.model.api.bean.BaseResponse;
import com.oeasy.stb.mvp.model.database.DBInfo;
import com.oeasy.stb.mvp.model.entity.DeviceStatusInfo;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.mvp.model.entity.QrCodeInfo;
import com.oeasy.stb.mvp.model.manager.ResolverManager;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.QrCodeUtil;
import com.oeasy.stb.utils.STBUtil;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Mr.Double
 * @data 2018/7/24-19:18
 * Description:
 */
public class FirstPagePresenter implements FirstPageContract.Presenter {
    private final String TAG = this.getClass().getSimpleName();

    private FirstPageContract.View mView;
    private CompositeDisposable mCompositeDisposable;

    public FirstPagePresenter() {
    }

    @Override
    public void attachView(FirstPageContract.View view) {
        this.mView = view;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void detachView() {
        this.mView = null;
        mCompositeDisposable.clear();
        mCompositeDisposable = null;
    }

    @Override
    public boolean isViewAttached() {
        return null != mView;
    }

    @Override
    public void getQrContent() {
        OeLog.i(TAG, "getQrContent() enter.");
        if (STBUtil.isNetworkAvailable(BaseApplication.getAppContext())) {
            OeLog.i(TAG, "getQrContent() network is ok.");
            Bitmap logoBitmap = BitmapFactory.decodeResource(BaseApplication.getAppContext().getResources(), R.mipmap.oeasy_logo);
            Bitmap bgBitmap = QrCodeUtil.generateBitmap(getQrCodeInfo(), 150, 150);
            if (null == bgBitmap) {
                OeLog.e(TAG, "getQrContent() bgBitmap is null.");
                return;
            }
            Bitmap qrBitmap = QrCodeUtil.addLogo(bgBitmap, logoBitmap);

            if (isViewAttached()) {
                mView.showQrCode(qrBitmap);
            }
            logoBitmap.recycle();
            bgBitmap.recycle();
        } else {
            OeLog.e(TAG, "getQrContent() without network.");
            if (isViewAttached()) {
                mView.showQrCode(null);
            }
        }
    }

    @Override
    public void getDeviceStatus() {
        OeLog.i(TAG, "getDeviceStatus() enter.");
        if (isViewAttached()) {
            // 显示加载框
            mView.showLoading();
        }

        mCompositeDisposable.add(ApiFactory.getDeviceService().getDeviceStatus(STBUtil.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse<DeviceStatusInfo>>() {
                    @Override
                    public void onSuccessedCall(BaseResponse<DeviceStatusInfo> response) {
                        OeLog.i(TAG, "getDeviceStatus() onSuccessedCall enter.");
                        DeviceStatusInfo deviceInfo = response.getData();
                        if (null == deviceInfo) {
                            OeLog.e(TAG, "getDeviceStatus() onSuccessedCall deviceInfo is null.");
                            return;
                        }

                        if (0 != deviceInfo.getUnitId()) {
                            SpfUtil.setUnitId(deviceInfo.getUnitId());
                        } else {
                            OeLog.e(TAG, "getDeviceStatus() onSuccessedCall unitId is 0.");
                        }

                        if (!TextUtils.isEmpty(deviceInfo.getBuildingCode())) {
                            SpfUtil.setBulidingCode(deviceInfo.getBuildingCode());
                        } else {
                            OeLog.e(TAG, "getDeviceStatus() onSuccessedCall buildCode is empty.");
                        }

                        if (!TextUtils.isEmpty(deviceInfo.getRoomCode())) {
                            SpfUtil.setRoomCode(deviceInfo.getRoomCode());
                        } else {
                            OeLog.e(TAG, "getDeviceStatus() onSuccessedCall roomCode is empty.");
                        }

                        if (isViewAttached()) {
                            mView.showDeviceStatus(true, deviceInfo.getOwnerName(), deviceInfo.getAddress());
                        }
                    }

                    @Override
                    public void onFailedCall(BaseResponse<DeviceStatusInfo> response) {
                        OeLog.e(TAG, "getDeviceStatus() onFailedCall response: " + response.toJson());
                        // 450代表未绑定房屋，其他异常情况继续掉绑定接口
                        if ("450".equals(response.getCode()) && isViewAttached()) {
                            mView.showDeviceStatus(false, BaseApplication.getAppContext().getString(R.string.device_status_unbind_name),
                                    BaseApplication.getAppContext().getString(R.string.device_status_unbind_des));
                        } else {
                            if (isViewAttached()) {
                                mView.getDeviceStatusLoop();
                            }
                        }

                    }

                    @Override
                    public void onCompletedCall(BaseResponse<DeviceStatusInfo> response) {
                        OeLog.i(TAG, "getDeviceStatus() onCompletedCall enter.");
                        if (isViewAttached()) {
                            // 取消加载框
                            mView.hideLoading();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "getDeviceStatus() accept enter: " + throwable.getMessage());
                        if (isViewAttached()) {
                            // 取消加载框
                            mView.hideLoading();
                            if (isViewAttached()) {
                                mView.getDeviceStatusLoop();
                            }
                        }
                    }
                }));
    }

    /**
     * 获取本地DB通知数据
     */
    @Override
    public void getDbNotiList() {
        OeLog.i(TAG, "getDbNotiList() enter.");
        if (isViewAttached()) {
            // 显示加载框
            mView.showLoading();
        }

        ArrayList<NotiItemInfo> list = new ArrayList<>();
        Gson gson = new Gson();
        Cursor cursor = ResolverManager.getManager().query(DBInfo.COMMUNITY_NOTI_INFO_URI
                , new String[]{DBInfo.NotiColumns.COMMUNITY_NOTI_INFO}
                , null
                , null
                , DBInfo.NotiColumns.COMMUNITY_NOTI_TIME + " DESC");

        if (null == cursor) {
            OeLog.e(TAG, "getDbNotiList() cursor is null.");
            return;
        }

        while (cursor.moveToNext()) {
            String strNotiInfo = cursor.getString(cursor.getColumnIndex(DBInfo.NotiColumns.COMMUNITY_NOTI_INFO));
            list.add(gson.fromJson(strNotiInfo, NotiItemInfo.class));
        }
        cursor.close();

        if (isViewAttached()) {
            mView.showNotiList(list);
            // 取消加载框
            mView.hideLoading();
        }
    }

    /**
     * 获取服务器上的通知数据
     */
    @Override
    public void getNetNotiList() {
        OeLog.i(TAG, "getNetNotiList() enter.");
        mCompositeDisposable.add(ApiFactory.getDeviceService().getDeviceNotiList(SpfUtil.getUnitId()
                , SpfUtil.getBulidingCode()
                , SpfUtil.getRoomCode()
                , null
                , null)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse<ArrayList<NotiItemInfo>>>() {
                    @Override
                    public void onSuccessedCall(BaseResponse<ArrayList<NotiItemInfo>> response) {
                        OeLog.i(TAG, "getNetNotiList() onSuccessedCall enter.");
                        ArrayList<NotiItemInfo> list = response.getData();
                        if (null == list || 0 == list.size()) {
                            OeLog.e(TAG, "getNetNotiList() onSuccessedCall list is empty.");
                            return;
                        }

                        updateAndSaveToDb(list);
                    }

                    @Override
                    public void onFailedCall(BaseResponse<ArrayList<NotiItemInfo>> response) {
                        OeLog.e(TAG, "getNetNotiList() onFailedCall response: " + response.toJson());
                        if (isViewAttached()) {
                            mView.getNetNotiList();
                        }
                    }

                    @Override
                    public void onCompletedCall(BaseResponse<ArrayList<NotiItemInfo>> response) {
                        OeLog.i(TAG, "getNetNotiList() onCompletedCall enter.");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "getNetNotiList() accept enter.");
                    }
                }));
    }

    /**
     * 从本地DB获取未读通知数量
     */
    @Override
    public void getNotiUnReadCount() {
        OeLog.i(TAG, "getNotiUnReadCount() enter.");
        Cursor cursor = ResolverManager.getManager().query(DBInfo.COMMUNITY_NOTI_INFO_URI
                , new String[]{DBInfo.NotiColumns.COMMUNITY_NOTI_READ_STATE}
                , DBInfo.NotiColumns.COMMUNITY_NOTI_READ_STATE + "=?"
                , new String[]{Constants.NOTI_UNREAD + ""}
                , null);

        if (null == cursor) {
            OeLog.e(TAG, "getNotiUnReadCount() cursor is null.");
            return;
        }

        if (isViewAttached()) {
            mView.setNotiUnReadCount(cursor.getCount());
        }
        cursor.close();
    }

    @Override
    public void clearDbData() {
        OeLog.i(TAG, "clearDbData() enter.");
        ResolverManager.getManager().delete(DBInfo.COMMUNITY_NOTI_INFO_URI, null, null);
    }

    private String getQrCodeInfo() {
        OeLog.i(TAG, "getQrCodeInfo() enter.");
        QrCodeInfo qrCodeInfo = new QrCodeInfo(STBUtil.getDeviceMac(), STBUtil.getDeviceSn(), STBUtil.getOperateCode());
        OeLog.i(TAG, "getQrCodeInfo() enter." + qrCodeInfo.toJson());
        return qrCodeInfo.toJson();
    }

    /**
     * 判断从服务器获取的通知是否已经在本地数据库存在，不存在才加到本地数据库
     *
     * @param list 从服务器获取的通知列表
     */
    private void updateAndSaveToDb(ArrayList<NotiItemInfo> list) {
        OeLog.i(TAG, "updateAndSaveToDb() enter.");
        ContentValues values = new ContentValues();
        for (NotiItemInfo info : list) {
            ResolverManager.getManager().updateAndSaveToDb(info, values);
        }
    }

}
