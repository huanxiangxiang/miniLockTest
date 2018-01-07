package com.example.jarvist.minilock;

import android.app.Application;
import android.os.StrictMode;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by wangweiqiang on 2017/12/24.
 */



public class miniLockApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this,"YGB2ErmfjNR7dpAPhPjWFrt4-gzGzoHsz","mxwOs75S8CpyqBGRuGn942mn");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

    }


}
