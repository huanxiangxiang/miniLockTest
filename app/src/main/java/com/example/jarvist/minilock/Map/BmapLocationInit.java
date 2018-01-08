package com.example.jarvist.minilock.Map;

import android.content.Context;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by Jarvist on 2017/12/12.
 */

public class BmapLocationInit {

    private double currentLatitude;
    private double currentLongtitude;
    private boolean isFirstLocate = true;
    private Context mContext;
    private BaiduMap mBaidumap;
    private BDAbstractLocationListener myListener = new MyLocationListener();
    private LocationClient mLocationClient = null;
    private LocationClientOption option;

    public BmapLocationInit(Context context,BaiduMap baidumap){

        mContext = context;
        mBaidumap = baidumap;
        mLocationClient = new LocationClient(mContext);
        mLocationClient.registerLocationListener(myListener);
    }

    public void setFirstLocate(boolean firstLocate) {
        isFirstLocate = firstLocate;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public double getCurrentLongtitude() {
        return currentLongtitude;
    }



    public void initLocation(){
        option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    public void setLocationStop(){
        if(mLocationClient != null)
        mLocationClient.stop();
    }


    public class MyLocationListener extends BDAbstractLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location){
            if(location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
                currentLatitude = location.getLatitude();
                currentLongtitude = location.getLongitude();
            }
        }
    }

    private void navigateTo(BDLocation location){
        MyLocationData locationData = new MyLocationData.Builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracy(location.getRadius())
                .direction(100).build();
        mBaidumap.setMyLocationData(locationData);
        if(isFirstLocate){
            isFirstLocate = false;
            Toast.makeText(mContext,"定位到 "+ location.getAddrStr(),Toast.LENGTH_SHORT).show();
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll,18f);
            mBaidumap.animateMapStatus(update);

        }
    }
}
