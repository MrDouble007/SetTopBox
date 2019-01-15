package com.oeasy.stb.mvp.model.api.bean;

import io.reactivex.functions.Consumer;

/**
 * @author Mr.Double
 * @data 2018/7/31-20:02
 * Description: 统一处理所有请求的状态过滤
 */
public abstract class BaseHttpConsumer<T> implements Consumer<T> {

    private static final String HTTP_STATUS_OK = "200";

    @Override
    public void accept(T response) throws Exception {
        if (null == response) {
            onFailedCall(response);
        } else if (response instanceof BaseResponse) {
            BaseResponse baseResponse = (BaseResponse) response;

            if (null != baseResponse.getCode() && baseResponse.getCode().equals(HTTP_STATUS_OK)) {
                onSuccessedCall(response);
            } else {
                onFailedCall(response);
            }
        }
        onCompletedCall(response);
    }

    /**
     * 状态成功时会走向这里
     */
    public abstract void onSuccessedCall(T response);

    /**
     * 状态非成功时会走向这里，
     */
    public abstract void onFailedCall(T response);

    //无论成功或失败都会走向这里，可用于统一处理失败和成功都需要处理的事件
    public void onCompletedCall(T response) {

    }

}
