package com.example.jarvist.minilock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private DrawerLayout mDrawerLayout;
    private NavigationView nav_View;
    private FloatingActionButton fab;
    private BaiduMap mBaiduMap;
    private boolean isFirstLocate = true;
    public LocationClient mLocationClient = null;
    public BDAbstractLocationListener myListener = new MyLocationListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.bmapView);
        mBaiduMap = mapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        mBaiduMap.setMyLocationEnabled(true);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        nav_View = (NavigationView)findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
        nav_View.setCheckedItem(R.id.nav_chatting);
        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"fab clicked",Toast.LENGTH_SHORT).show();
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setOrientationLocked(false)//设置扫码的方向
                        .setPrompt("将条码放置于框内")//设置下方提示文字
                        .setCameraId(0)//前置或后置摄像头
                        .setBeepEnabled(false)//扫码提示音，默认开启
                        .initiateScan();
            }
        });

        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(! permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String [permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        else{
            requestLocation();
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener{

        @Override
        public void onReceiveLocation(BDLocation location){
           /* location.getTime();
            location.getLocationID();
            location.getLocType();
            location.getLatitude();
            location.getLongitude();*/
            if(location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }

    private void navigateTo(BDLocation location){
        MyLocationData locationData = new MyLocationData.Builder()
        .latitude(location.getLatitude())
        .longitude(location.getLongitude())
        .accuracy(location.getRadius())
        .direction(100).build();
        mBaiduMap.setMyLocationData(locationData);
        if(isFirstLocate){
            isFirstLocate = false;
            Toast.makeText(MainActivity.this,"定位到 "+ location.getAddrStr(),Toast.LENGTH_SHORT).show();
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(ll,18f);
            mBaiduMap.animateMapStatus(update);
            //update = MapStatusUpdateFactory.zoomTo(16f);
            //mBaiduMap.animateMapStatus(update);

        }

    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.backup:
                Toast.makeText(MainActivity.this,"BACK",Toast.LENGTH_SHORT).show();
                break;
            case R.id.more:
                Toast.makeText(MainActivity.this,"more",Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
            {
                if(grantResults.length > 0 ) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必须开启权限才能实现功能", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            requestLocation();
                        }
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:break;
        }
    }

    /*@Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(MainActivity.this,"无结果", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.putExtra("data",result.getContents());
                startActivity(intent);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

}
