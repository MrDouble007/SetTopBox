package com.oeasy.stb.mvp.model.entity;

import com.oeasy.stb.mvp.model.api.bean.BaseModel;

/**
 * @author Mr.Double
 * @data 2018/8/8-16:56
 * Description: 获取到的绑定设备的信息
 */
public class DeviceStatusInfo extends BaseModel {
    private int unitId; // 小区编码
    private String buildingCode; // 楼栋编码
    private String roomCode; // 房屋编码
    private String address; // 机顶盒绑定的房屋地址
    private String ownerName; // 业主姓名

    public DeviceStatusInfo(int unitId, String buildingCode, String roomCode, String address, String ownerName) {
        this.unitId = unitId;
        this.buildingCode = buildingCode;
        this.roomCode = roomCode;
        this.address = address;
        this.ownerName = ownerName;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
