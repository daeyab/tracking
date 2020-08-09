package com.daeyab.tracking

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.daeyab.tracking.MtValues.CUR_LAT
import com.daeyab.tracking.MtValues.CUR_LNG
import com.daeyab.tracking.MtValues.MT_LAT
import com.daeyab.tracking.MtValues.MT_LNG
import com.daeyab.tracking.MtValues.MT_MID
import com.daeyab.tracking.MtValues.MT_NAME
import com.daeyab.tracking.MtValues.MtFacility
import com.daeyab.tracking.MtValues.MtRoute
import com.daeyab.tracking.MtValues.locationlog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Utmk
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_finished_hiking.*
import kotlinx.android.synthetic.main.activity_show_mountain.*
import kotlinx.android.synthetic.main.activity_start_hiking.*
import kotlinx.android.synthetic.main.activity_start_hiking.hikingTimeTxt
import kotlinx.android.synthetic.main.activity_start_hiking.startTimeTxt
import kotlinx.android.synthetic.main.activity_start_hiking.titleTxt
import org.jetbrains.anko.toast
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class FinishedHikingActivity : AppCompatActivity(), OnMapReadyCallback,View.OnClickListener {


    lateinit var locationSource : FusedLocationSource
    val LOCATION_PERMISSION_REQUEST_CODE=1000
    lateinit var naverMap:NaverMap
    var time =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finished_hiking)
//        val a=intent.getBundleExtra("facility")
        //       Log.d("adadad",a.toString())
        initMap()
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)
        init()
        toHomeBtn.setOnClickListener(this)
        toMainBtn.setOnClickListener(this)
    }

    private fun init() {
        val time=intent.getStringExtra("hikingTime")
        hikingTimeTxt.text="소요 시간 : "+time
        val start=intent.getStringExtra("startTime")
        startTimeTxt.text="시작 시각 : "+start
        endTimeTxt.text="종료 시각 : "+intent.getStringExtra("endTime")
       finishedTxt.text="${MT_NAME} 등산완료"
        val hikingLogDBHelper=HikingLogDBHelper(this)
        val mountainDBHelper=MountainDBHelper(this)
        mountainDBHelper.updateVisited(MT_MID)
        hikingLogDBHelper.create()
        val result=hikingLogDBHelper.insertMountain(MT_NAME,start,time)
     }

    override fun onBackPressed() {
        val intent=Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    lateinit var fusedLocationClient: FusedLocationProviderClient

    fun initMap(){
        val fm = supportFragmentManager
        var mapFragment = fm.findFragmentById(R.id.hikingMap) as MapFragment
        if(mapFragment==null){
            mapFragment= MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map,mapFragment).commit()
        }
        mapFragment.getMapAsync(this)
        locationSource= FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

    }
    override fun onMapReady(naverMap: NaverMap) {
        //NaverMap 객체가 준비되면 실행되는 콜백 메소드

        naverMap.mapType= NaverMap.MapType.Terrain
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN,true)
        val uiSettings=naverMap.uiSettings
        //       uiSettings.isScaleBarEnabled=true
        uiSettings.isLocationButtonEnabled=true
        uiSettings.isCompassEnabled=true
        naverMap.locationSource=locationSource
        naverMap.locationTrackingMode= LocationTrackingMode.Follow

        naverMap.cameraPosition=CameraPosition(LatLng(CUR_LAT, CUR_LNG),14.0)

        if(locationlog.size>2){
            val path= PathOverlay()
            path.color=Color.RED
            path.coords=locationlog
            path.map=naverMap
        }
        this.naverMap=naverMap
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(locationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)){
            if(!locationSource.isActivated){
                //권한 거부됨
                naverMap.locationTrackingMode= LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.toHomeBtn->{
                val intent=Intent(this,ShowRouteActivity::class.java)
                intent.putExtra("home",true)
                startActivity(intent)
            }
            R.id.toMainBtn->{
                startActivity(Intent(this,MainActivity::class.java))
            }
        }
    }
}

