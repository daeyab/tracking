package com.daeyab.tracking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.daeyab.tracking.MtValues.MT_HEIGHT
import com.daeyab.tracking.MtValues.MT_LAT
import com.daeyab.tracking.MtValues.MT_LNG
import com.daeyab.tracking.MtValues.MT_LOCATION
import com.daeyab.tracking.MtValues.MT_MID
import com.daeyab.tracking.MtValues.MT_NAME
import com.daeyab.tracking.MtValues.MtFacility
import com.daeyab.tracking.MtValues.MtRoute
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Utmk
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_search_mountain.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate


class SearchMountainActivity:AppCompatActivity(), OnMapReadyCallback, View.OnClickListener{

    var INDEX=0
    var LISTSIZE=1
    val LOCATION_PERMISSION_REQUEST_CODE=1000


    lateinit var searchedMtInfo: MutableList<Mountain>
    lateinit var locationSource : FusedLocationSource
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var naverMap:NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_mountain)
        init()
        readMt()
        initMap()
        searchNextBtn.setOnClickListener(this)
        searchPreviousBtn.setOnClickListener(this)
        showMtInfoBtn.setOnClickListener(this)

    }

    private fun init() {
        val mtName=intent.getStringExtra("searchedName")
        MT_NAME=mtName
        val mountainDBHelper=MountainDBHelper(this)
        searchedMtInfo=mountainDBHelper.getSerachedRecord(MT_NAME)
        getInfoFromList()
        searchMtName.text=MT_NAME
        searchMtAddress.text=MT_LOCATION
        searchHeadText.text="${INDEX} / ${searchedMtInfo.size}"
        LISTSIZE=searchedMtInfo.size
        if(LISTSIZE==0){
            toast("죄송합니다. 해당 산을 찾을 수 없습니다.")
            startActivity(Intent(this,MainActivity::class.java))
        }
    }

     fun getInfoFromList() {
         if(searchedMtInfo.size==0)
             return
         MT_MID=searchedMtInfo[INDEX].mID
         MT_LOCATION=searchedMtInfo[INDEX].location
         MT_NAME=searchedMtInfo[INDEX].name
         MT_LAT=searchedMtInfo[INDEX].lat
         MT_HEIGHT =searchedMtInfo[INDEX].height
         MT_LNG=searchedMtInfo[INDEX].lng
    }

    fun readMt() {
        MtRoute.clear()
        MtFacility.clear()

        val assetManager = resources.assets
        val ROUTE_FILE_NAME = "geoson/PMNTN_" + MT_NAME + "_" + MT_MID + ".json"
        try {
            val routeInputStream = assetManager.open(ROUTE_FILE_NAME)
            val routeJsonString = routeInputStream.bufferedReader().use { it.readText()}
            val routeJsonObject=JSONObject(routeJsonString)
            val routeJsonArray=routeJsonObject.getJSONArray("features")
            for(i in 0 until routeJsonArray.length()){
                val paths=routeJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONArray("paths").getJSONArray(0)
                val pathsList= mutableListOf<Pair<Double,Double>>()
                for( j in 0 until paths.length()){
                    pathsList.add(Pair(paths.getJSONArray(j).get(0).toString().toDouble(),paths.getJSONArray(j).get(1).toString().toDouble()))
                }
                val level=routeJsonArray.getJSONObject(i).getJSONObject("attributes").get("PMNTN_DFFL")
                val name=routeJsonArray.getJSONObject(i).getJSONObject("attributes").get("PMNTN_NM")
                val rID=routeJsonArray.getJSONObject(i).getJSONObject("attributes").get("FID")
                val mID=routeJsonArray.getJSONObject(i).getJSONObject("attributes").get("MNTN_ID")
                MtRoute.add(Route(rID = rID.toString(),mID = mID.toString(),level = level.toString(),name = name.toString(),path = pathsList))
                }
                val facilityDBHelper=FacilityDBHelper(this)
                val result=facilityDBHelper.getFacilities(MT_MID)
                Log.d("asdasd","SIZE"+result.size.toString())
                MtFacility.addAll(facilityDBHelper.getFacilities(MT_MID))
             }
        catch (e: Exception) {
            Log.d("adadad","exception msg: "+e.message.toString())

        }
    }


    fun initMap(){
        val fm = supportFragmentManager
        var mapFragment = fm.findFragmentById(R.id.map) as MapFragment
        if(mapFragment==null){
            mapFragment= MapFragment.newInstance()
            fm.beginTransaction().add(R.id.map,mapFragment).commit()
        }
        mapFragment.getMapAsync(this)
        locationSource= FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onMapReady(naverMap: NaverMap) {
        //NaverMap 객체가 준비되면 실행되는 콜백 메소드
        getInfoFromList()
//        readMt()
        if(searchedMtInfo.size==0)
            return

        naverMap.mapType= NaverMap.MapType.Terrain
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN,true)
        val uiSettings=naverMap.uiSettings
        //       uiSettings.isScaleBarEnabled=true
        uiSettings.isLocationButtonEnabled=true
        uiSettings.isCompassEnabled=true
        naverMap.locationSource=locationSource
        naverMap.locationTrackingMode= LocationTrackingMode.None

        val factory : CRSFactory = CRSFactory()
        val beforeCrs=factory.createFromName("EPSG:5186")
        val afterCrs=factory.createFromName("EPSG:5179")
        val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)

        searchMtName.text=searchedMtInfo[INDEX].name
        searchMtAddress.text=searchedMtInfo[INDEX].location
        searchHeadText.text="(${INDEX+1}/${searchedMtInfo.size})"

        val cameraPosition:CameraPosition= CameraPosition(LatLng(MT_LAT, MT_LNG),13.0)
        naverMap.cameraPosition=cameraPosition

        this.naverMap=naverMap

        Log.d("WLsakr",MtFacility.size.toString()+"?")
        for(i in 0 until MtFacility.size) {
            Log.d("WLsakr",MtFacility[i].toString())
            val type = MtValues.MtFacility[i].type
            if (type.contains("훼손") || type.contains("분기"))
                continue
            val marker = Marker()
            marker.position = LatLng(MtValues.MtFacility[i].lat, MtValues.MtFacility[i].lng)
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
            Log.d("dbdbdbdb","mtroute size: "+MtRoute.size)
            val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)

            val name=MtRoute[i].name
            val level=MtRoute[i].level
            val path=PathOverlay()
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
            Log.d("dbdbdbdb","lst size: "+lst.size)
            when(MtRoute[i].level){
                "쉬움"->path.color=Color.GREEN
                "중간"->path.color=Color.MAGENTA
                "어려움"->path.color=Color.RED

            }
            path.coords=lst
            path.map=naverMap
        }

        //     searchMtName.text=searchedInfo[INDEX].name
        //     searchMtAddress.text=searchedInfo[INDEX].address
        searchHeadText.text="(${INDEX+1}/${searchedMtInfo.size})"

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
            R.id.searchNextBtn->{
                if(INDEX==LISTSIZE-1)
                    INDEX=0;
                else
                    INDEX++
                getInfoFromList()
                readMt()
                onMapReady(naverMap)
            }
            R.id.searchPreviousBtn->{
                if(INDEX==0)
                    INDEX=LISTSIZE-1;
                else
                    INDEX--
                getInfoFromList()
                readMt()
                onMapReady(naverMap)
            }
            R.id.showMtInfoBtn->{
                val intent=Intent(this,ShowMountainActivity::class.java)
                Log.d("final","${MT_LAT}, ${MT_LNG}")
                startActivity(intent)
            }
        }
    }
}