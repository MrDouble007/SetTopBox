package com.oeasy.stb.mvp.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.oeasy.stb.R;
import com.oeasy.stb.utils.OeLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Mr.Double
 * @data 2018/7/30-17:44
 * Description: 自定义通用加载dialog
 */
public class CommonLoadingDialog extends Dialog {
    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private Unbinder mUnbinder;
    private String mLoadingMsg;

    @BindView(R.id.tv_loading)
    TextView mLoadingTv;

    /**
     * @param context    上下文
     * @param themeResId dialog的主题
     * @param loadingMsg 加载的提示语
     */
    public CommonLoadingDialog(@NonNull Context context, int themeResId, String loadingMsg) {
        super(context, themeResId);
        this.mContext = context;
        this.mLoadingMsg = loadingMsg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_common_loading, null);
        setContentView(view);
        mUnbinder = ButterKnife.bind(this, view);

        if (TextUtils.isEmpty(mLoadingMsg)) {
            mLoadingTv.setVisibility(View.GONE);
        } else {
            mLoadingTv.setText(mLoadingMsg);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        OeLog.i(TAG, "dismiss() enter.");
        if (mUnbinder != null && mUnbinder != Unbinder.EMPTY) mUnbinder.unbind();
        mUnbinder = null;

        if (null != mContext) {
            mContext = null;
        }
    }
}
