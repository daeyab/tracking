package com.daeyab.tracking

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.daeyab.tracking.MtValues.CUR_LAT
import com.daeyab.tracking.MtValues.CUR_LNG
import com.daeyab.tracking.MtValues.MT_LAT
import com.daeyab.tracking.MtValues.MT_LNG
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
import kotlinx.android.synthetic.main.activity_show_mountain.*
import kotlinx.android.synthetic.main.activity_start_hiking.*
import org.jetbrains.anko.toast
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class StartHikingActivity : AppCompatActivity(), OnMapReadyCallback {


    lateinit var locationSource : FusedLocationSource
    val LOCATION_PERMISSION_REQUEST_CODE=1000
    lateinit var naverMap:NaverMap
    var time =0
    lateinit var timerTask1:Timer
    lateinit var timerTask2:Timer
    var mapInitialied=false
    var ZOOM=16.0
    var startDate=Date(System.currentTimeMillis())
    var cam_lat=0.0
    var cam_lng=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_hiking)
//        val a=intent.getBundleExtra("facility")
 //       Log.d("adadad",a.toString())
        initMap()
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun init() {
        locationlog.clear()
        getCurrentLocation()
        cam_lat= CUR_LAT
        cam_lng= CUR_LNG
        titleTxt.text="${MT_NAME} 등산 중"
        val simpleDateFormat=SimpleDateFormat("yyyy년 MM월 dd일 hh시 mm분")
        val startTimeInfo=simpleDateFormat.format(startDate)
        startTimeTxt.text="시작 시각 : $startTimeInfo"
        runtime()
        recordLocations()
        hikingCancelBtn.setOnClickListener{
            timerTask1.cancel()
            timerTask2.cancel()
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        hikingFinishBtn.setOnClickListener {
            val endDate= Date(System.currentTimeMillis())
            val simpleDateFormat=SimpleDateFormat("yyyy년 MM월 dd일 hh시 mm분")
            val endTimeInfo=simpleDateFormat.format(endDate)
            val diff=endDate.time-startDate.time
            val hikingTimeInfo="${TimeUnit.MILLISECONDS.toHours(diff).toInt()}시간 ${TimeUnit.MILLISECONDS.toMinutes(diff).toInt()%60}분 ${TimeUnit.MILLISECONDS.toSeconds(diff).toInt()%60}초"
            //val TimeFormat=SimpleDateFormat("yyyy년 MM월 dd일 hh시 mm분")
            //startTimeTxt.text="시작 시각 : $startTimeInfo"

            val intent=Intent(this,FinishedHikingActivity::class.java)
            intent.putExtra("startTime",startTimeInfo)
            intent.putExtra("endTime",endTimeInfo)
            intent.putExtra("hikingTime", hikingTimeInfo)
            intent.putExtra("date",startTimeInfo)
            timerTask1.cancel()
            timerTask2.cancel()
            startActivity(intent)
        }
        SmsSOSBtn.setOnClickListener{
        getCurrentLocation()
        var mmsnum=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("smsNum","010-0000-0000")
        var mmstxt=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("smsText","")
        val txt="""
                    latitute : ${CUR_LAT} longitude : ${CUR_LNG}
                    ${mmstxt}
                """.trimIndent()
        val msg= Uri.parse("sms:"+mmsnum)
        val msgIntent=Intent(Intent.ACTION_SENDTO,msg)
        msgIntent.putExtra("sms_body",txt)
  //      Log.d("dbdb",txt)
        startActivity(msgIntent)
    }
    CallSOSBtn.setOnClickListener{
        var telnum=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("callNum","01000000000")
        val tel= Uri.parse("tel:"+ telnum)
        val callIntent=Intent(Intent.ACTION_DIAL,tel)
        startActivity(callIntent)
    }
    }


    private fun recordLocations() {
        try{
            timerTask1= kotlin.concurrent.timer(period = 10000){
                getCurrentLocation()
                Log.d("tlrks","${CUR_LAT}  ${CUR_LNG}")
                locationlog.add(LatLng(CUR_LAT, CUR_LNG))
                runOnUiThread{
                    if(mapInitialied)
                        onMapReady(naverMap)
                }
            }
        }
        catch (e:Exception){
            toast(e.message.toString())
        }
    }

    override fun onBackPressed(){
        toast("취소 버튼을 통해 등산을 종료하실 수 있습니다.")
        null
    }



        private fun getCurrentLocation(){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener {
                    location ->
                CUR_LAT=location.latitude
                CUR_LNG=location.longitude
                //    toast("lat:"+lat.toString()+" lng:"+lng.toString()+" alt:"+alt.toString())
            }

        }
        private fun runtime() {
            timerTask2= kotlin.concurrent.timer(period = 1000){
                time++
                val sec=time%60
                val min=time/60
                val hour=time/3600
                runOnUiThread{
                    Log.d("tlrks","${hour}시간 ${min}분 ${sec}초")
                    hikingTimeTxt.text="진행 시간 : ${hour}시간 ${min}분 ${sec}초"
                }
            }
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

            val factory : CRSFactory = CRSFactory()
            val beforeCrs=factory.createFromName("EPSG:5186")
            val afterCrs=factory.createFromName("EPSG:5179")
            val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)

            naverMap.cameraPosition=CameraPosition(LatLng(cam_lat, cam_lng),ZOOM)
            naverMap.addOnCameraChangeListener(){ i: Int, b: Boolean ->
                ZOOM=naverMap.cameraPosition.zoom
                naverMap.cameraPosition
            }

            if(locationlog.size>2){
                val path= PathOverlay()
                path.color=Color.RED
                path.coords=locationlog
                path.map=naverMap
            }

            for(i in 0 until MtFacility.size) {
                val type = MtFacility[i].type
                if (type.contains("훼손") || type.contains("분기"))
                    continue
                val marker = Marker()
                marker.position = LatLng(MtFacility[i].lat, MtFacility[i].lng)
                when (type) {
                    "시종점" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.start)
                    }
                    "벤치", "목벤치" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.bench)
                    }
                    "이정표", "안내","안내판", "지도", "안내문", "관리사무소" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.info)
                    }
                    "송전탑"->{
                        marker.icon= OverlayImage.fromResource(R.drawable.elec)
                    }
                    "가로등" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.light)
                    }
                    "음수대", "약수터" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.water)
                    }
                    "정자" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.gazebo)
                    }
                    "재난기구" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.emergency)
                    }
                    "다리" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.bridge)
                    }
                    "화장실" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.toilet)
                    }
                    "전망", "조망점", "전경", "등산로전경" -> {
                        marker.icon = OverlayImage.fromResource(R.drawable.photo)
                    }
                    else -> {
                        if (type.contains("계단")) {
                            marker.icon = OverlayImage.fromResource(R.drawable.stairs)
                        } else if (type.contains("운동")) {
                            marker.icon = OverlayImage.fromResource(R.drawable.workout)
                        } else {
                            marker.icon =
                                OverlayImage.fromResource(R.drawable.ic_baseline_fiber_manual_record_24)
                            marker.captionTextSize = 10f
                            marker.captionText = type
                        }
                    }
                }
                marker.minZoom = 14.0
                marker.width=40
                marker.height=40
                marker.map = naverMap
                marker.setOnClickListener{ overlay ->  toast(type); true }

            }
            for(i in 0 until MtRoute.size){
               // Log.d("dbdbdbdb","mtroute size: "+MtRoute.size)
                val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)

                val path= PathOverlay()
                val lst= mutableListOf<LatLng>()

                for(j in 0 until MtRoute[i].path.size){
                    val pathList=MtRoute[i].path[j]
                    val x=pathList.first
                    val y=pathList.second
                    val beforeCoord= ProjCoordinate(x,y)
                    val afterCoord= ProjCoordinate()
                    transform.transform(beforeCoord,afterCoord)
                    //   path.coords.add(Utmk(afterCoord.x,afterCoord.y).toLatLng())


                    lst.add(Utmk(afterCoord.x,afterCoord.y).toLatLng())
                }
                // Log.d("dbdbdbdb","lst size: "+lst.size)
                when(MtRoute[i].level){
                    "쉬움"->path.color= Color.GREEN
                    "중간"->path.color= Color.MAGENTA
                    "어려움"->path.color=Color.RED
                }
                path.coords=lst
                path.map=naverMap
            }



            this.naverMap=naverMap
            mapInitialied=true
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
    }

