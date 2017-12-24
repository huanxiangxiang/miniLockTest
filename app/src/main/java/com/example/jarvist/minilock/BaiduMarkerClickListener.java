package com.example.jarvist.minilock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * Created by Jarvist on 2017/12/9.
 */

public class BaiduMarkerClickListener implements BaiduMap.OnMarkerClickListener,OnGetRoutePlanResultListener {

    private Context context;
    private InfoWindow infoWindow;
    public BaiduMap mBaiduMap;
    private RouteLine route  = null;
    private Button navigate;
    private double mCurrentLantitude;
    private double mCurrentLongtitude;
    private double mDesLantitude;
    private double mDesLongtitude;
    RoutePlanSearch mSearch = null;
    private OverlayManager routeOverlay = null;
    private View infoView;
    private Button walking;
    private Button biking;
    private Button driving;
    public BaiduMarkerClickListener(Context context,BaiduMap mBaiduMap,
                                    double mCurrentLantitude, double mCurrentLongtitude,
                                    double mDesLantitude,double mDesLongtitude){
        this.context = context;
        this.mBaiduMap = mBaiduMap;
        this.mCurrentLantitude = mCurrentLantitude;
        this.mCurrentLongtitude = mCurrentLongtitude;
        this.mDesLantitude = mDesLantitude;
        this.mDesLongtitude = mDesLongtitude;


        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        final LatLng markerll = marker.getPosition();
//        LayoutInflater inflater = LayoutInflater.from(context.getApplicationContext());
//        infoView = inflater.inflate(R.layout.layout_infowindow,null);
//        walking = (Button)infoView.findViewById(R.id.walk);
//        biking = (Button)infoView.findViewById(R.id.bike);
        driving = new Button(context.getApplicationContext());
        driving.setText("导航");
        InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick() {
                SearchProcess();
            }
        };
        infoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(driving),markerll,-70,listener);
        mBaiduMap.showInfoWindow(infoWindow);
        return false;
    }

    public void SearchProcess(){
        route = null;
        mBaiduMap.clear();
        PlanNode stNode = PlanNode.withLocation(new LatLng(mDesLantitude,mDesLongtitude));
        PlanNode edNode = PlanNode.withLocation(new LatLng(mCurrentLantitude,mCurrentLongtitude));
        mSearch.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode)
                .to(edNode));
       // mSearch.destroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if(drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR){
            Toast.makeText(context,"Sorry! Search failed!",Toast.LENGTH_SHORT).show();
        }
        if(drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
            drivingRouteResult.getSuggestAddrInfo();
        }
        if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR){
            route = drivingRouteResult.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap );
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(drivingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }
}
