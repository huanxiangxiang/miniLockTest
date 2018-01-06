package com.example.jarvist.minilock;

import android.content.Context;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.route.RoutePlanSearch;

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
