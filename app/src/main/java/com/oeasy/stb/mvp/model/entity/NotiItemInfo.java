package com.oeasy.stb.mvp.model.entity;

import com.oeasy.stb.mvp.model.api.bean.BaseModel;

import java.util.ArrayList;

/**
 * @author Mr.Double
 * @data 2018/8/1-18:28
 * Description:
 */
public class NotiItemInfo extends BaseModel {
    private int id; // 通知唯一id
    private int areaid; // 小区id
    private int readCount; // 阅读数量
    private String unitName; // 小区名称
    private String buildingCode; // 楼栋编码
    private String roomCode; // 房屋编码
    private String title;
    private String content;
    private String note;
    private long createDate; // 发布时间
    private long noticeBeginTime; // 公告开始时间
    private long noticeEndTime; // 公告结束时间
    private int isView; // 是否已读，0：未读，1：已读
    private ArrayList<String> imageList; // 有效图片路径集合

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAreaid() {
        return areaid;
    }

    public void setAreaid(int areaid) {
        this.areaid = areaid;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
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

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getNoticeBeginTime() {
        return noticeBeginTime;
    }

    public void setNoticeBeginTime(long noticeBeginTime) {
        this.noticeBeginTime = noticeBeginTime;
    }

    public long getNoticeEndTime() {
        return noticeEndTime;
    }

    public void setNoticeEndTime(long noticeEndTime) {
        this.noticeEndTime = noticeEndTime;
    }

    public int getIsView() {
        return isView;
    }

    public void setIsView(int isView) {
        this.isView = isView;
    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
