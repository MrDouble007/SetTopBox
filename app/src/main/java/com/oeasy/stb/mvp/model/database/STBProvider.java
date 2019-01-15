package com.oeasy.stb.mvp.model.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.oeasy.stb.mvp.model.manager.ResolverManager;
import com.oeasy.stb.utils.OeLog;

/**
 * @author Mr.Double
 * @data 2018/8/3-17:46
 * Description:
 */
public class STBProvider extends ContentProvider {
    private final String TAG = this.getClass().getSimpleName();

    private STBOpenHelper mOpenHelper;
    private Context mContext;

    private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int CODE_NOTI_INFO = 1;

    @Override
    public boolean onCreate() {
        OeLog.i(TAG, "onCreate() enter.");
        mContext = this.getContext();
        mOpenHelper = new STBOpenHelper(mContext);
        mMatcher.addURI(DBInfo.AUTHORITIES, DBInfo.NOTI_PATH, CODE_NOTI_INFO);
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (mMatcher.match(uri)) {
            case CODE_NOTI_INFO:
                return STBOpenHelper.STB_NOTI_TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        synchronized (STBProvider.class) {
            String tableName = getTableName(uri);
            if (TextUtils.isEmpty(tableName)) {
                OeLog.e(TAG, "insert() tableName is empty.");
                return null;
            }
            OeLog.d(TAG, "insert() tableName: " + tableName);

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            return db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        synchronized (STBProvider.class) {
            String tableName = getTableName(uri);
            if (TextUtils.isEmpty(tableName)) {
                OeLog.e(TAG, "insert() tableName is empty.");
                return null;
            }
            OeLog.d(TAG, "insert() tableName: " + tableName);

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            long rowId = db.insert(tableName, null, values);
            Uri returnUri = ContentUris.withAppendedId(uri, rowId);
            ResolverManager.getManager().notifiChange(returnUri);
            return returnUri;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        synchronized (STBProvider.class) {
            String tableName = getTableName(uri);
            if (TextUtils.isEmpty(tableName)) {
                OeLog.e(TAG, "insert() tableName is empty.");
                return 0;
            }
            OeLog.d(TAG, "insert() tableName: " + tableName);

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            return db.delete(tableName, selection, selectionArgs);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        synchronized (STBProvider.class) {
            String tableName = getTableName(uri);
            if (TextUtils.isEmpty(tableName)) {
                OeLog.e(TAG, "insert() tableName is empty.");
                return 0;
            }
            OeLog.d(TAG, "insert() tableName: " + tableName);

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            return db.update(tableName, values, selection, selectionArgs);
        }
    }

    private String getTableName(Uri uri) {
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case CODE_NOTI_INFO:
                tableName = STBOpenHelper.STB_NOTI_TABLE_NAME;
                break;
            default:
                OeLog.e(TAG, "getTableName() invalid uri.");
                break;
        }
        return tableName;
    }

}
