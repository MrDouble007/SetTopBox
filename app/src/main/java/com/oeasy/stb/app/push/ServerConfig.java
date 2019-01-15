package com.oeasy.stb.app.push;

import com.oeasy.stb.mvp.model.api.bean.BaseModel;

import java.util.List;

public class ServerConfig extends BaseModel {

    //访问后台的域,可以是ip和域名
    private String host;
    //访问后台的端口
    private int port;
    //连接超时时间（毫秒)
    private long connectTimeout = 3000;
    // 长连接心跳包发送频率（秒）
    private int keepAliveTimeInterval = 4 * 60;
    //长连接心跳包应答超时（秒）
    private int keepAliveResponseTimeout = 3;
    //断开连接后重联时隔（毫秒）
    private long reconnectInterval = 1000;
    //设备唯一ID
    private String id;
    //在服务端分配的token
    private String token;
    //在服务端分配的私钥（与token一样,这两项是不可公开访问的）
    private String privateKey;
    //登录超时（毫秒）
    private long loginTimeout = 8000;
    //用户类型：1=管理平台 2=终端设备
    private int type;
    //设备标签（字符串数组）
    private List tags;

    public long getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(long loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getKeepAliveTimeInterval() {
        return keepAliveTimeInterval;
    }

    public void setKeepAliveTimeInterval(int keepAliveTimeInterval) {
        this.keepAliveTimeInterval = keepAliveTimeInterval;
    }

    public int getKeepAliveResponseTimeout() {
        return keepAliveResponseTimeout;
    }

    public void setKeepAliveResponseTimeout(int keepAliveResponseTimeout) {
        this.keepAliveResponseTimeout = keepAliveResponseTimeout;
    }

    public long getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(long reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List getTags() {
        return tags;
    }

    public void setTags(List tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{hostName:").append(host)
                .append(",port:").append(port)
                .append(",connectTimeout:").append(connectTimeout)
                .append(",keepAliveTimeInterval:").append(keepAliveTimeInterval)
                .append(",keepAliveResponseTimeout:").append(keepAliveResponseTimeout)
                .append(",reconnectInterval:").append(reconnectInterval)
                .append(",deviceid:").append(id)
                .append(",token:").append(token)
                .append(",privateKey:").append(privateKey).append("}");
        return sb.toString();
    }

    public String getToken() {
        try {
            return CommonTool.encryptHMAC(token, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
