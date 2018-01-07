package com.example.jarvist.minilock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangweiqiang on 2017/12/31.
 */

public class UserAdapter extends BaseAdapter {
    private Context context;
    private int resId;
    private List<Map<String, Object>> data;
    private LayoutInflater mLayoutInflater = null;
    private UserSQLHelper sqlHelper;

    public UserAdapter(Context ctx, int layout, UserSQLHelper s){
        context = ctx;
        resId = layout;
        mLayoutInflater = LayoutInflater.from(context);
        sqlHelper = s;
        data = readSQL();
    }

    @Override
    public int getCount() {
        return data.size();
    }
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        UserHolder userHolder;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(resId,parent,false);
            userHolder = new UserHolder(convertView);
            userHolder.tvAccount = (TextView) convertView.findViewById(R.id.adapter_account_item_username);
            userHolder.ivDelete = (ImageView) convertView.findViewById(R.id.adapter_account_item_delete);
            userHolder.ivDelete.setTag(position);
            userHolder.ivDelete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    deleteData(position);
                    freshData();
                    notifyDataSetChanged();
                }
            });
            convertView.setTag(userHolder);
        }else{
            userHolder = (UserHolder)convertView.getTag();
        }
        userHolder.tvAccount.setText((String)data.get(position).get(UserContract.UserTable.COLUMN_NAME_USERNAME));
        userHolder.ivDelete.setImageResource(R.drawable.delete_icon);
        return convertView;
    }

    private List<Map<String, Object>> readSQL() {
        List<Map<String, Object>> ret = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        // the column we need
        String[] projection = {
                UserContract.UserTable._ID,
                UserContract.UserTable.COLUMN_NAME_USERNAME,
                UserContract.UserTable.COLUMN_NAME_PASSWORD
        };
        Cursor c = db.query(
                UserContract.UserTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        while(c.moveToNext()) {
            Map<String, Object> tempData = new HashMap<>();
            tempData.put(UserContract.UserTable.COLUMN_NAME_USERNAME,
                    c.getString(c.getColumnIndex(UserContract.UserTable.COLUMN_NAME_USERNAME)));
            tempData.put(UserContract.UserTable.COLUMN_NAME_PASSWORD,
                    c.getString(c.getColumnIndex(UserContract.UserTable.COLUMN_NAME_PASSWORD)));
            tempData.put(UserContract.UserTable._ID,
                    c.getInt(c.getColumnIndex(UserContract.UserTable._ID)));
            ret.add(tempData);
        }
        c.close();
        db.close();
        return ret;
    }

    public void freshData() {
        data = readSQL();
    }


    public boolean isExistData(String username)
    {
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        // the column we need
        String[] projection = {
                UserContract.UserTable.COLUMN_NAME_USERNAME,
        };
        Cursor c = db.query(
                UserContract.UserTable.TABLE_NAME,
                projection,
                UserContract.UserTable.COLUMN_NAME_USERNAME + "= ?",
                new String[]{username},
                null,
                null,
                null
        );
        if (c.getCount() != 0)
        {
            c.close();
            db.close();
            return true;
        }
        else
        {
            c.close();
            db.close();
            return false;
        }
    }

    public void addData(String username, String password) {
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserContract.UserTable.COLUMN_NAME_USERNAME, username);
        values.put(UserContract.UserTable.COLUMN_NAME_PASSWORD, password);
        db.insert(UserContract.UserTable.TABLE_NAME, null, values);
        db.close();
    }

    public void deleteData(int position) {
        int id = (int)data.get(position).get(UserContract.UserTable._ID);
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
        String selections = UserContract.UserTable._ID + " = ?";
        String[] selectionArgs = {id+""};
        db.delete(UserContract.UserTable.TABLE_NAME, selections, selectionArgs);
        data.remove(position);
        db.close();
    }

    class UserHolder{
        public TextView tvAccount;
        public ImageView ivDelete;
        public UserHolder(View v){
            tvAccount = (TextView)v.findViewById(R.id.adapter_account_item_username);
            ivDelete = (ImageView)v.findViewById(R.id.adapter_account_item_delete);
        }
    }

}
