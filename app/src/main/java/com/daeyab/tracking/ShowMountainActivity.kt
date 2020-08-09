package com.daeyab.tracking

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.daeyab.tracking.MtValues.CUR_LAT
import com.daeyab.tracking.MtValues.CUR_LNG
import com.daeyab.tracking.MtValues.MT_HEIGHT
import com.daeyab.tracking.MtValues.MT_LAT
import com.daeyab.tracking.MtValues.MT_LNG
import com.daeyab.tracking.MtValues.MT_LOCATION
import com.daeyab.tracking.MtValues.MT_MID
import com.daeyab.tracking.MtValues.MT_NAME
import com.daeyab.tracking.MtValues.MtFacility
import com.daeyab.tracking.MtValues.MtRoute
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
import jxl.Workbook
import kotlinx.android.synthetic.main.activity_show_mountain.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import java.io.InputStream
import java.net.URL
import kotlin.math.sin

class ShowMountainActivity : AppCompatActivity(), OnMapReadyCallback,View.OnClickListener {

    val KEY_VALUE="YjHJMtVPZA6ee3oHe8v8l4V9%2F4%2B0uRskWQdWPzrLasgunvsAXUUb96Uqg3ehMXOEefZ1y0j%2BGH2G54zuD7%2BLJQ%3D%3D"
    var URL_VALUE="http://apis.data.go.kr/1400000/service/cultureInfoService/mntInfoOpenAPI?serviceKey=${KEY_VALUE}&searchWrd="
    lateinit var locationSource : FusedLocationSource
    val LOCATION_PERMISSION_REQUEST_CODE=1000
    lateinit var naverMap:NaverMap


    lateinit var fusedLocationClient: FusedLocationProviderClient

    val storeList= mutableListOf<Place>()
    val cafeList= mutableListOf<Place>()
    val hospitalList= mutableListOf<Place>()
    val drugList= mutableListOf<Place>()
    val restaurantList= mutableListOf<Place>()
    val toiletList= mutableListOf<Place>()


