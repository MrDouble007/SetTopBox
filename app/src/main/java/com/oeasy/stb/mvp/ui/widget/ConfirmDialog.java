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
import android.widget.TextView;

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
public class ConfirmDialog extends Dialog implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;
    private Unbinder mUnbinder;
    private String mTitleMsg;
    private String mConfirmMsg;
    private String mCancelMsg;
    private boolean mIsBothIcon;
    private ClickListenerInterface mClickListenerInterface;

    @BindView(R.id.tv_tip_msg)
    TextView mTitle;
    @BindView(R.id.bt_tip_confirm)
    Button mBtConfirm;
    @BindView(R.id.bt_tip_cancel)
    Button mBtCancel;

    /**
     * @param context    上下文
     * @param titleMsg   标题
     * @param confirmMsg 确认的提示
     * @param cancelMsg  取消的提示
     * @param isBothIcon 是否确认和取消两个图标都要
     */
    public ConfirmDialog(@NonNull Context context, String titleMsg, String confirmMsg, String cancelMsg, boolean isBothIcon) {
        super(context);
        this.mContext = context;
        this.mTitleMsg = titleMsg;
        this.mConfirmMsg = confirmMsg;
        this.mCancelMsg = cancelMsg;
        this.mIsBothIcon = isBothIcon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OeLog.i(TAG, "onCreate() enter.");
        // 去除上面黑色边框
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_confirm, null);
        setContentView(view);
        mUnbinder = ButterKnife.bind(this, view);

        if (mIsBothIcon) {
            mBtCancel.setOnClickListener(this);
            mBtCancel.setText(mCancelMsg);
        } else {
            mBtCancel.setVisibility(View.GONE);
        }
        mBtConfirm.setOnClickListener(this);
        mBtConfirm.setText(mConfirmMsg);

        mTitle.setText(mTitleMsg);

    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = QrCodeUtil.dip2px(mContext.getApplicationContext(), 270);
        layoutParams.height = QrCodeUtil.dip2px(mContext.getApplicationContext(), 170);
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

    public void setClickListenerInterface(ClickListenerInterface clickListenerInterface) {
        this.mClickListenerInterface = clickListenerInterface;
    }

    @Override
    public void onClick(View v) {
        OeLog.i(TAG, "onClick() enter.");
        switch (v.getId()) {
            case R.id.bt_tip_confirm:
                OeLog.i(TAG, "onClick() bt_tip_confirm.");
                if (null == mClickListenerInterface) {
                    OeLog.e(TAG, "onClick() mClickListenerInterface is null.");
                    return;
                }

                mClickListenerInterface.doConfirm();
                break;
            case R.id.bt_tip_cancel:
                OeLog.i(TAG, "onClick() bt_tip_cancel.");
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

    public interface ClickListenerInterface {
        void doConfirm();

        void doCancel();
    }
}
