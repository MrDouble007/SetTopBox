package com.oeasy.stb.mvp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.oeasy.stb.R;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;
import com.oeasy.stb.utils.STBUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Mr.Double
 * @data 2018/8/1-18:44
 * Description:通知模块的适配器
 */
public class NotiListAdapter extends BaseAdapter {
    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private ArrayList<NotiItemInfo> mNotiList;

    public NotiListAdapter(Context context, ArrayList<NotiItemInfo> notiList) {
        this.mContext = context;
        this.mNotiList = notiList;
    }

    @Override
    public int getCount() {
        return mNotiList.size();
    }

    @Override
    public Object getItem(int position) {
        return mNotiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(NotiItemInfo info) {
        mNotiList.add(0, info);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_notification, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        NotiItemInfo itemInfo = mNotiList.get(position);
        if (null != itemInfo) {
            viewHolder.mTvNotiTitle.setText(itemInfo.getTitle());
            viewHolder.mTvNotiContent.setText(itemInfo.getContent());
            viewHolder.mTvNotiTime.setText(STBUtil.getDateByLongByPattern(itemInfo.getNoticeBeginTime(), "yyyy-MM-dd  HH:mm"));

            if (Constants.NOTI_READ == itemInfo.getIsView()) {
                viewHolder.mTvNotiTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else {
                viewHolder.mTvNotiTitle.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.circular_shape_red), null, null, null);
            }
        } else {
            OeLog.e(TAG, "getView() itemInfo is null.");
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_noti_title)
        TextView mTvNotiTitle;
        @BindView(R.id.tv_noti_content)
        TextView mTvNotiContent;
        @BindView(R.id.tv_noti_time)
        TextView mTvNotiTime;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