    val storeMarker= mutableListOf<Marker>()
    val cafeMarker= mutableListOf<Marker>()
    val hospitalMarker= mutableListOf<Marker>()
    val drugMarker= mutableListOf<Marker>()
    val restaurantMarker= mutableListOf<Marker>()
    val toiletMarker= mutableListOf<Marker>()


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_mountain)

        startSearchingBtn.setOnClickListener(this)
        startHikingBtn.setOnClickListener(this)
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        val noInfo=intent.getBooleanExtra("noInfo",false) //심볼로 왔는데 자료가 없는 것
        Log.d("final","${MT_LAT}, ${MT_LNG}")


        if(noInfo){
            MtAddressTxt.text="이 산은 등산로가 제공되지 않습니다."
            MtAddressTxt.textAlignment= View.TEXT_ALIGNMENT_CENTER
            MtNameTxt.text=MT_NAME
        }
        else{
            MtNameTxt.text=MT_NAME
            MtHeightTxt.text= MT_HEIGHT+"m"
            MtAddressTxt.text=MT_LOCATION
            readMt()
        }

        initPlaces()
        initMap()
    //    initParsing()


        searchRouteBtn.setOnClickListener(this)

    }

    private fun initPlaces() {
        val task=GooglePlaceAsyncTask(this)
        val URL_store="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${MT_LAT},${MT_LNG}&radius=3000&language=ko&rankBy=distance&type=convenience_store&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
        val URL_hospital="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${MT_LAT},${MT_LNG}&radius=3000&language=ko&rankBy=distance&type=doctor&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
        val URL_cafe="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${MT_LAT},${MT_LNG}&radius=3000&language=ko&rankBy=distance&type=cafe&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
        val URL_drug="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${MT_LAT},${MT_LNG}&radius=3000&language=ko&rankBy=distance&keyword=약국&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
        val URL_rest="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${MT_LAT},${MT_LNG}&radius=3000&language=ko&rankBy=distance&type=restaurant&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
        val URL_toilet="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${MT_LAT},${MT_LNG}&radius=3000&language=ko&rankBy=distance&keyword=화장실&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
        task.execute(URL(URL_store),URL(URL_hospital),URL(URL_cafe),URL(URL_drug),(URL(URL_rest)), URL(URL_toilet))
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

    fun distance(lat1:Double, lon1:Double, lat2:Double, lon2:Double):Double {

        val theta = lon1 - lon2;
        var dist = sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    fun deg2rad(deg:Double):Double {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    fun rad2deg(rad:Double):Double {
        return (rad * 180 / Math.PI);
    }


    override fun onMapReady(naverMap: NaverMap) {
        //NaverMap 객체가 준비되면 실행되는 콜백 메소드
      //  readMt()

        naverMap.mapType= NaverMap.MapType.Terrain
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN,true)
        val uiSettings=naverMap.uiSettings
        //       uiSettings.isScaleBarEnabled=true
        uiSettings.isLocationButtonEnabled=true
        uiSettings.isCompassEnabled=true
        naverMap.locationSource=locationSource
        naverMap.locationTrackingMode= LocationTrackingMode.None

        val cameraPosition=CameraPosition(LatLng(MT_LAT,MT_LNG),13.00)
        naverMap.cameraPosition=cameraPosition

 //       toast(MtFacility.size.toString())

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

        val factory : CRSFactory = CRSFactory()
        val beforeCrs=factory.createFromName("EPSG:5186")
        val afterCrs=factory.createFromName("EPSG:5179")
        val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)
        for(i in 0 until MtRoute.size){
            val path= PathOverlay()
            val lst= mutableListOf<LatLng>()

            for(j in 0 until MtRoute[i].path.size){
                val pathList=MtRoute[i].path[j]
                val beforeCoord= ProjCoordinate(pathList.first,pathList.second)
                val afterCoord= ProjCoordinate()
                transform.transform(beforeCoord,afterCoord)
                lst.add(Utmk(afterCoord.x,afterCoord.y).toLatLng())
            }
             when(MtRoute[i].level){
                "쉬움"->path.color= Color.GREEN
                "중간"->path.color= Color.MAGENTA
                 "어려움"->path.color=Color.RED
             }
            path.coords=lst
            path.map=naverMap

    //        Log.d("Wls","제발여긴: "+ lst.toString())
        }

        Log.d("Wls","제발여긴: "+ MtRoute.size.toString()+","+ MtFacility.size.toString())

        storeCheckBox.setOnClickListener() {
            if(storeCheckBox.isChecked){
                for(i in 0 until storeList.size) {
                    storeMarker.add(Marker())
                    storeMarker[i].position = LatLng(storeList[i].lat, storeList[i].lng)
                    storeMarker[i].iconTintColor = Color.GREEN
                    storeMarker[i].captionTextSize = 10f
                    storeMarker[i].width = 40
                    storeMarker[i].height = 40
                    storeMarker[i].map = naverMap
                    storeMarker[i].captionText=storeList[i].name
                    storeMarker[i].icon= OverlayImage.fromResource(R.drawable.store)
                }
            }
            else{
                for(i in 0 until storeMarker.size)
                    storeMarker[i].map=null
                storeMarker.clear()
            }
        }
        cafeCheckBox.setOnClickListener() {
            if(cafeCheckBox.isChecked){
                for(i in 0 until cafeList.size) {
                    cafeMarker.add(Marker())
                    cafeMarker[i].position = LatLng(cafeList[i].lat, cafeList[i].lng)
                    cafeMarker[i].iconTintColor = resources.getColor(R.color.Brown)
                    cafeMarker[i].captionTextSize = 10f
                    cafeMarker[i].icon= OverlayImage.fromResource(R.drawable.cafe)
                    cafeMarker[i].width = 40
                    cafeMarker[i].height = 40
                    cafeMarker[i].map = naverMap
                    cafeMarker[i].captionText=cafeList[i].name
                }
            }
            else{
                for(i in 0 until cafeMarker.size)
                    cafeMarker[i].map=null
                cafeMarker.clear()
            }
        }
        drugCheckBox.setOnClickListener() {
            if(drugCheckBox.isChecked){
                for(i in 0 until drugList.size) {
                    drugMarker.add(Marker())
                    drugMarker[i].icon= OverlayImage.fromResource(R.drawable.drug_store)
                    drugMarker[i].position = LatLng(drugList[i].lat, drugList[i].lng)
                    drugMarker[i].iconTintColor = resources.getColor(R.color.Purple)
                    drugMarker[i].captionTextSize = 10f
                    drugMarker[i].width = 40
                    drugMarker[i].height = 40
                    drugMarker[i].map = naverMap
                    drugMarker[i].captionText=drugList[i].name
                }
            }
            else{
                for(i in 0 until drugMarker.size)
                    drugMarker[i].map=null
                drugMarker.clear()
            }
        }
        hospitalCheckBox.setOnClickListener() {
            if(hospitalCheckBox.isChecked){
                for(i in 0 until hospitalList.size) {
                    hospitalMarker.add(Marker())
                    hospitalMarker[i].position = LatLng(hospitalList[i].lat, hospitalList[i].lng)
                    hospitalMarker[i].iconTintColor = Color.RED
                    hospitalMarker[i].captionTextSize = 10f
                    hospitalMarker[i].width = 40
                    hospitalMarker[i].height = 40
                    hospitalMarker[i].icon= OverlayImage.fromResource(R.drawable.hospital)
                    hospitalMarker[i].map = naverMap
                    hospitalMarker[i].captionText=hospitalList[i].name
                }
            }
            else{
                for(i in 0 until hospitalMarker.size)
                    hospitalMarker[i].map=null
                hospitalMarker.clear()
            }
        }
        restaurantCheckBox.setOnClickListener() {
            if(restaurantCheckBox.isChecked){
                for(i in 0 until restaurantList.size) {
                    restaurantMarker.add(Marker())
                    restaurantMarker[i].position = LatLng(restaurantList[i].lat, restaurantList[i].lng)
                    restaurantMarker[i].iconTintColor = resources.getColor(R.color.Orange)
                    restaurantMarker[i].captionTextSize = 10f
                    restaurantMarker[i].width = 40
                    restaurantMarker[i].icon= OverlayImage.fromResource(R.drawable.food)
                    restaurantMarker[i].height = 40
                    restaurantMarker[i].map = naverMap
                    restaurantMarker[i].captionText=restaurantList[i].name
                }
            }
            else{
                for(i in 0 until restaurantMarker.size)
                    restaurantMarker[i].map=null
                restaurantMarker.clear()
            }
        }

        toiletCheckBox.setOnClickListener() {
            if(toiletCheckBox.isChecked){
                for(i in 0 until toiletList.size) {
                    toiletMarker.add(Marker())
                    toiletMarker[i].position = LatLng(toiletList[i].lat, toiletList[i].lng)
                    toiletMarker[i].iconTintColor = Color.BLUE
                    toiletMarker[i].captionTextSize = 10f
                    toiletMarker[i].width = 40
                    toiletMarker[i].icon= OverlayImage.fromResource(R.drawable.bathroom)
                    toiletMarker[i].height = 40
                    toiletMarker[i].map = naverMap
                    toiletMarker[i].captionText=toiletList[i].name
                }
            }
            else{
                for(i in 0 until toiletMarker.size)
                    toiletMarker[i].map=null
                toiletMarker.clear()
            }
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
        }
    }



    fun setUpTxt(str:String)=runOnUiThread {
        val mountainDBHelper=MountainDBHelper(this)
        mountainDBHelper.updateHeight(MT_MID,str)
    //    MtHeightTxt.text=str+"m"
    }


    fun readMt() {
        MtRoute.clear()
        MtFacility.clear()
        val assetManager = resources.assets
        val ROUTE_FILE_NAME = "geoson/PMNTN_" + MT_NAME + "_" + MT_MID + ".json"
        try {
            val routeInputStream = assetManager.open(ROUTE_FILE_NAME)
            val routeJsonString = routeInputStream.bufferedReader().use { it.readText()}
            val routeJsonObject= JSONObject(routeJsonString)
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
             MtFacility.addAll(facilityDBHelper.getFacilities(MT_MID))
//            Log.d("Wls","SUCCESS: "+ MtRoute.size.toString()+","+ MtFacility.size.toString())
        }
        catch (e: Exception) {
   //         Log.d("adadad","exception msg: "+e.message.toString())
        }
    }


    fun getResult( result: MutableList<Pair<Int,Place>>)
    {
        for(i in 0 until  result.size){
            when(result[i].first){
                0->storeList.add(result[i].second)
                1->hospitalList.add(result[i].second)
                2->cafeList.add(result[i].second)
                3->drugList.add(result[i].second)
                4->restaurantList.add(result[i].second)
                5->toiletList.add(result[i].second)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.searchRouteBtn->{
                val intent= Intent(this,ShowRouteActivity::class.java)
     //           intent.putExtra("lat", LAT)
      //          intent.putExtra("lng",LNG)
       //         intent.putExtra("mtlat",MT_LAT)
        //        intent.putExtra("mtlng",MT_LNG)
         //       intent.putExtra("mtname",MT_NAME)
                startActivity(intent)
            }
            R.id.startHikingBtn->{
                getCurrentLocation()
                val dis=distance(CUR_LAT, CUR_LNG, MT_LAT, MT_LNG)
                if(dis>10)
                    toast("산과 10km 이내여야 합니다.")
                else{
                    val intent=Intent(this,StartHikingActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.startSearchingBtn->{
                val intent=Intent(Intent.ACTION_VIEW,Uri.parse("https://search.naver.com/search.naver?sm=top_hty&fbm=0&ie=utf8&query="+MT_NAME))
                startActivity(intent);
            }
        }
    }


}
