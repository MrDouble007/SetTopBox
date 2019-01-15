package com.oeasy.stb.mvp.model.entity;

import com.oeasy.stb.mvp.model.api.bean.BaseModel;

/**
 * @author Mr.Double
 * @data 2018/7/31-15:40
 * Description:
 */
public class QrCodeInfo extends BaseModel {
    private String mac;
    private String sn;
    private String operateCode;

    public QrCodeInfo(String mac, String sn, String operateCode) {
        this.mac = mac;
        this.sn = sn;
        this.operateCode = operateCode;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getOperateCode() {
        return operateCode;
    }

    public void setOperateCode(String operateCode) {
        this.operateCode = operateCode;
    }
}
