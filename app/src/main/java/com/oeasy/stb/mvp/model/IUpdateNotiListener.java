package com.oeasy.stb.mvp.model;

import com.oeasy.stb.mvp.model.entity.NotiItemInfo;

/**
 * @author Mr.Double
 * @data 2018/8/10-13:48
 * Description: 与通知相关的UI监听接口，主要两个地方：1、主界面新增未读 2、通知列表界面新增新通知
 */
public interface IUpdateNotiListener {
    void updateNoti(NotiItemInfo info);
}
