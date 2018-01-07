package com.example.jarvist.minilock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.camera.CameraManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE =1;
    private static final int CAMERA_OK = 1;
    private MapView mapView;
    private DrawerLayout mDrawerLayout;
    private NavigationView nav_View;
    private FloatingActionButton fab;
    private BaiduMap mBaiduMap;
    private TextView nickName;
    private TextView email;
    private View nav_headerLayout;
    private CircleImageView headview;
    private String locateAddress;
    //光线传感器相关
    private SensorManager sm;
    private Sensor ligthSensor;
    private Camera mCamera;
    //private CameraManager manager;

    private String currentUserName ;
    private String currentEmail ;
    private boolean isFirstLocate = true;
    private boolean isScanCode = false;
    private boolean isFlashOn = false;
    private Camera.Parameters params;
    public LocationClient mLocationClient = null;
    public BDAbstractLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String sExtra = "";
        if(intent.getExtras()!=null) {
            sExtra = intent.getStringExtra("result");
            if (!sExtra.isEmpty() && !sExtra.equals("")) {
                Toast.makeText(MainActivity.this, sExtra, Toast.LENGTH_SHORT).show();
            }
        }
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
                String s = "精度:"+acc+",光线强度:"+lux + String.valueOf(isScanCode)+"   "+String.valueOf(isFlashOn +"   "+String.valueOf(retval));
                Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
                if (isScanCode) {
                    if(!isFlashOn) {
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
        nickName = (TextView)nav_headerLayout.findViewById(R.id.nickname);
        email = (TextView)nav_headerLayout.findViewById(R.id.mail);
        changeHeadView();
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
                        Intent start_account=new Intent(MainActivity.this,PersonalActivity.class);
                        startActivity(start_account);
                        break;
                    case R.id.nav_logOut:
                        AVUser.logOut();// 清除缓存用户对象
                        AVUser currentUser = AVUser.getCurrentUser();
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        MainActivity.this.finish();
                        break;// 现在的 currentUser 是 null 了
                    case R.id.nav_suggestion:
                        Intent start_advice = new Intent(MainActivity.this, RepairActivity.class);
                        start_advice.putExtra("address",locateAddress);
                        startActivity(start_advice);
                        MainActivity.this.finish();
                        break;
                    case R.id.nav_share:
                        Intent start_share = new Intent(MainActivity.this, ShareActivity.class);
                        startActivity(start_share);
                        MainActivity.this.finish();
                        break;
                    case R.id.nav_record:
                        Intent start_record = new Intent(MainActivity.this, RecordActivity.class);
                        startActivity(start_record);
                        MainActivity.this.finish();
                    default:
                        mDrawerLayout.closeDrawers();
                        break;
                }
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>22){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                        //先判断有没有权限 ，没有就在这里进行权限的申请
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.CAMERA},CAMERA_OK);

                    }else {
                        //说明已经获取到摄像头权限了 想干嘛干嘛
                    }
                }else {
//这个说明系统版本在6.0之下，不需要动态获取权限。

                }
                isScanCode = true;
                Toast.makeText(MainActivity.this,"fab clicked",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                /*
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setOrientationLocked(false)//设置扫码的方向
                        .setPrompt("将条码放置于框内")//设置下方提示文字
                        .setCameraId(0)//前置或后置摄像头
                        .setBeepEnabled(false)//扫码提示音，默认开启
                        .initiateScan();
                        */


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
            locateAddress = location.getAddrStr();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        /*
        if (result != null) {
             private void sendRequest{
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url()
                    }
                })
            }


            Intent intent = new Intent (MainActivity.this,showActivity.class);
            intent.putExtra("data",result.getContents());
            startActivity(intent);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }*/
        if (resultCode == RESULT_OK) { //RESULT_OK = -1
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            Toast.makeText(MainActivity.this, scanResult, Toast.LENGTH_LONG).show();
        }
        isScanCode = false;
        isFlashOn = false;
        closeLight();
    }

    public void changeHeadView()
    {
        AVUser currentUser=AVUser.getCurrentUser();
        if(currentUser.getString("ImageId")!=null)
        {
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
                                Bitmap bitmap= BitmapFactory.decodeFile(downloadedFile.getAbsolutePath());
                                headview=(CircleImageView)findViewById(R.id.profile_photo);
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


/*
    private void lightSwitch(boolean lightStatus) {
        if (lightStatus) { // 关闭手电筒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            }
        } else { // 打开手电筒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                final PackageManager pm = getPackageManager();
                final FeatureInfo[] features = pm.getSystemAvailableFeatures();
                for (final FeatureInfo f : features) {
                    if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) { // 判断设备是否支持闪光灯
                        if (null == mCamera) {
                            mCamera = Camera.open();
                        }
                        final Camera.Parameters parameters = mCamera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(parameters);
                        mCamera.startPreview();
                    }
                }
            }
        }
    }*/


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


}





