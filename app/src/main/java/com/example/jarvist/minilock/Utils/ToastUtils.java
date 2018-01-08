package com.example.jarvist.minilock.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Jarvist on 2017/12/14.
 */

public  class ToastUtils  {
    public static void show(Context context,String s){
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
}
