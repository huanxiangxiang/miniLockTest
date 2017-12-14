package com.example.jarvist.minilock;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements FloatingActionButton.OnClickListener,BaiduMap.OnMarkerClickListener{

    public static  final String SITE_URL = "http://api.heclouds.com/devices/";

    private MapView mapView;
    private DrawerLayout mDrawerLayout;
    private NavigationView nav_View;
    private FloatingActionButton fab,fab2;
    private BaiduMap mBaiduMap;
    private TextView nickName;
    private TextView email;
    private View nav_headerLayout;
    private String currentUserName ;
    private String currentEmail ;
    private boolean isFirstLocate = true;
    //public LocationClient mLocationClient = null;
    //public BDAbstractLocationListener myListener = new MyLocationListener();
    private Marker mk ;
    private double currentLatitude;
    private double currentLongtitude;

    private BmapLocationInit LocInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initViews();
        initUser();
        requestPermissions();
    }

    protected void initUser(){
        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            currentUserName = AVUser.getCurrentUser().getUsername();
            currentEmail = AVUser.getCurrentUser().getEmail();
            nickName.setText(currentUserName);
            email.setText(currentEmail);
        }
        else{
            startActivity(new Intent(MainActivity.this,launchActivity.class));
        }
    }


    void initViews(){
        mapView = (MapView)findViewById(R.id.bmapView);
        mBaiduMap = mapView.getMap();
        BaiduMapInitUtils.init(mBaiduMap);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        nav_View = (NavigationView)findViewById(R.id.nav_view);
        nav_headerLayout = nav_View.inflateHeaderView(R.layout.nav_header);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.turnback);
        fab.setOnClickListener(this);
        fab2.setOnClickListener(this);
        nickName = (TextView)nav_headerLayout.findViewById(R.id.nickname);
        email = (TextView)nav_headerLayout.findViewById(R.id.mail);
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
        nav_View.setCheckedItem(R.id.nav_locked);
        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_account:
                        Toast.makeText(MainActivity.this,"nav_account",Toast.LENGTH_SHORT);
                        break;
                    case R.id.nav_locked:
                        try{
                            Toast.makeText(MainActivity.this, "申请上锁成功，请稍候......", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case R.id.nav_notification:
                        markerAppear();
                        break;
                    case R.id.nav_logOut:
                        AVFile file = new AVFile();
                        //file.getOwnerObjectId();
                        AVUser.logOut();// 清除缓存用户对象
                        AVUser currentUser = AVUser.getCurrentUser();
                        Intent intent = new Intent(MainActivity.this,launchActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                        break;// 现在的 currentUser 是 null 了
                    default:
                        mDrawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });
    }

    void requestPermissions(){
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
            LocInit = new BmapLocationInit(getApplicationContext(),mBaiduMap);
            LocInit.requestLocation();
        }

    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.turnback:
                /*isFirstLocate = true;
                mLocationClient = new LocationClient(getApplicationContext());
                mLocationClient.registerLocationListener(myListener);
                mBaiduMap.setMyLocationEnabled(true);*/
                break;
            case R.id.fab:
                ToastUtils.show(MainActivity.this,"fab clicked");
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setOrientationLocked(false)//设置扫码的方向
                        .setPrompt("将条码放置于框内")//设置下方提示文字
                        .setCameraId(0)//前置或后置摄像头
                        .setBeepEnabled(false)//扫码提示音，默认开启
                        .initiateScan();
                break;
            default:
                break;

        }
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
        LocInit.getmLocationClient().stop();
        //mLocationClient.stop();
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
                            LocInit = new BmapLocationInit(getApplicationContext(),mBaiduMap);
                            LocInit.requestLocation();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        final String deviceId = result.getContents();
        final JSONObject object = new JSONObject();
        if (deviceId != null) {
            try{
                object.put("status",1);
                new Thread(){
                    @Override
                    public void run() {
                        HttpUtils.post(MainActivity.this,SITE_URL,deviceId,object);
                    }
                }.start();
                Log.i("deviceId",deviceId);
                Toast.makeText(MainActivity.this, "申请开锁成功，请稍候......", Toast.LENGTH_SHORT).show();
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public void markerAppear(){
        Toast.makeText(MainActivity.this,"marker appear",Toast.LENGTH_SHORT).show();
        currentLongtitude = LocInit.getCurrentLongtitude();
        currentLatitude = LocInit.getCurrentLatitude();
        double mklatitude = 39.963175;
        double mklongtitude = 116.400244;
        LatLng ll = new LatLng(mklatitude,mklongtitude);
        BitmapDescriptor marker = BitmapDescriptorFactory
                .fromResource(R.drawable.map_marker);
        OverlayOptions option = new MarkerOptions()
                .position(ll)
                .icon(marker);
        mk = (Marker)(mBaiduMap.addOverlay(option));
        mBaiduMap.setOnMarkerClickListener(new BaiduMarkerClickListener(getApplicationContext(),mBaiduMap,
                mklatitude,mklongtitude,currentLatitude,currentLongtitude));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

}
