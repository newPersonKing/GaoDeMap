package com.example.gaodemap.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapCarInfo;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapRestrictionInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.PoiInputItemWidget;
import com.amap.api.navi.view.RouteOverLay;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.example.gaodemap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by emcc-pc on 2018/4/17
 * 驾车多路线规划.
 */

public class RestRouteShowActivity extends Activity implements AMapNaviListener,View.OnClickListener,CheckBox.OnCheckedChangeListener{

    private boolean iscongestion, iscost, ishightspeed, isavoidhightspeed;

    /*导航对象（单例）*/
    private AMapNavi mAMapNavi;
    private AMap mAmap;

    /**
     *地图对象
     */
    private Marker mStartMarker;
    private Marker mEndMarker;

    private NaviLatLng endLatlng = new NaviLatLng(39.955846, 116.352765);
    private NaviLatLng startLatlng = new NaviLatLng(39.925041, 116.437901);

    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();

    //途径坐标点集合
    private List<NaviLatLng> wayList=new ArrayList<>();
    //终点坐标集合【建议就一个终点】
    private List<NaviLatLng> endList=new ArrayList<>();
    //保存当前算好的路线
    private SparseArray<RouteOverLay> routeOverlays=new SparseArray<>();
    //当前用户选择的路线,在下个页面进行导航
    private int routeIndex;
    //路线的权值，路线重合的情况下，权值高的路线会覆盖权值低的路段
    private int zindex=1;
    //路线计算成功标志
    private boolean calculateSuccess=false;
    private boolean chooseRouteSuccess=false;

