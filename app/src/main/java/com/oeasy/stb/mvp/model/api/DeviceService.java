package com.oeasy.stb.mvp.model.api;

import com.oeasy.stb.app.push.ServerConfig;
import com.oeasy.stb.mvp.model.api.bean.BaseResponse;
import com.oeasy.stb.mvp.model.entity.DeviceStatusInfo;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;

import java.util.ArrayList;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Mr.Double
 * @data 2018/7/31-18:22
 * Description:
 */
public interface DeviceService {

    // 获取机顶盒绑定状态
    @GET("settop/box/info")
    Observable<BaseResponse<DeviceStatusInfo>> getDeviceStatus(@Query("sn") String sn);

    // 获取通知列表
    @GET("padWyService/notice/list")
    Observable<BaseResponse<ArrayList<NotiItemInfo>>> getDeviceNotiList(@Query("unitId") int unitId,
                                                                        @Query("buildingCode") String buildingCode,
                                                                        @Query("roomCode") String roomCode,
                                                                        @Query("pageNo") String pageNo,
                                                                        @Query("pageSize") String pageSize);

    // 获取通知详情
    @GET("padWyService/notice/find/byid")
    Observable<BaseResponse<NotiItemInfo>> getNotiDetail(@Query("unitId") int unitId,
                                                         @Query("id") int notiId);

    // 获取推送账号
    @GET("settop/box/push/account/get")
    Observable<BaseResponse<ServerConfig>> getPushAccount(@Query("sn") String sn);

    // 收到呼叫推送后通知服务器
    @GET("app/visibletalk/call/app/receive")
    Observable<BaseResponse> notifiServerGetIncall(@Query("callId") String callId,
                                                   @Query("callAccount") String callAccount,
                                                   @Query("type") String type);

    // 直接发起拒绝开门指定
    @GET("app/visibletalk/call/app/reject/opendoor")
    Observable<BaseResponse> rejectOpendoor(@Query("callId") String callId,
                                            @Query("callAccount") String callAccount,
                                            @Query("type") String type);

    // 直接发起开门指令
    @GET("app/visibletalk/call/app/opendoor")
    Observable<BaseResponse> opendoor(@Query("callId") String callId,
                                      @Query("callAccount") String callAccount,
                                      @Query("type") String type);

    // 获取门口机名称 需要传入可视对讲房号字符串（7段以_连接的字符串）
    @GET("roomPad/vtname/get")
    Observable<BaseResponse<String>> getCallDoorName(@Query("roomno") String roomno);
}
