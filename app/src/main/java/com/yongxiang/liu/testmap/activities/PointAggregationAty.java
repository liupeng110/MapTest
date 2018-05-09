package com.yongxiang.liu.testmap.activities;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.yongxiang.liu.testmap.BuildConfig;
import com.yongxiang.liu.testmap.R;
import com.yongxiang.liu.testmap.utils.ScreenUtils;

//import org.xutils.*;

public class PointAggregationAty extends Activity implements OnClickListener,
		OnCameraChangeListener, OnMarkerClickListener {
	/**  地图view  */
	private MapView mapView;
	/**  高德amap */
	private AMap aMap;
	private int screenHeight;// 屏幕高度(px)
	private int screenWidth;// 屏幕宽度(px)

	/**  定位  */
//	private LocationManagerProxy aMapLocManager = null;
	/** 所有的marker  */
	private ArrayList<MarkerOptions> markerOptionsListall = new ArrayList<MarkerOptions>();
	/**  视野内的marker  */
	private ArrayList<MarkerOptions> markerOptionsListInView = new ArrayList<MarkerOptions>();
	private ImageView img_location;
    Thread  thread;
    Runnable runnable;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point_aggregation);
//		org.xutils.x.Ext.init(getApplication());
//		org.xutils.x.Ext.setDebug(BuildConfig.DEBUG);
		initView();
		initEvent();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = ScreenUtils.getScreenWidth(this);
		screenHeight = ScreenUtils.getScreenHeight(this);

		// 方法必须重写
		mapView.onCreate(savedInstanceState);
//		if (aMapLocManager == null) {
			// 打开定位
//			aMapLocManager = LocationManagerProxy .getInstance(PointAggregationAty.this);
			/**
			 * mAMapLocManager.setGpsEnable(false);//
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式 ，第一个参数是定位provider，第二个参数时间最短是2000毫秒
			 * ，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
//			aMapLocManager.requestLocationData(
//					LocationProviderProxy.AMapNetwork, 2000, 10,
//					PointAggregationAty.this);

//		}

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

	private void initEvent() {
		img_location.setOnClickListener(this);
	}

	private void initView() {
		mapView = (MapView) findViewById(R.id.map);
		img_location = (ImageView) findViewById(R.id.img_location);
	}

	private void initDatas() {
		for(int i=0;i<1000;i++){
//			runnable=new Runnable() {
//				@Override public void run() {
					Random r = new Random();
					double lat = (290000 + r.nextInt(30000)) / 10000.0D;
					double lng = (1120000 + r.nextInt(30000)) / 10000.0D;
					addDate(lat,lng);
				}
//			};

//		}


//		thread=new Thread(runnable);
//		thread.start();

	}

	// 添加临时数据
	private void addDate(double latitude,double longitude) {
		LatLng latLng = new LatLng(latitude, longitude);
		markerOptionsListall.add(new MarkerOptions()
				.position(latLng)
				.icon(BitmapDescriptorFactory .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO marker点击监听
		return false;
	}

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO 地图改变时的监听

	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {// 地图改变完之后的监听
		resetMarks();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 获取视野内的marker 根据聚合算法合成自定义的marker 显示视野内的marker
	 */
	private void resetMarks() {
		// 开始刷新
		Projection projection = aMap.getProjection();
		Point p = null;
		markerOptionsListInView.clear();
		// 获取在当前视野内的marker;提高效率
		for (MarkerOptions mp : markerOptionsListall) {
			p = projection.toScreenLocation(mp.getPosition());
			if (p.x < 0 || p.y < 0 || p.x > screenWidth || p.y > screenHeight) {
				// 不添加到计算的列表中
			} else {
				markerOptionsListInView.add(mp);
			}
		}
		// 自定义的聚合类MyMarkerCluster
		ArrayList<MarkerImageView> clustersMarker = new ArrayList<MarkerImageView>();
		for (MarkerOptions mp : markerOptionsListInView) {
			if (clustersMarker.size() == 0) {
				// 添加一个新的自定义marker
				clustersMarker.add(new MarkerImageView( PointAggregationAty.this, mp, projection, 80));// 80=相距多少才聚合
			} else {
				boolean isIn = false;
				for (MarkerImageView cluster : clustersMarker) {
					// 判断当前的marker是否在前面marker的聚合范围内 并且每个marker只会聚合一次。
					if (cluster.getBounds().contains(mp.getPosition())) {
						cluster.addMarker(mp);
						isIn = true;
						break;
					}
				}
				// 如果没在任何范围内，自己单独形成一个自定义marker。在和后面的marker进行比较
				if (!isIn) {
					clustersMarker.add(new MarkerImageView(
							PointAggregationAty.this, mp, projection, 80));// 80=相距多少才聚合
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
			aMap.addMarker(cluster.getOptions());
		}
	}



	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.img_location:
				// 先销毁定位

				break;
		}
	}




}
