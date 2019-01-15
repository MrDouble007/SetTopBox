package com.oeasy.stb.mvp.model.api.bean;

/**
 * Desc:
 * Created by cjb on 2017/7/11.
 */

public class BasePushAction extends BaseModel {
    private String msgType;
    private String cmd;
    private long bindTime;
    private long unwrapTime;
    private Object noticeInfo;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public long getBindTime() {
        return bindTime;
    }

    public void setBindTime(long bindTime) {
        this.bindTime = bindTime;
    }

    public long getUnwrapTime() {
        return unwrapTime;
    }

    public void setUnwrapTime(long unwrapTime) {
        this.unwrapTime = unwrapTime;
    }

    public Object getNoticeInfo() {
        return noticeInfo;
    }

    public void setNoticeInfo(Object noticeInfo) {
        this.noticeInfo = noticeInfo;
    }
}
