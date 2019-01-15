package com.oeasy.stb.mvp.model.entity;

import com.oeasy.stb.mvp.model.api.bean.BaseModel;

/**
 * @author Mr.Double
 * @data 2018/8/13-13:37
 * Description:
 */
public class IncallPushInfo extends BaseModel {
    private int cmd; // 呼叫命令；1=对讲申请呼叫；2=图片更新；3=对讲挂断；4=呼叫被人接听
    private String callId; // 呼叫编码
    private String sipAccount; // sip账号
    private String roomCode; // roomCode
    private String image; // 门口机抓拍图片，为空显示默认图片
    private String title; // 消息标题
    private String content; // 消息内容
    private String typeC; // 消息类型

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getSipAccount() {
        return sipAccount;
    }

    public void setSipAccount(String sipAccount) {
        this.sipAccount = sipAccount;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTypeC() {
        return typeC;
    }

    public void setTypeC(String typeC) {
        this.typeC = typeC;
    }
}