    @BindView(R.id.car_number)
    EditText editText;//车牌号
    @BindView(R.id.congestion)
    CheckBox congestion;//躲避拥堵
    @BindView(R.id.cost)
    CheckBox cost;//躲避收费
    @BindView(R.id.hightspeed)
    CheckBox hightspeed;//高速优先
    @BindView(R.id.avoidhightspeed)
    CheckBox avoidhightspeed;//不走高速
    @BindView(R.id.calculate)
    Button calculate;//开始算路
    @BindView(R.id.startpoint)
    Button startPoint;
    @BindView(R.id.endpoint)
    Button endPoint;
    @BindView(R.id.selectroute)
    Button selectroute;//选路径
    @BindView(R.id.gpsnavi)
    Button gpsnavi;//开始导航
    @BindView(R.id.emulatornavi)
    Button emulatornavi;
    @BindView(R.id.navi_view)
    MapView mRouteMapView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_calculate);
        ButterKnife.bind(this);
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState){
        calculate.setOnClickListener(this);
        startPoint.setOnClickListener(this);
        endPoint.setOnClickListener(this);
        selectroute.setOnClickListener(this);
        gpsnavi.setOnClickListener(this);
        emulatornavi.setOnClickListener(this);
        congestion.setOnCheckedChangeListener(this);
        cost.setOnCheckedChangeListener(this);
        hightspeed.setOnCheckedChangeListener(this);
        avoidhightspeed.setOnCheckedChangeListener(this);

        mRouteMapView.onCreate(savedInstanceState);
        mAmap = mRouteMapView.getMap();
        // 初始化Marker添加到地图
        mStartMarker = mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.start))));
        mEndMarker = mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.end))));

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRouteMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRouteMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRouteMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startList.clear();
        endList.clear();
        wayList.clear();
        routeOverlays.clear();
        mRouteMapView.onDestroy();
        //当前页面只是显示地图，activity销毁后不需要再回调导航的状态
        mAMapNavi.removeAMapNaviListener(this);
        mAMapNavi.destroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.calculate:
                //规划路线 并绘制
                clearRoute();
                if (isavoidhightspeed && ishightspeed) {
                    Toast.makeText(getApplicationContext(), "不走高速与高速优先不能同时为true.", Toast.LENGTH_LONG).show();
                }
                if (iscost && ishightspeed) {
                    Toast.makeText(getApplicationContext(), "高速优先与避免收费不能同时为true.", Toast.LENGTH_LONG).show();
                }
                /*
             * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
			 * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
			 */
                int strategyFlag=0;//计算路线的策略
                try {
                    strategyFlag=mAMapNavi.strategyConvert(iscongestion,isavoidhightspeed,iscost,ishightspeed,true);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(strategyFlag>=0){
                    String carNumber=editText.getText().toString();
                    AMapCarInfo aMapCarInfo=new AMapCarInfo();
                    //设置车牌
                    aMapCarInfo.setCarNumber(carNumber);
                    //设置车牌是否参与限行算路
                    aMapCarInfo.setRestriction(true);
                    mAMapNavi.setCarInfo(aMapCarInfo);
                    mAMapNavi.calculateDriveRoute(startList,endList,wayList,strategyFlag);
                }
                break;
            case R.id.startpoint:
                Intent sintent = new Intent(RestRouteShowActivity.this,SearchPoiActivity.class);
                Bundle bundle=new Bundle();
                bundle.putInt("pointType", PoiInputItemWidget.TYPE_START);
                sintent.putExtras(bundle);
                startActivityForResult(sintent,100);
                break;
            case R.id.endpoint:
                Intent eintent = new Intent(RestRouteShowActivity.this, SearchPoiActivity.class);
                Bundle ebundle = new Bundle();
                ebundle.putInt("pointType", PoiInputItemWidget.TYPE_DEST);
                eintent.putExtras(ebundle);
                startActivityForResult(eintent, 200);
                break;
            case R.id.selectroute:
                changeRoute();
                break;
            case R.id.gpsnavi:
                Intent gpsintent = new Intent(getApplicationContext(), RouteNaviActivity.class);
                gpsintent.putExtra("gps", true);
                startActivity(gpsintent);
                break;
            case R.id.emulatornavi:
                Intent intent = new Intent(getApplicationContext(), RouteNaviActivity.class);
                intent.putExtra("gps", false);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    private void changeRoute() {
        if(!calculateSuccess){
            Toast.makeText(this, "请先算路", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 计算出来的路径只有一条
         */
        if(routeOverlays.size()==1){
            chooseRouteSuccess=true;
            //必须告诉AmapNavi 你最后选择的哪条路
            mAMapNavi.selectRouteId(routeOverlays.keyAt(0));
            Toast.makeText(this, "导航距离:" + (mAMapNavi.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (mAMapNavi.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }
        if (routeIndex >= routeOverlays.size())
            routeIndex = 0;
        int routeID=routeOverlays.keyAt(routeIndex);
        //突出选择的那条路
        for(int i=0;i<routeOverlays.size();i++){
            int key=routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.4f);
        }
        routeOverlays.get(routeID).setTransparency(1.0f);
        //把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变得透明
        routeOverlays.get(routeID).setZindex(zindex++);

        //必须告诉AMapNavi 你最后选择的哪条路
        mAMapNavi.selectRouteId(routeID);

        Toast.makeText(this, "路线标签:" + mAMapNavi.getNaviPath().getLabels(), Toast.LENGTH_SHORT).show();
        routeIndex++;
        chooseRouteSuccess = true;

        /**选完路径后判断路线是否是限行路线**/
        AMapRestrictionInfo info = mAMapNavi.getNaviPath().getRestrictionInfo();
        if (!TextUtils.isEmpty(info.getRestrictionTitle())) {
            Toast.makeText(this, info.getRestrictionTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 清除当前地图上算好的路线
     */
    private void clearRoute() {
        for(int i=0;i<routeOverlays.size();i++){
            RouteOverLay routeOverLay= routeOverlays.valueAt(i);
            routeOverLay.removeFromMap();
        }
        routeOverlays.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getParcelableExtra("poi") != null) {
            clearRoute();
            Poi poi = data.getParcelableExtra("poi");
            if (requestCode == 100) {//起点选择完成
                //Toast.makeText(this, "100", Toast.LENGTH_SHORT).show();
                startLatlng = new NaviLatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude);
                mStartMarker.setPosition(new LatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude));
                startList.clear();
                startList.add(startLatlng);
            }
            if (requestCode == 200) {//终点选择完成
                //Toast.makeText(this, "200", Toast.LENGTH_SHORT).show();
                endLatlng = new NaviLatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude);
                mEndMarker.setPosition(new LatLng(poi.getCoordinate().latitude, poi.getCoordinate().longitude));
                endList.clear();
                endList.add(endLatlng);
            }
        }
    }

    @Override
    public void onInitNaviFailure() {
        //初始化失败
    }

    @Override
    public void onInitNaviSuccess() {
        //初始化成功
    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {
        calculateSuccess = false;
        Toast.makeText(getApplicationContext(), "计算路线失败，errorcode＝" + i, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }
    //计算路径成功
    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        //清空上次计算的路径列表
        routeOverlays.clear();
        HashMap<Integer,AMapNaviPath> paths=mAMapNavi.getNaviPaths();
        for(int i=0;i<ints.length;i++){
            AMapNaviPath path=paths.get(i);
            if(path!=null){
                drawRoutes(ints[i],path);
            }
        }
    }

    private void drawRoutes(int routeId, AMapNaviPath path) {
        calculateSuccess=true;
        //方法可以改变可视区域的倾斜角度并且保留其他属性
        mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay=new RouteOverLay(mAmap,path,this);
        routeOverLay.setTrafficLine(false);
        routeOverLay.addToMap();
        routeOverlays.put(routeId,routeOverLay);
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int id=compoundButton.getId();
        switch (id){
            case R.id.congestion:
                iscongestion = isChecked;
                break;
            case R.id.avoidhightspeed:
                isavoidhightspeed = isChecked;
                break;
            case R.id.cost:
                iscost = isChecked;
                break;
            case R.id.hightspeed:
                ishightspeed = isChecked;
                break;
            default:
                break;
        }
    }
}
