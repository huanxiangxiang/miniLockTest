package com.example.jarvist.minilock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
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

public class MainActivity extends AppCompatActivity {

    public static String SITE_URL = "api.mediatek.com/mcs/v2/devices/";
    private MapView mapView;
    private DrawerLayout mDrawerLayout;
    private NavigationView nav_View;
    private FloatingActionButton fab;
    private BaiduMap mBaiduMap;
    private TextView nickName;
    private TextView email;
    private View nav_headerLayout;

    private String currentUserName ;
    private String currentEmail ;
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
        nav_headerLayout = nav_View.inflateHeaderView(R.layout.nav_header);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        fab = (FloatingActionButton)findViewById(R.id.fab);
        nickName = (TextView)nav_headerLayout.findViewById(R.id.nickname);
        email = (TextView)nav_headerLayout.findViewById(R.id.mail);

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }
        nav_View.setCheckedItem(R.id.nav_chatting);
        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_account:
                        Toast.makeText(MainActivity.this,"nav_account",Toast.LENGTH_SHORT);
                        break;
                    case R.id.nav_logOut:
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

        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            currentUserName = AVUser.getCurrentUser().getUsername();
            currentEmail = AVUser.getCurrentUser().getEmail();
            nickName.setText(currentUserName);
            email.setText(currentEmail);
        }

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //if (result != null) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                       String path = "https://api.mediatek.com/mcs/v2/devices/DckZ7P05/datapoints.csv";
                        try{
                            URL url = new URL(path);
                            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                            connection.setConnectTimeout(5000);
                            connection.setRequestMethod("POST");
                            String upload = "lock_status,,1";
                            Integer length = upload.length();
                            connection.setRequestProperty("deviceKey","92FFNVfP7ZnAWDqJ");
                            connection.setRequestProperty("Content-Type","text/csv");
                            connection.setRequestProperty("Content-Length",length.toString());
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
    }



            /*Intent intent = new Intent (MainActivity.this,showActivity.class);
            intent.putExtra("data",result.getContents());
            startActivity(intent);*/

     /*   else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/

    Bitmap encode(String str){
        Bitmap bitmap = null;
        BitMatrix result;
        result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 200, 200);
            // 使用 ZXing Android Embedded 要写的代码
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e){
            e.printStackTrace();
        } catch (IllegalArgumentException iae){ // ?
            return null;
        }

        // 如果不使用 ZXing Android Embedded 的话，要写的代码

//        int w = result.getWidth();
//        int h = result.getHeight();
//        int[] pixels = new int[w * h];
//        for (int y = 0; y < h; y++) {
//            int offset = y * w;
//            for (int x = 0; x < w; x++) {
//                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
//            }
//        }
//        bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
//        bitmap.setPixels(pixels,0,100,0,0,w,h);

        return bitmap;
    }




}
