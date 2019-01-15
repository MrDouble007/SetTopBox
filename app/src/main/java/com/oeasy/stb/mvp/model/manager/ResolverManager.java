package com.oeasy.stb.mvp.model.manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.oeasy.stb.mvp.base.BaseApplication;
import com.oeasy.stb.mvp.model.IUpdateNotiListener;
import com.oeasy.stb.mvp.model.database.DBInfo;
import com.oeasy.stb.mvp.model.entity.NotiItemInfo;
import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mr.Double
 * @data 2018/8/9-19:53
 * Description: 数据库操作类
 */
public class ResolverManager {
    private final String TAG = this.getClass().getSimpleName();

    private static ResolverManager mResolverManager;
    //监听器集合
    private Set<IUpdateNotiListener> mListenerSet = Collections.synchronizedSet(new HashSet<IUpdateNotiListener>());
    private ContentResolver mContentResolver;

    private ResolverManager() {
        mContentResolver = BaseApplication.getAppContext().getContentResolver();
    }

    public static ResolverManager getManager() {
        if (null == mResolverManager) {
            synchronized (ResolverManager.class) {
                if (null == mResolverManager) {
                    mResolverManager = new ResolverManager();
                }
            }
        }
        return mResolverManager;
    }

    public void registUpdateNotiListener(IUpdateNotiListener listener) {
        WeakReference<IUpdateNotiListener> reference = new WeakReference<>(listener);
        mListenerSet.add(reference.get());
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mContentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    private Uri insert(Uri uri, ContentValues values) {
        return mContentResolver.insert(uri, values);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return mContentResolver.delete(uri, selection, selectionArgs);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return mContentResolver.update(uri, values, selection, selectionArgs);
    }

    public void notifiChange(Uri uri) {
        mContentResolver.notifyChange(uri, null);
    }

    /**
     * 先判断该条通知是否已经在本地数据库存在，不存在才加到本地数据库
     *
     * @param info 通知
     */
    public void updateAndSaveToDb(NotiItemInfo info, ContentValues values) {
        OeLog.i(TAG, "updateAndSaveToDb() enter, info: " + info.toJson());
        Cursor cursor = query(DBInfo.COMMUNITY_NOTI_INFO_URI
                , new String[]{DBInfo.NotiColumns.COMMUNITY_NOTI_ID}
                , DBInfo.NotiColumns.COMMUNITY_NOTI_ID + "=?"
                , new String[]{info.getId() + ""}
                , null);
        if (null == cursor) {
            OeLog.e(TAG, "updateAndSaveToDb() cursor is null.");
            return;
        }

        if (0 == cursor.getCount()) {
            OeLog.i(TAG, "updateAndSaveToDb() new noti, save it.");
            values.clear();
            info.setIsView(Constants.NOTI_UNREAD);
            values.put(DBInfo.NotiColumns.COMMUNITY_NOTI_ID, info.getId());
            values.put(DBInfo.NotiColumns.COMMUNITY_NOTI_READ_STATE, Constants.NOTI_UNREAD);
            values.put(DBInfo.NotiColumns.COMMUNITY_NOTI_TIME, info.getNoticeBeginTime());
            values.put(DBInfo.NotiColumns.COMMUNITY_NOTI_INFO, info.toJson());
            insert(DBInfo.COMMUNITY_NOTI_INFO_URI, values);
            notifiUpdate(info);
        } else {
            OeLog.e(TAG, "updateAndSaveToDb() this noti already exist.");
        }

        cursor.close();
    }

    private void notifiUpdate(NotiItemInfo info) {
        OeLog.i(TAG, "notifiUpdate() enter.");
        if (0 == mListenerSet.size()) {
            OeLog.e(TAG, "notifiUpdate() mListenerSet size is 0.");
            return;
        }

        for (IUpdateNotiListener listener : mListenerSet) {
            listener.updateNoti(info);
        }
    }
}
