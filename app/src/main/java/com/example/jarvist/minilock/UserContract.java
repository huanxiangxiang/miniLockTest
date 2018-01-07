package com.example.jarvist.minilock;

import android.provider.BaseColumns;

/**
 * Created by wangweiqiang on 2017/12/31.
 */

public class UserContract {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "user.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = " ,";

    private UserContract(){}

    public static class UserTable implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
        public static final String SQL_CREATE_TABLE = "CREATE TABLE "
                + UserTable.TABLE_NAME + " (" +
                UserTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                UserTable.COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                UserTable.COLUMN_NAME_PASSWORD + TEXT_TYPE + " )";
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + UserTable.TABLE_NAME;
    }

    public static class UnlockRecordTable implements BaseColumns{
        public static final String TABLE_NAME = "unlockRecord";
        public static final String COLUMN_NAME_USERID = "userId";
        public static final String COLUMN_NAME_ADRESS = "adress";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String SQL_CREATE_TABLE = "CREATE TABLE "
                + UnlockRecordTable.TABLE_NAME + " (" +
                UnlockRecordTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                UnlockRecordTable.COLUMN_NAME_USERID + TEXT_TYPE + COMMA_SEP +
                UnlockRecordTable.COLUMN_NAME_TIME + TEXT_TYPE +COMMA_SEP +
                UnlockRecordTable.COLUMN_NAME_ADRESS + TEXT_TYPE + " )";
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + UnlockRecordTable.TABLE_NAME;

    }
}
