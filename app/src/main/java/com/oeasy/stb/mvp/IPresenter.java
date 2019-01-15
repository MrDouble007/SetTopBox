package com.oeasy.stb.mvp;

/**
 * @author Mr.Double
 * @data 2018/7/24-19:07
 * Description:要求框架中的每个 Presenter 都需要实现此类,以满足规范
 */
public interface IPresenter<V extends IView> {
    /**
     * 绑定view，一般在初始化中调用该方法
     */
    void attachView(V view);

    /**
     * 断开view，一般在onDestroy中调用
     */
    void detachView();

    /**
     * 是否与View建立连接
     * 每次调用业务请求的时候都要出先调用方法检查是否与View建立连接
     */
    boolean isViewAttached();
}
