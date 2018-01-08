package com.example.jarvist.minilock.Map;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.jarvist.minilock.Overlay.BikingRouteOverlay;
import com.example.jarvist.minilock.Overlay.DrivingRouteOverlay;
import com.example.jarvist.minilock.Overlay.OverlayManager;
import com.example.jarvist.minilock.Overlay.WalkingRouteOverlay;

/**
 * Created by Jarvist on 2018/1/5.
 */

public class routeSearching implements OnGetRoutePlanResultListener {

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
    public routeSearching(Context context,BaiduMap mBaiduMap,
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
    public void drivingSearchProcess(){
        route = null;
        mBaiduMap.clear();
        PlanNode stNode = PlanNode.withLocation(new LatLng(mCurrentLantitude,mCurrentLongtitude));
        PlanNode edNode = PlanNode.withLocation(new LatLng(mDesLantitude,mDesLongtitude));
        mSearch.drivingSearch((new DrivingRoutePlanOption())
                .from(stNode)
                .to(edNode));
        // mSearch.destroy();
    }
    public void walkingSearchProcess(){
        route = null;
        mBaiduMap.clear();
        PlanNode stNode = PlanNode.withLocation(new LatLng(mCurrentLantitude,mCurrentLongtitude));
        PlanNode edNode = PlanNode.withLocation(new LatLng(mDesLantitude,mDesLongtitude));
        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode)
                .to(edNode));
        // mSearch.destroy();
    }
    public void bikingSearchProcess(){
        route = null;
        mBaiduMap.clear();
        PlanNode stNode = PlanNode.withLocation(new LatLng(mCurrentLantitude,mCurrentLongtitude));
        PlanNode edNode = PlanNode.withLocation(new LatLng(mDesLantitude,mDesLongtitude));
        mSearch.bikingSearch((new BikingRoutePlanOption())
                .from(stNode)
                .to(edNode));
        // mSearch.destroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        if(walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR){
            Toast.makeText(context,"Sorry! Search failed!",Toast.LENGTH_SHORT).show();
        }
        if(walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
            walkingRouteResult.getSuggestAddrInfo();
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR){
            route = walkingRouteResult.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap );
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(walkingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
        mSearch.destroy();
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
        mSearch.destroy();
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        if(bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR){
            Toast.makeText(context,"Sorry! Search failed!",Toast.LENGTH_SHORT).show();
        }
        if(bikingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR){
            bikingRouteResult.getSuggestAddrInfo();
        }
        if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR){
            route = bikingRouteResult.getRouteLines().get(0);
            BikingRouteOverlay overlay = new BikingRouteOverlay(mBaiduMap);
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(bikingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
        mSearch.destroy();
    }

}
