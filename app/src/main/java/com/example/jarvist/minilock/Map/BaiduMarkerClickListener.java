package com.example.jarvist.minilock.Map;

import android.content.Context;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;

import com.baidu.mapapi.model.LatLng;
import com.example.jarvist.minilock.Activity.MainActivity;
import com.example.jarvist.minilock.View.InfoView;

/**
 * Created by Jarvist on 2017/12/9.
 */

public class BaiduMarkerClickListener implements BaiduMap.OnMarkerClickListener {

    private Context context;
    public BaiduMap mBaiduMap;
    private double mCurrentLantitude;
    private double mCurrentLongtitude;
    private GeoCode geoCode;
    public BaiduMarkerClickListener(Context context,BaiduMap mBaiduMap,
                                    double mCurrentLantitude, double mCurrentLongtitude
                                    ){
        this.context = context;
        this.mBaiduMap = mBaiduMap;
        this.mCurrentLantitude = mCurrentLantitude;
        this.mCurrentLongtitude = mCurrentLongtitude;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        InfoView infoView = new InfoView(context,mBaiduMap,
                new LatLng(mCurrentLantitude,mCurrentLongtitude),
                marker.getPosition());
        geoCode = new GeoCode(context,marker.getPosition(),infoView);
        mBaiduMap.showInfoWindow(infoView.getInfoWindow());
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(marker.getPosition()));
        MainActivity.isShowInfo = true;
        return false;
    }


}
