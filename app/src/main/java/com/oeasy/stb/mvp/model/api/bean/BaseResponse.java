package com.oeasy.stb.mvp.model.api.bean;

import android.support.annotation.Keep;

/**
 * @author Mr.Double
 * @data 2018/7/31-18:28
 * Description:
 */
@Keep
public class BaseResponse<T> extends BaseModel {
    private T data;
    private String code;
    private String desc;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSuccess() {
        if (code != null && code.equals("200")) {
            return true;
        } else {
            return false;
        }
    }
}
