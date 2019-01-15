package com.oeasy.stb.mvp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oeasy.stb.R;
import com.oeasy.stb.mvp.base.BaseActivity;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.QrCodeUtil;
import com.oeasy.stb.utils.STBUtil;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * @author Mr.Double
 * @data 2018/8/3-14:25
 * Description: 通知list子Item点击进入的界面
 */
public class ItemDetailActivity extends BaseActivity {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.tv_detail_title)
    TextView mDetailTitle;
    @BindView(R.id.tv_detail_content)
    TextView mDetailContent;
    @BindView(R.id.tv_detail_time)
    TextView mDetailTime;
    @BindView(R.id.ll_detail_layout)
    LinearLayout mllView;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_item_detial;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (null == intent) {
            OeLog.e(TAG, "initData() intent is null.");
            finish();
            return;
        }

        showDetailInfo(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        OeLog.i(TAG, "onNewIntent() enter.");
        if (null == intent) {
            OeLog.e(TAG, "onNewIntent() intent is null.");
            finish();
            return;
        }

        setIntent(intent);
        showDetailInfo(intent);
    }

    private void showDetailInfo(Intent intent) {
        OeLog.i(TAG, "showDetailInfo() enter.");
        NotiItemInfo notiItemInfo = (NotiItemInfo) intent.getSerializableExtra(Constants.KEY_NOTI_ITEM_DETAIL);
        if (null == notiItemInfo) {
            OeLog.e(TAG, "showDetailInfo() notiItemInfo is null.");
            finish();
            return;
        }

        OeLog.i(TAG, "showDetailInfo() notiItemInfo: " + notiItemInfo.toJson());
        mDetailTitle.setText(notiItemInfo.getTitle());
        mDetailContent.setText(getString(R.string.notice_detail_content, notiItemInfo.getContent()));
        mDetailTime.setText(STBUtil.getDateByLongByPattern(notiItemInfo.getNoticeBeginTime(), "yyyy-MM-dd  HH:mm"));
        ArrayList<String> array = notiItemInfo.getImageList();
        if (null == array || 0 == array.size()) {
            OeLog.e(TAG, "showDetailInfo() array is null.");
            return;
        }
        showPic(array);
    }

    private void showPic(ArrayList<String> array) {
        OeLog.i(TAG, "showPic() enter, size: " + array.size());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = QrCodeUtil.dip2px(this.getApplicationContext(), 10);
        for (int i = 0; i < array.size(); i++) {
            ImageView imageView = new ImageView(this);
            Glide.with(this)
                    .load(array.get(i))
                    .into(imageView);
            mllView.addView(imageView, 2 + i, params);
        }
    }
}
