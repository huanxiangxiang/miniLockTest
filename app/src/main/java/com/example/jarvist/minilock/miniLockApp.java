package com.example.jarvist.minilock;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Jarvist on 2017/12/14.
 */

public class miniLockApp extends Application {
    @Override
    public void onCreate() {

        AVOSCloud.initialize(this,"YGB2ErmfjNR7dpAPhPjWFrt4-gzGzoHsz","mxwOs75S8CpyqBGRuGn942mn");
        AVOSCloud.setDebugLogEnabled(true);
        super.onCreate();
    }
}
