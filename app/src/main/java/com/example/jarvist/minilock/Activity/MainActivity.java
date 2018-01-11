package com.example.jarvist.minilock.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.feedback.FeedbackAgent;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.jarvist.minilock.Utils.BaiduMapInitUtils;
import com.example.jarvist.minilock.Map.BaiduMarkerClickListener;
import com.example.jarvist.minilock.Map.BmapLocationInit;
import com.example.jarvist.minilock.Utils.HttpUtils;
import com.example.jarvist.minilock.R;
import com.example.jarvist.minilock.Utils.ToastUtils;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.camera.CameraManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FloatingActionButton.OnClickListener  {

    public static final String SITE_URL = "http://api.heclouds.com/devices/";

    private ArrayList<OverlayOptions> markerList = new ArrayList<OverlayOptions>();
    private static final int REQUEST_CODE = 1;
    private static final int CAMERA_OK = 1;
    private MapView mapView;
    private DrawerLayout mDrawerLayout;
    private NavigationView nav_View;
    private FloatingActionButton fab, fab2;
    private BaiduMap mBaiduMap;
    private TextView nickName;
    private TextView email;
    private View nav_headerLayout;
    private CircleImageView headview;
    public static boolean isShowInfo = false;
    private Marker mk;
    private double currentLatitude;
    private double currentLongtitude;
    private BmapLocationInit LocInit;
    private long mExitTime;
    private String locateAddress;
    //光线传感器相关
    private SensorManager sm;
    private Sensor ligthSensor;
    private Camera mCamera;
    //private CameraManager manager;
    private String currentUserName;
    private String currentEmail;
    private boolean isScanCode = false;
    private boolean isFlashOn = false;
    private boolean isShowDrawer;
    private Camera.Parameters params;


    private int theme;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if(savedInstanceState != null){
            theme = savedInstanceState.getInt("theme");
            setTheme(theme);
        }




        SDKInitializer.initialize(getApplicationContext());
        AVUser currentUser = AVUser.getCurrentUser();
    setContentView(R.layout.activity_main);
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        initViews();
        mapInit();
        if (currentUser != null) {
            currentUserName = AVUser.getCurrentUser().getUsername();
            currentEmail = AVUser.getCurrentUser().getEmail();
            nickName.setText(currentUserName);
            email.setText(currentEmail);
            requestPermissions();

        }
        FeedbackAgent agent = new FeedbackAgent(getApplicationContext());
        agent.sync();
    }


    protected void mapInit() {
        if (mapView != null) {
            mBaiduMap = mapView.getMap();
            BaiduMapInitUtils.init(mBaiduMap);
        }
        LocInit = new BmapLocationInit(getApplicationContext(), mBaiduMap);
    }

    protected void mapDestroy() {
        if (mapView != null)
            mapView.onDestroy();
        if (mBaiduMap != null) {
            mBaiduMap.setMyLocationEnabled(false);
        }
    }

    protected void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        //光线传感器使用注册
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        //manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        ligthSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //获取精度
                float acc = event.accuracy;
                //获取光线强度
                float lux = event.values[0];
                int retval = Float.compare(lux, (float) 10.0);
                String s = "精度:" + acc + ",光线强度:" + lux + String.valueOf(isScanCode) + "   " + String.valueOf(isFlashOn + "   " + String.valueOf(retval));
                //Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                if (isScanCode) {
                    if (!isFlashOn) {
                        if (retval < 0) {
                            openLight();
                            isFlashOn = true;
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, ligthSensor, SensorManager.SENSOR_DELAY_NORMAL);


        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.memu1);
        }
        mapView = (MapView) findViewById(R.id.bmapView);
        nav_View = (NavigationView) findViewById(R.id.nav_view);




        nav_headerLayout = nav_View.inflateHeaderView(R.layout.nav_header);




        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.turnback);
        fab.setOnClickListener(this);
        fab2.setOnClickListener(this);
        nickName = (TextView) nav_headerLayout.findViewById(R.id.nickname);
        email = (TextView) nav_headerLayout.findViewById(R.id.mail);


        nav_View.setCheckedItem(R.id.nav_locked);

        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_account:
                        Intent start_account = new Intent(MainActivity.this, PersonalActivity.class);
                        startActivity(start_account);
                        break;


                    case R.id.nav_night:

                        theme = (theme == R.style.AppTheme) ? R.style.NightAppTheme : R.style.AppTheme;
                        MainActivity.this.recreate();
                        break;




                    case R.id.nav_locked:
                        try {
                            Toast.makeText(MainActivity.this, "申请上锁成功，请稍候......", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.nav_notification:
                        markerAppear();
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(14.f));
                        break;
                    case R.id.nav_logOut:
                        AVUser.logOut();// 清除缓存用户对象
                        //AVUser currentUser = AVUser.getCurrentUser();
                        Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent2);
                        MainActivity.this.finish();
                        break;// 现在的 currentUser 是 null 了
                    case R.id.nav_feedback:
                        FeedbackAgent agent = new FeedbackAgent(getApplicationContext());
                        agent.startDefaultThreadActivity();
                        break;
                    case R.id.nav_suggestion:
                        Intent start_advice = new Intent(MainActivity.this, RepairActivity.class);
                        start_advice.putExtra("address", locateAddress);
                        startActivity(start_advice);
                        //MainActivity.this.finish();
                        break;
                    case R.id.nav_share:
                        Intent start_share = new Intent(MainActivity.this, ShareActivity.class);
                        startActivity(start_share);
                        //MainActivity.this.finish();
                        break;
                    case R.id.nav_about:
                        Intent aboutIntent = new Intent(MainActivity.this, aboutActivity.class);
                        startActivity(aboutIntent);
                        //MainActivity.this.finish();
                    default:
                        mDrawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });
    }


    void requestPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            LocInit.requestLocation();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.turnback:
                LocInit.setFirstLocate(true);
                LocInit.requestLocation();
                mBaiduMap.setMyLocationEnabled(true);
                break;
            case R.id.fab:
                ToastUtils.show(MainActivity.this, "fab clicked");
                try {
                    if (Build.VERSION.SDK_INT > 22) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            //先判断有没有权限 ，没有就在这里进行权限的申请
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_OK);
                        }
                        isScanCode = true;
                        Toast.makeText(MainActivity.this, "fab clicked", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                        startActivityForResult(intent, REQUEST_CODE);
                        setResult(RESULT_OK, intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backup:
                Toast.makeText(MainActivity.this, "BACK", Toast.LENGTH_SHORT).show();
                break;
            case R.id.more:
                Toast.makeText(MainActivity.this, "more", Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
        if (AVUser.getCurrentUser() != null)
            changeHeadView();
        if (mapView != null)
            mapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocInit.setLocationStop();
        mapDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必须开启权限才能实现功能", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            LocInit = new BmapLocationInit(getApplicationContext(), mBaiduMap);
                            LocInit.requestLocation();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode){
            case REQUEST_CODE:
            String scanResult = null;
            if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) { //RESULT_OK = -1
                Bundle bundle = data.getExtras();
                scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                ToastUtils.show(MainActivity.this, scanResult);
            }
            final String deviceId = scanResult;
            final JSONObject object = new JSONObject();
            if (deviceId != null) {
                try {
                    object.put("status", 1);
                    new Thread() {

                        @Override
                        public void run() {
                            HttpUtils.post(MainActivity.this, SITE_URL, deviceId, object);
                        }
                    }.start();
                    isScanCode = false;
                    isFlashOn = false;
                    //closeLight();
                    Log.i("deviceId", deviceId);
                    Toast.makeText(MainActivity.this, "申请开锁成功，请稍候......", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
        }


    }





    public void changeHeadView() {
        AVUser currentUser = AVUser.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getString("ImageId") != null) {
                AVQuery<AVObject> query = new AVQuery<>("_File");
                query.getInBackground(currentUser.getString("ImageId"), new GetCallback<AVObject>() {
                    @Override
                    public void done(AVObject avObject, AVException e) {
                        if (e == null) {
                            Log.d("saved", "文件找到了");
                            AVFile file = new AVFile("test.txt", avObject.getString("url"), new HashMap<String, Object>());
                            file.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] bytes, AVException e) {
                                    if (e == null) {
                                        Log.d("saved", "文件大小" + bytes.length);
                                    } else {
                                        Log.d("saved", "出错了" + e.getMessage());
                                    }
                                    File downloadedFile = new File(Environment.getExternalStorageDirectory() + "/test.png");
                                    FileOutputStream fout = null;
                                    try {
                                        fout = new FileOutputStream(downloadedFile);
                                        fout.write(bytes);
                                        Log.d("saved", "文件写入成功.");
                                        fout.close();
                                    } catch (FileNotFoundException e1) {
                                        e1.printStackTrace();
                                        Log.d("saved", "文件找不到.." + e1.getMessage());
                                    } catch (IOException e1) {
                                        Log.d("saved", "文件读取异常.");
                                    }
                                    Bitmap bitmap = BitmapFactory.decodeFile(downloadedFile.getAbsolutePath());
                                    headview = (CircleImageView) findViewById(R.id.profile_photo);
                                    headview.setImageBitmap(bitmap);
                                }
                            }, new ProgressCallback() {
                                @Override
                                public void done(Integer integer) {
                                    Log.d("saved", "文件下载进度" + integer);
                                }
                            });
                        } else {
                            Log.d("saved", "出错了" + e.getMessage());
                        }
                    }
                });
            }
        }
    }



    public void markerAppear(){
        ToastUtils.show(MainActivity.this,"marker appear");
        currentLongtitude = LocInit.getCurrentLongtitude();
        currentLatitude = LocInit.getCurrentLatitude();
        BitmapDescriptor marker = BitmapDescriptorFactory
                .fromResource(R.drawable.mapmark);
        for(int i = 0;i<5;i++) {
            double mklatitude = 39.96 + 0.022 * i ;
            double mklongtitude = 116.40 + 0.02 * i;
            LatLng mk = new LatLng(mklatitude, mklongtitude);
            OverlayOptions option = new MarkerOptions().position(mk).icon(marker);
            markerList.add(option);
        }
        mBaiduMap.addOverlays(markerList);
        mBaiduMap.setOnMarkerClickListener(new BaiduMarkerClickListener(getApplicationContext(),mBaiduMap,
                currentLatitude,currentLongtitude));
    }



    @Override
    public void onBackPressed() {
        if(isShowInfo) {
            mBaiduMap.clear();
            isShowInfo = false;
        }
        else if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            if(System.currentTimeMillis() - mExitTime > 2000){
                Toast.makeText(MainActivity.this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }
            else{
                System.exit(0);
            }
        }


    }

    private void openLight() //开闪光灯
    {
        mCamera = CameraManager.getCamera();
        params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
        mCamera.startPreview();
    }

    private void closeLight() //关闪光灯
    {
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        theme = savedInstanceState.getInt("theme");
    }





    }










