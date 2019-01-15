package com.oeasy.stb.mvp.ui.activity;


import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.oeasy.stb.R;
import com.oeasy.stb.mvp.IView;
import com.oeasy.stb.mvp.base.BaseActivity;
import com.oeasy.stb.mvp.model.IUpdateNotiListener;
import com.oeasy.stb.mvp.model.api.ApiFactory;
import com.oeasy.stb.mvp.model.api.RetryWithDelay;
import com.oeasy.stb.mvp.model.api.bean.BaseHttpConsumer;
import com.oeasy.stb.mvp.model.api.bean.BaseResponse;
import com.oeasy.stb.mvp.model.database.DBInfo;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.mvp.model.manager.ResolverManager;
import com.oeasy.stb.mvp.ui.adapter.NotiListAdapter;
import com.oeasy.stb.mvp.ui.widget.CommonLoadingDialog;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.SpfUtil;

import java.util.ArrayList;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Mr.Double
 * @data 2018/8/7-10:32
 * Description:
 */
public class NotiListActivity extends BaseActivity implements IView, AdapterView.OnItemClickListener, IUpdateNotiListener {
    private final String TAG = this.getClass().getSimpleName();

    @BindView(R.id.lv_device_noti)
    ListView mNotiList;

    private NotiListAdapter mListAdapter;
    private ArrayList<NotiItemInfo> mNotiInfoList;
    private CompositeDisposable mCompositeDisposable;
    private CommonLoadingDialog mLoadingDialog;
    private int mUnReadCount; // 未读通知数量

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_noti_list;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (null == intent) {
            OeLog.e(TAG, "initData() intent is null.");
            return;
        }
        // 注册推送监听
        ResolverManager.getManager().registUpdateNotiListener(this);
        mNotiInfoList = (ArrayList<NotiItemInfo>) intent.getSerializableExtra(Constants.KEY_NOTI_LIST_INFO);
        mUnReadCount = intent.getIntExtra(Constants.KEY_NOTI_UNREAD_COUNT, 0);
        mListAdapter = new NotiListAdapter(this, mNotiInfoList);
        mNotiList.setAdapter(mListAdapter);
        mNotiList.setOnItemClickListener(this);
        initLoadingDialog();
        mCompositeDisposable = new CompositeDisposable();
    }

    private void initLoadingDialog() {
        OeLog.i(TAG, "initLoadingDialog() enter.");
        mLoadingDialog = new CommonLoadingDialog(this, R.style.CommonDialogTheme, getString(R.string.loading_tip_common));
        mLoadingDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void showLoading() {
        OeLog.i(TAG, "showLoading() enter.");
        if (mLoadingDialog.isShowing()) {
            return;
        }
        mLoadingDialog.show();
    }

    @Override
    public void hideLoading() {
        OeLog.i(TAG, "hideLoading() enter.");
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        OeLog.i(TAG, "onDestroy() enter.");

        if (null != mLoadingDialog) {
            mLoadingDialog = null;
        }

        if (null != mCompositeDisposable) {
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OeLog.i(TAG, "onItemClick() enter.");
        TextView notiTitle = view.findViewById(R.id.tv_noti_title);
        if (null != notiTitle) {
            // 点击已读取消红点,更新数据到数据库
            NotiItemInfo info = mNotiInfoList.get(position);
            if (Constants.NOTI_UNREAD == info.getIsView()) {
                mUnReadCount--;
                info.setIsView(Constants.NOTI_READ);
                notiTitle.setCompoundDrawables(null, null, null, null);

                ContentValues values = new ContentValues();
                values.put(DBInfo.NotiColumns.COMMUNITY_NOTI_READ_STATE, Constants.NOTI_READ);
                values.put(DBInfo.NotiColumns.COMMUNITY_NOTI_INFO, info.toJson());
                ResolverManager.getManager().update(DBInfo.COMMUNITY_NOTI_INFO_URI, values, DBInfo.NotiColumns.COMMUNITY_NOTI_ID + "=?",
                        new String[]{info.getId() + ""});
            } else {
                OeLog.i(TAG, "onItemClick() info is already read.");
            }
        } else {
            OeLog.e(TAG, "onItemClick() notiTitle is null.");
        }

        getNotiDetail(mNotiInfoList.get(position).getId());
    }

    @Override
    public void onBackPressed() {
        OeLog.i(TAG, "onBackPressed() enter, mUnReadCount: " + mUnReadCount);
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY_NOTI_UNREAD_COUNT, mUnReadCount);
        setResult(Constants.RESULTCODE_OK, intent);
        this.finish();
        super.onBackPressed();
    }

    private void startItemDetailActivity(NotiItemInfo notiItemInfo) {
        OeLog.i(TAG, "startItemDetailActivity() enter.");
        if (null == notiItemInfo) {
            OeLog.e(TAG, "startItemDetailActivity() notiItemInfo is null.");
            return;
        }

        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(Constants.KEY_NOTI_ITEM_DETAIL, notiItemInfo);
        startActivity(intent);
    }

    @Override
    public void updateNoti(NotiItemInfo info) {
        OeLog.i(TAG, "updateNoti() enter.");
        // 新通知未读+1
        mUnReadCount++;
        mListAdapter.addItem(info);
        mListAdapter.notifyDataSetChanged();
    }

    private void getNotiDetail(int notiId) {
        OeLog.i(TAG, "getNotiDetail() enter.");
        showLoading();
        mCompositeDisposable.add(ApiFactory.getDeviceService().getNotiDetail(SpfUtil.getUnitId(), notiId)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 3))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpConsumer<BaseResponse<NotiItemInfo>>() {
                    @Override
                    public void onSuccessedCall(BaseResponse<NotiItemInfo> response) {
                        OeLog.i(TAG, "getNotiDetail() onSuccessedCall.");
                        NotiItemInfo info = response.getData();
                        if (null == info) {
                            OeLog.i(TAG, "getNotiDetail() onSuccessedCall, info is null.");
                            return;
                        }
                        startItemDetailActivity(info);
                    }

                    @Override
                    public void onFailedCall(BaseResponse<NotiItemInfo> response) {
                        OeLog.e(TAG, "getNotiDetail() onFailedCall: " + response.toJson());
                    }

                    @Override
                    public void onCompletedCall(BaseResponse<NotiItemInfo> response) {
                        OeLog.i(TAG, "getNotiDetail() onCompletedCall.");
                        hideLoading();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        OeLog.e(TAG, "getNotiDetail() accept throwable: " + throwable.getMessage());
                    }
                }));
    }
}
