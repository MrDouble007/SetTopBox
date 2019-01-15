package com.oeasy.stb.mvp;

/**
 * @author Mr.Double
 * @data 2018/7/24-19:07
 * Description:要求框架中的每个 View 都需要实现此类,以满足规范
 */
public interface IView {
    /**
     * 显示加载
     */
    void showLoading();

    /**
     * 隐藏加载
     */
    void hideLoading();
}
