package com.example.jarvist.minilock.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.jarvist.minilock.Map.GeoCode;
import com.example.jarvist.minilock.R;
import com.example.jarvist.minilock.Map.routeSearching;

import java.text.DecimalFormat;

/**
 * Created by Jarvist on 2018/1/5.
 */

public class InfoView{


    private Context mContext;
    private LatLng mCurrentLatLng;
    private LatLng mDesLatlng;
    private TextView agent;
    private TextView addr;
    private View view;
    private InfoWindow infoWindow;
    private routeSearching searching;
    private BaiduMap mBaiduMap;
    private LinearLayout nav_walk;
    private LinearLayout nav_bike;
    private LinearLayout nav_car;
    private TextView distanceView;
    private GeoCode geoCode;

    public InfoView(Context context, BaiduMap baiduMap, LatLng CurrentLatLng, LatLng DesLatLng){
        mContext = context;
        mBaiduMap = baiduMap;
        mCurrentLatLng = CurrentLatLng;
        mDesLatlng = DesLatLng;
        searching = new routeSearching(context,baiduMap,CurrentLatLng.latitude,CurrentLatLng.longitude,
                DesLatLng.latitude,DesLatLng.longitude);
        InitView();
    }


    private void InitView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.layout_infowindow,null);
        agent = (TextView)view.findViewById(R.id.agent_name);
        addr = (TextView)view.findViewById(R.id.agent_addr);
        distanceView = (TextView)view.findViewById(R.id.distance);
        DecimalFormat df = new DecimalFormat("#.0");
        double km = DistanceUtil.getDistance(mCurrentLatLng,mDesLatlng)/1000;
        distanceView.setText(String.valueOf(df.format(km))+"km");
        nav_bike = (LinearLayout)view.findViewById(R.id.navigation_bike);
        nav_car = (LinearLayout)view.findViewById(R.id.navigation_car);
        nav_walk = (LinearLayout)view.findViewById(R.id.navigation_walk);
        nav_walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searching.walkingSearchProcess();
            }
        });
        nav_bike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searching.bikingSearchProcess();
            }
        });
        nav_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searching.drivingSearchProcess();
            }
        });
        infoWindow = new InfoWindow(view,mDesLatlng,-50);
    }

    public InfoWindow getInfoWindow() {
        return infoWindow;
    }

    public TextView getAgent() {
        return agent;
    }

    public TextView getAddr() {
        return addr;
    }
}
