package com.oeasy.stb.mvp.contract;

import android.graphics.Bitmap;

import com.oeasy.stb.mvp.IPresenter;
import com.oeasy.stb.mvp.IView;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;

import java.util.ArrayList;

/**
 * @author Mr.Double
 * @data 2018/7/24-20:09
 * Description:契约类用于定义同一个界面的view和presenter的接口
 */
public interface FirstPageContract {

    interface Presenter extends IPresenter<View> {
        void getQrContent();

        void getDeviceStatus();

        void getDbNotiList();

        void getNetNotiList();

        void getNotiUnReadCount();

        void clearDbData();
    }

    interface View extends IView {
        void showQrCode(Bitmap qrBitmap);

        void showDeviceStatus(boolean isBind, String name, String address);

        void showNotiList(ArrayList<NotiItemInfo> notiInfoList);

        void setNotiUnReadCount(int count);

        void getDeviceStatusLoop();

        void getNetNotiList();
    }

}
