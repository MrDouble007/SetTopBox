package com.oeasy.stb.mvp.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oeasy.stb.R;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.QrCodeUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Mr.Double
 * @data 2018/7/26-9:50
 * Description:
 */
public class InCallDialog extends Dialog implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private Unbinder mUnbinder;
    private String mAddress;
    private ClickListenerInterface mClickListenerInterface;

    @BindView(R.id.tv_incall_address)
    TextView mInCallAdd;
    @BindView(R.id.iv_incall_picture)
    ImageView mIncallPicture;
    @BindView(R.id.bt_incall_confirm)
    Button mBtConfirm;
    @BindView(R.id.bt_incall_cancel)
    Button mBtCancel;

    /**
     * @param context 上下文
     * @param address 门口机坐标
     */
    public InCallDialog(@NonNull Context context, String address) {
        super(context);
        this.mContext = context;
        this.mAddress = address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OeLog.i(TAG, "onCreate() enter.");
        // 去除上面黑色边框
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_incall, null);
        setContentView(view);
        mUnbinder = ButterKnife.bind(this, view);

        mInCallAdd.setText(mAddress);
        mBtCancel.setOnClickListener(this);
        mBtConfirm.setOnClickListener(this);
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度或者位置，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        if (null == mContext) {
            OeLog.i(TAG, "show() mContext is null.");
            return;
        }
        layoutParams.width = QrCodeUtil.dip2px(mContext.getApplicationContext(), 450);
        getWindow().setAttributes(layoutParams);
    }

    public void setClickListenerInterface(ClickListenerInterface clickListenerInterface) {
        this.mClickListenerInterface = clickListenerInterface;
    }

    public void setPicUrl(String picUrl) {
        OeLog.i(TAG, "setPicUrl() enter.");
        if (null == mContext) {
            OeLog.e(TAG, "setPicUrl() mContext is null.");
            return;
        }

        Glide.with(mContext)
                .load(picUrl)
                .into(mIncallPicture);
    }

    @Override
    public void onClick(View v) {
        OeLog.i(TAG, "onClick() enter.");
        switch (v.getId()) {
            case R.id.bt_incall_confirm:
                OeLog.i(TAG, "onClick() bt_incall_confirm.");
                if (null == mClickListenerInterface) {
                    OeLog.e(TAG, "onClick() mClickListenerInterface is null.");
                    return;
                }

                mClickListenerInterface.doConfirm();
                break;
            case R.id.bt_incall_cancel:
                OeLog.i(TAG, "onClick() bt_incall_cancel.");
                if (null == mClickListenerInterface) {
                    OeLog.e(TAG, "onClick() mClickListenerInterface is null.");
                    return;
                }

                mClickListenerInterface.doCancel();
                break;
            default:
                OeLog.e(TAG, "onClick() invalid id.");
                break;
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

    @Override
    public void onBackPressed() {
        OeLog.i(TAG, "onBackPressed() enter.");
        if (null == mClickListenerInterface) {
            OeLog.e(TAG, "onBackPressed() mClickListenerInterface is null.");
            return;
        }

        mClickListenerInterface.doCancel();
        super.onBackPressed();
    }

    public interface ClickListenerInterface {
        void doConfirm();

        void doCancel();
    }
}
