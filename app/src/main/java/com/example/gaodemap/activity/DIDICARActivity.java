package com.example.gaodemap.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.AlphaAnimation;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.example.gaodemap.R;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.functions.Consumer;

public class DIDICARActivity extends AppCompatActivity {

    MapView mMapView = null;
    AMap aMap;

    private double[] coords;
    private List<LatLng> carsLatLng;
    private List<LatLng> goLatLng;
    private List<Marker> showMarks;
    private List<SmoothMoveMarker> smoothMarkers;

    private double lng = 0.0;
    private double lat = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_didicar);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        initView();
        initData();
    }

    private void initView() {
        Button put = (Button) findViewById(R.id.put);
        Button run = (Button) findViewById(R.id.run);
        Button go=findViewById(R.id.bt_go);
        Button single=findViewById(R.id.gd_single);
        Button gd_all_time=findViewById(R.id.gd_all_time);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DIDICARActivity.this,SmoothMoveActivity.class);
                startActivity(intent);
            }
        });

        put.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (smoothMarkers != null) {
                    for (int i = 0; i < smoothMarkers.size(); i++) {
                        smoothMarkers.get(i).destroy();
                    }
                }
                if (showMarks == null) {
                    showMarks = new ArrayList<Marker>();
                }
                for (int j = 0; j < showMarks.size(); j++) {
                    showMarks.get(j).remove();
                }
                for (int i = 0; i < carsLatLng.size(); i++) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car_up);
                    lng = Double.valueOf(carsLatLng.get(i).longitude);
                    lat = Double.valueOf(carsLatLng.get(i).latitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(lat, lng))
                            .icon(icon);
                    showMarks.add(aMap.addMarker(markerOptions));
                    Animation startAnimation = new AlphaAnimation(0, 1);
                    startAnimation.setDuration(600);
//                            showMarks.get(i).setRotateAngle(Float.valueOf(listBaseBean.datas.get(i).angle));
                    showMarks.get(i).setAnimation(startAnimation);
                    showMarks.get(i).setRotateAngle(new Random().nextInt(359));
                    showMarks.get(i).startAnimation();
                }
            }
        });

        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (smoothMarkers != null) {
                    for (int i = 0; i < smoothMarkers.size(); i++) {
                        smoothMarkers.get(i).destroy();
                    }
                }
                if (showMarks == null) {
                    showMarks = new ArrayList<Marker>();
                }
                for (int j = 0; j < showMarks.size(); j++) {
                    showMarks.get(j).remove();
                }
                smoothMarkers = null;
                smoothMarkers = new ArrayList<SmoothMoveMarker>();
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car_up);
                //carsLatLng 初始的car的位置 一共设置了3辆车
                for (int i = 0; i < carsLatLng.size(); i++) {
                    /*每一个newoords 存储的初始化的车的起始点与结束点*/
                    double[] newoords = {Double.valueOf(carsLatLng.get(i).longitude), Double.valueOf(carsLatLng.get(i).latitude),
                            Double.valueOf(goLatLng.get(i).longitude), Double.valueOf(goLatLng.get(i).latitude)};
                    coords = newoords;
                    movePoint(icon);
                }
            }
        });
        getPermissions();
        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DIDICARActivity.this,SingleRouteCalculateActivity.class);
                startActivity(intent);

            }
        });
        gd_all_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DIDICARActivity.this,GPSNaviActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getPermissions(){
        RxPermissions rxPermissions=new RxPermissions(this);
        rxPermissions.request(Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                });
    }

    private void initData() {
        LatLng car1 = new LatLng(39.902138, 116.391415);
        LatLng car2 = new LatLng(39.935184, 116.328587);
        LatLng car3 = new LatLng(39.987814, 116.488232);
        carsLatLng = new ArrayList<>();
        carsLatLng.add(car1);
        carsLatLng.add(car2);
        carsLatLng.add(car3);

        LatLng go1 = new LatLng(39.96782, 116.403775);
        LatLng go2 = new LatLng(39.891225, 116.322235);
        LatLng go3 = new LatLng(39.883322, 116.415619);
        goLatLng = new ArrayList<>();
        goLatLng.add(go1);
        goLatLng.add(go2);
        goLatLng.add(go3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
    /*bitmap 显示的汽车图标*/
    public void movePoint(BitmapDescriptor bitmap) {
        List<LatLng> points = readLatLngs();
//        LatLngBounds bounds = new LatLngBounds(points.get(0), points.get(points.size() - 2));
//        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

//        SparseArrayCompat sparseArrayCompat = new SparseArrayCompat();
        /*新建smoothMarker的对象*/
        SmoothMoveMarker smoothMarker = new SmoothMoveMarker(aMap);
        smoothMarkers.add(smoothMarker);
        /*每次操作的都是最后的一个smoothMarker*/
        smoothMarker.setDescriptor(bitmap);
        /*汽车起始点*/
        LatLng drivePoint = points.get(0);
        /**/
        Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
        points.set(pair.first, drivePoint);
        List<LatLng> subList = points.subList(pair.first, points.size());

        smoothMarker.setPoints(subList);
        smoothMarker.setTotalDuration(10);
        smoothMarker.startSmoothMove();
    }

    private List<LatLng> readLatLngs() {
        List<LatLng> points = new ArrayList<LatLng>();
        for (int i = 0; i < coords.length; i += 2) {
            points.add(new LatLng(coords[i + 1], coords[i]));
        }
        return points;
    }
}
