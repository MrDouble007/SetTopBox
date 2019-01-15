package com.oeasy.stb.mvp.model.api.bean;

import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * @author Mr.Double
 * @data 2018/7/31-18:28
 * Description:
 */
public class BaseModel implements Serializable {

    public String toJson() {
        return new GsonBuilder().create().toJson(this);
    }
}
