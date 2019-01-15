package com.oeasy.stb.app.push;

import android.text.TextUtils;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class PushData {
    public static final int STATE_UN_READ = 1;    //未读
    public static final int STATE_READED = 2;    //已读
    public static final int STATE_EXECUTED = 3;    //已执行

    private int id;
    private String action;            // 具体要传达的指令
    private String uid;            // 推送到哪个用户
    private String tagsStr;        // 推送到哪个标签
    private int state = 1;            // 推送状态
    private String createBy;        // 谁创建的
    private String createPf;        // 哪个平台创建的
    private Timestamp createTime;    // 创建时间
    private Timestamp effectTime;    // 生效时间
    private Timestamp expireTime;    // 过期时间
    private Timestamp readTime;    // 读取时间
    private Timestamp executeTime;    // 执行时间
    private Set<String> tags = new HashSet<>();

    public Timestamp getReadTime() {
        return readTime;
    }

    public void setReadTime(Timestamp readTime) {
        this.readTime = readTime;
    }

    public Timestamp getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Timestamp executeTime) {
        this.executeTime = executeTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(Timestamp effectTime) {
        this.effectTime = effectTime;
    }

    public Timestamp getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Timestamp expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isTag() {
        return !TextUtils.isEmpty(tagsStr);
    }

    public String getTagsStr() {
        return tagsStr;
    }

    public void setTagsStr(String tagsStr) {
        this.tagsStr = tagsStr;
    }

    public Set<String> getTags() {
        if (tags.isEmpty() && !TextUtils.isEmpty(tagsStr)) {
            String[] _tags = tagsStr.split(",");
            for (int i = 0; i < _tags.length; i++) {
                tags.add(_tags[i]);
            }
        }
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getCreatePf() {
        return createPf;
    }

    public void setCreatePf(String createPf) {
        this.createPf = createPf;
    }

    /**
     * 是否已经过期
     *
     * @param compareTime
     * @return
     */
    public boolean isExpire(long compareTime) {
        // 过期时间小于比较时间，说明已经过期了
        return this.expireTime.getTime() < compareTime;
    }

    /**
     * 是否已经生效
     *
     * @return
     */
    public boolean isEffect(long compareTime) {
        //生效时间大于等于比较时间，说明已经生效
        return this.effectTime.getTime() >= compareTime;
    }

    public static PushData fromJson(JSONObject json) {
        PushData data = new PushData();
        data.setAction(json.optString("action"));
        data.setUid(json.optString("uid"));
        data.setTagsStr(json.optString("tagsStr", null));
        data.setId(json.optInt("id"));
        try {
            Object eftObj = json.opt("effectTime");
            if (eftObj instanceof Timestamp) {
                data.setEffectTime((Timestamp) eftObj);
            } else if (eftObj != null) {
                String[] timeValue = eftObj.toString().split(".");
                if (timeValue.length > 0) {
                    data.setEffectTime(Timestamp.valueOf(timeValue[0]));
                } else {
                    data.setEffectTime(Timestamp.valueOf(eftObj.toString()));
                }
            }
        } catch (Exception pe) {
            pe.printStackTrace();
        }
        try {
            Object extObj = json.opt("expireTime");
            if (extObj instanceof Timestamp) {
                data.setExpireTime((Timestamp) extObj);
            } else if (extObj != null) {
                String[] timeValue = extObj.toString().split(".");
                if (timeValue.length > 0) {
                    data.setExpireTime(Timestamp.valueOf(timeValue[0]));
                } else {
                    data.setEffectTime(Timestamp.valueOf(extObj.toString()));
                }
            }
        } catch (Exception pe) {
            pe.printStackTrace();
        }
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PushData) {
            return this.id == ((PushData) obj).id;
        }
        return false;
    }

    @Override
    public String toString() {
        return "PushData{" +
                "effectTime=" + effectTime +
                '}';
    }
}
