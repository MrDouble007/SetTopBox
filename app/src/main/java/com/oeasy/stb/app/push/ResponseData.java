package com.oeasy.stb.app.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResponseData implements Serializable {
    private static final long serialVersionUID = -7390197122586456496L;
    private int rId; // 客户端请求过来的id
    private int code; // 状态码,参考ResponseCode
    private Map<String, Object> data;// 返回的数据

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getrId() {
        return rId;
    }

    public void setrId(int rId) {
        this.rId = rId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public static ResponseData fromJson(JSONObject json) {
        ResponseData response = new ResponseData();
        response.rId = json.optInt("rId");
        response.code = json.optInt("code");
        JSONObject jsonData = json.optJSONObject("data");
        if (jsonData != null) {
            response.data = new HashMap<>();
            Iterator<String> keys = jsonData.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                response.data.put(key, jsonData.opt(key));
            }
        }
        return response;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("rId", rId);
            json.put("code", code);
            json.put("data", new JSONObject(data));
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return json;
    }

}
