package com.yongxiang.liu.testmap.activities;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.yongxiang.liu.testmap.R;
import com.yongxiang.liu.testmap.utils.ScreenUtils;


public class PointAggregationAty extends Activity implements OnClickListener, AMap.OnCameraChangeListener, AMap.OnMarkerClickListener {

	private MapView mapView;//**  地图view  */
	private AMap aMap;   	//***  高德amap */
	private int screenHeight;// 屏幕高度(px)
	private int screenWidth;// 屏幕宽度(px)
	private ArrayList<MarkerOptions> markerOptionsListall = new ArrayList<MarkerOptions>();	//** 所有的marker  */
	private ArrayList<MarkerOptions> markerOptionsListInView = new ArrayList<MarkerOptions>();//**  视野内的marker  */
	private ImageView img_location;
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_aggregation);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = ScreenUtils.getScreenWidth(this);
		screenHeight = ScreenUtils.getScreenHeight(this);
		mapView = (MapView) findViewById(R.id.map);
		img_location = (ImageView) findViewById(R.id.img_location);
		img_location.setOnClickListener(this);
		mapView.onCreate(savedInstanceState);// 方法必须重写

		if (aMap == null) {
			aMap = mapView.getMap();
			UiSettings mUiSettings = aMap.getUiSettings();//拿到地图工具类
			mUiSettings.setTiltGesturesEnabled(false);// 禁用倾斜手势。
			mUiSettings.setRotateGesturesEnabled(false);// 禁用旋转手势。
			mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//放大缩小按钮放在屏幕中间
			aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
			aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
		}
		// 添加临时数据
		initDatas();
	}

	private void initDatas() {//模拟1000条数据
		for(int i=0;i<1000;i++) {
			Random r = new Random();
			double lat = (290000 + r.nextInt(30000)) / 10000.0D;
			double lng = (1120000 + r.nextInt(30000)) / 10000.0D;
			addDate(lat, lng,i+","+i%3+"."+i*2);//桩子总数,类型，station id
		}
	}

	// 添加临时数据
	private void addDate(double latitude,double longitude,String str) {
		LatLng latLng = new LatLng(latitude, longitude);
		MarkerOptions markerOptions = new MarkerOptions().position(latLng) .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
		markerOptions.title(str);
		markerOptionsListall.add(markerOptions);
	}
             float  leavel =0;
	@Override public boolean onMarkerClick(Marker marker) {
		Log.e("点击mark","");
		Log.e("点击mark",""+marker.getOptions().getTitle());

		if (marker.getOptions().getTitle().equals("d")){
			leavel=aMap.getCameraPosition().zoom;
			leavel=leavel+3;
			aMap.moveCamera(CameraUpdateFactory.zoomTo(leavel));
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
		}else {
			aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
			aMap.moveCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
		}

		return true;
	}

	@Override public void onCameraChange(CameraPosition cameraPosition) { } //地图改变时
	@Override public void onCameraChangeFinish(CameraPosition cameraPosition) { resetMarks(); }// 地图改变完之后的监听
	@Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }//必须重写
	@Override protected void onPause() { super.onPause(); mapView.onPause(); }
	@Override protected void onResume() { super.onResume(); mapView.onResume(); }
	@Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }

	//聚合  入口点
	/** 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker */
	private void resetMarks() {
		Projection projection = aMap.getProjection();// 开始刷新
		Point p = null;
		markerOptionsListInView.clear();
		for (MarkerOptions mp : markerOptionsListall) {// 获取在当前视野内的marker;提高效率
			p = projection.toScreenLocation(mp.getPosition());
			if (p.x < 0 || p.y < 0 || p.x > screenWidth || p.y > screenHeight) {// 不添加到计算的列表中

			} else {
				markerOptionsListInView.add(mp);
			}
		}
		//自定义的聚合类MyMarkerCluster
		ArrayList<MarkerImageView> clustersMarker = new ArrayList<MarkerImageView>();
		for (MarkerOptions mp : markerOptionsListInView) {
			if (clustersMarker.size() == 0) {// 添加一个新的自定义marker
				clustersMarker.add(new MarkerImageView( PointAggregationAty.this, mp, projection, 120,1));// 80=相距多少才聚合
			} else {
				boolean isIn = false;
				for (MarkerImageView cluster : clustersMarker) {
					// 判断当前的marker是否在前面marker的聚合范围内 并且每个marker只会聚合一次。
					if (cluster.getBounds().contains(mp.getPosition())) {
						cluster.addMarker(mp); isIn = true; break;
					}
				}
				// 如果没在任何范围内，自己单独形成一个自定义marker。在和后面的marker进行比较
				if (!isIn) {
					clustersMarker.add(new MarkerImageView(PointAggregationAty.this, mp, projection, 120,clustersMarker.size()));// 80=相距多少才聚合
				}
			}
		}
		// 设置聚合点的位置和icon
		for (MarkerImageView mmc : clustersMarker) {
			mmc.setpositionAndIcon();
		}
		aMap.clear();
		// 重新添加 marker
		for (MarkerImageView cluster : clustersMarker) {
		Marker marker=aMap.addMarker(cluster.getOptions());
		marker.setTitle(cluster.getOptions().getTitle());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_location:
				// 先销毁定位
				break;
		}
	}




}
