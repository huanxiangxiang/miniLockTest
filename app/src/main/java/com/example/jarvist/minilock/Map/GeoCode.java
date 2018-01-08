package com.example.jarvist.minilock.Map;

import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.jarvist.minilock.View.InfoView;

/**
 * Created by Jarvist on 2018/1/5.
 */

public class  GeoCode implements OnGetGeoCoderResultListener {
    GeoCoder mSearch = null;
    private Context mContext;
    private LatLng mDesCenter;
    private String addr;
    private String business;
    private InfoView mView;
    public GeoCode(Context context,LatLng desCenter,InfoView view){
        mContext = context;
        mDesCenter = desCenter;
        mView = view;
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(mDesCenter));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            mSearch.destroy();
            return;
        }
        else{
            business = result.getBusinessCircle();
            addr = result.getAddress();
            mView.getAddr().setText(addr);
            mView.getAgent().setText(business);
            mSearch.destroy();
        }
    }

    public String getAddr() {
        return addr;
    }

    public String getBusiness() {
        return business;
    }
}
