package com.oeasy.stb.mvp.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TabWidget;

import com.oeasy.stb.R;

/**
 * Created by wp.nine on 2015/10/19.
 */
public class DefaultBadgeView extends BadgeView {
    public static final int TYPE_DIGITAL = 0;  //显示数字
    public static final int TYPE_ONLY_POINT = 1;//只显示红点
    private int mType;

    public DefaultBadgeView(Context context) {
        super(context);
        init();
    }

    public DefaultBadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DefaultBadgeView(Context context, View target) {
        super(context, target);
        init();
    }

    public DefaultBadgeView(Context context, View target, int type) {
        super(context, target);
        mType = type;
        init();
    }

    public DefaultBadgeView(Context context, TabWidget target, int index) {
        super(context, target, index);
        init();
    }

    public DefaultBadgeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public DefaultBadgeView(Context context, AttributeSet attrs, int defStyle, View target, int tabIndex) {
        super(context, attrs, defStyle, target, tabIndex);
        init();
    }

    //统一BadgeView的样式
    private void init() {
        if (mType == TYPE_DIGITAL) {
            int badgeTextSize = getResources().getDimensionPixelSize(R.dimen.space10);
            int badgePadding = (int) (badgeTextSize / 2.5f);
            this.setBadgeBackgroundColor(Color.RED);
            this.setPadding(badgePadding, 0, badgePadding, 0);
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, badgeTextSize);
            this.setBadgeMargin(0);
        } else {
            int badgeTextSize = getResources().getDimensionPixelSize(R.dimen.space4);
            int badgePadding = (int) (badgeTextSize / 1.5);
            this.setBadgeBackgroundColor(Color.rgb(255, 115, 57));
            this.setPadding(badgePadding, 0, badgePadding, 0);
            this.setTextSize(TypedValue.COMPLEX_UNIT_PX, badgeTextSize);
            this.setBadgeMargin(0);

        }

    }

    public void setCount(int count) {
        if (count == 0) {
            this.hide();
        } else {
            this.show();
            if (mType == TYPE_DIGITAL) {
                if (count <= 99) {
                    setText(String.valueOf(count));
                } else {
                    setText("99+");
                }

            } else {
                setText(" ");
            }

        }
    }

}
