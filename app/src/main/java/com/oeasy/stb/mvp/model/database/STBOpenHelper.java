package com.oeasy.stb.mvp.model.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.oeasy.stb.utils.OeLog;

/**
 * @author Mr.Double
 * @data 2018/8/2-13:56
 * Description:
 */
public class STBOpenHelper extends SQLiteOpenHelper {
    private final String TAG = this.getClass().getSimpleName();

    private static final int DB_VERSION = 1;
    private static final String STB_DB_NAME = "stb_database.db";
    public static final String STB_NOTI_TABLE_NAME = "community_noti_info";

    private final static String CREAT_COMMUNITY_NOTI_TABLE = "create table " + STB_NOTI_TABLE_NAME + "(" + BaseColumns._ID
            + " integer primary key autoincrement," + DBInfo.NotiColumns.COMMUNITY_NOTI_ID + " text,"
            + DBInfo.NotiColumns.COMMUNITY_NOTI_READ_STATE + " text,"
            + DBInfo.NotiColumns.COMMUNITY_NOTI_TIME + " text,"
            + DBInfo.NotiColumns.COMMUNITY_NOTI_INFO + " text);";

    public STBOpenHelper(Context context) {
        super(context, STB_DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        OeLog.i(TAG, "onCreate() enter.");
        try {
            db.execSQL(CREAT_COMMUNITY_NOTI_TABLE);
        } catch (SQLException e) {
            OeLog.e(TAG, "onCreate() SQLException occured.");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        OeLog.i(TAG, "onUpgrade() enter.");
        db.execSQL("DROP TABLE IF EXISTS " + STB_NOTI_TABLE_NAME);
        onCreate(db);
    }

}
