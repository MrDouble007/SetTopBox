package com.oeasy.stb.utils;

/**
 * @author Mr.Double
 * @data 2018/7/31-18:37
 * Description:
 */
public class Constants {

    public static final String URL_BASE_URL_DEVELOP = "https://devone.0easy.com/";
    public static final String URL_BASE_URL_PRODUCT = "https://nsapp.0easy.com/";
    public static final String URL_BASE_URL_TEST = "https://testapp.0easy.com/";
    public static final String URL_BASE_URL_PREPARE = "https://presapp.0easy.com/";
    public static final String URL_DEVICE_BASE_URL_DEVELOP = URL_BASE_URL_DEVELOP + "yihao01-switch-api/";
    public static final String URL_DEVICE_BASE_URL_PRODUCT = URL_BASE_URL_PRODUCT + "yihao01-switch-api/";
    public static final String URL_DEVICE_BASE_URL_TEST = URL_BASE_URL_TEST + "yihao01-switch-api/";
    public static final String URL_DEVICE_BASE_URL_PREPARE = URL_BASE_URL_PREPARE + "yihao01-switch-api/";

    public static final String TID_ANDROID = "1";
    public static final String ACTION_STBSERVICE = "com.oeasy.stb.action.stbservice";

    public static final int CHUANGWEI_KEY_CODE_UP = 19;
    public static final int CHUANGWEI_KEY_CODE_DOWN = 20;
    public static final int CHUANGWEI_KEY_CODE_LEFT = 21;
    public static final int CHUANGWEI_KEY_CODE_RIGHT = 22;
    public static final int CHUANGWEI_KEY_CODE_CONFIRM = 23;
    public static final int CHUANGWEI_KEY_CODE_BACK = 4;

    public static final String KEY_NOTI_ITEM_DETAIL = "noti_item_detail";
    public static final String KEY_NOTI_LIST_INFO = "noti_list_info";
    public static final String KEY_NOTI_UNREAD_COUNT = "noti_unread_count"; // 未读通知数量

    public static final int RESULTCODE_OK = 200;
    public static final int NOTI_READ = 1;
    public static final int NOTI_UNREAD = 0;

    // 推送中心相关
    public static final int STATE_EFFECTIVITY = 1;    //未读
    public static final int STATE_READ = 2;        //已读
    public static final int STATE_EXECUTED = 3;        //已执行
    public static final int COMMAND_UPDATE_PUSH_STATE = 3; //更新推送信息状态
    public static final String TYPE_STB = "6"; //告知服务器收到推送的类型，机顶盒固定为6
    public static final String PUSH_TYPE_NOTICE = "1000"; // 收到小区通知
    public static final String PUSH_TYPE_INCALL = "4517"; // 收到小区呼叫 呼叫命令；1=对讲申请呼叫；2=图片更新；3=对讲挂断; 4=呼叫被接听
    public static final String ACTION_BIND_DEVICE = "action.bind.device"; // 收到绑定或解绑小区通知
    public static final String ACTION_UPDATE_UI = "action.update.ui"; // 根据网络状态更新UI
    public static final String KEY_BIND = "key_bind";
    public static final String KEY_UPDATE_UI = "key_update_ui";
    public static final String VALUE_BIND_ROOM = "bind_room";
    public static final String VALUE_UNWRAP_ROOM = "unwrap_room";
    public static final String VALUE_PUSH_NOTICE = "push_notice";
}
