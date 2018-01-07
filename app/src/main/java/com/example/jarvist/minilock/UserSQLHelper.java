package com.example.jarvist.minilock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wangweiqiang on 2017/12/31.
 */

public class UserSQLHelper extends SQLiteOpenHelper {

    public UserSQLHelper(Context context)
    {
        super(context, UserContract.DATABASE_NAME, null, UserContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(UserContract.UserTable.SQL_CREATE_TABLE);
        db.execSQL(UserContract.UnlockRecordTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(UserContract.UserTable.SQL_DELETE_ENTRIES);
        db.execSQL(UserContract.UnlockRecordTable.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
