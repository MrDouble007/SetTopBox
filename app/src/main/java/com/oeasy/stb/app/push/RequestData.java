package com.oeasy.stb.app.push;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

public class RequestData implements Serializable {
    private static final long serialVersionUID = -2656887953706499817L;
    private int command;                //请求是做什么的
    private int rId;                    //请求时的id
    private Map<String, Object> param;    //请求参数

    public int getrId() {
        return rId;
    }

    public void setrId(int rId) {
        this.rId = rId;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    public static RequestData fromJson(JSONObject json) {
        RequestData request = new RequestData();
        request.rId = json.optInt("rId");
        request.command = json.optInt("command");
        JSONObject jsonParam = json.optJSONObject("param");
        if (jsonParam != null) {
            Iterator<String> keys = jsonParam.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                request.param.put(key, jsonParam.opt(key));
            }
        }
        return request;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("rId", rId);
            json.put("command", command);
            JSONObject jsonParam = new JSONObject();
            for (String key : param.keySet()) {
                Object obj = param.get(key);
                if (obj instanceof Timestamp) {
                    obj = CommonTool.timestampToString((Timestamp) obj);
                }
                jsonParam.put(key, obj);
            }
            json.put("param", jsonParam);
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return json;
    }

}
