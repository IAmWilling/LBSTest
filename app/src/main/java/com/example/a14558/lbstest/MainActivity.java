package com.example.a14558.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient mLocationClient;
    private TextView postionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyLocationListenter myLocationListenter = new MyLocationListenter();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myLocationListenter);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        postionText = findViewById(R.id.position_text_view);
        mapView = findViewById(R.id.bmapView);
        LocationClientOption option = new LocationClientOption();
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，设置定位模式，默认高精度
//LocationMode.Hight_Accuracy：高精度；
//LocationMode. Battery_Saving：低功耗；
//LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
//可选，设置返回经纬度坐标类型，默认GCJ02
//GCJ02：国测局坐标；
//BD09ll：百度经纬度坐标；
//BD09：百度墨卡托坐标；
//海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
//可选，设置发起定位请求的间隔，int类型，单位ms
//如果设置为0，则代表单次定位，即仅定位一次，默认为0
//如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
//可选，设置是否使用gps，默认false
//使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
//可选，定位SDK内部是一个service，并放到了独立进程。
//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
//可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
//可选，V7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);
//mLocationClient为第二步初始化过的LocationClient对象
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        List<String> permisionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permisionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permisionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permisionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permisionList.isEmpty()) {
            String[] permissions = permisionList.toArray(new String[permisionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }
    private void requestLocation(){
        mLocationClient.start();
    }

    private void navigateTo(BDLocation location) {
        if(isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length > 0) {
                    for(int result : grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this,"必须统一所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    mLocationClient.start();
                }
        }
    }
    public class MyLocationListenter extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("维度：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("经线：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
            currentPosition.append("定位方式：");
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
            }
            postionText.setText(currentPosition);

                navigateTo(bdLocation);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
        }
    }

}
