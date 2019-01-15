package com.oeasy.stb.mvp.model.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Mr.Double
 * @data 2018/8/3-17:51
 * Description:
 */
public class DBInfo {

    public static final String AUTHORITIES = "com.oeasy.stb.data";
    public static final String NOTI_PATH = "noti";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITIES);
    public static final Uri COMMUNITY_NOTI_INFO_URI = Uri.withAppendedPath(BASE_URI, NOTI_PATH);

    public static class NotiColumns implements BaseColumns {
        public static final String COMMUNITY_NOTI_INFO = "noti_info";
        public static final String COMMUNITY_NOTI_ID = "noti_id";
        public static final String COMMUNITY_NOTI_READ_STATE = "noti_read_state";// 已读未读状态 0：未读 1：已读
        public static final String COMMUNITY_NOTI_TIME = "time";// 通知时间
    }


}
