package com.oeasy.stb.app.push;

/**
 * Created by chenyongchao on 2017/2/14.
 * 登录检查器
 */

public class LoginChecker {
    static final int STATUS_WAIT = 0;   //等待登录
    static final int STATUS_LOGGING = 1; //登录中
    static final int STATUS_SUCCESS = 2;    //登录成功
    static final int STATUS_FAIL = 3;       //登录失败

    long requestTime;
    long responseTime;
    int status = STATUS_WAIT;
    int rid;

    public long getRequestTime() {
        return requestTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public int getStatus() {
        return status;
    }

    public int getRid() {
        return rid;
    }
}
