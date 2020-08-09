package com.daeyab.tracking

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.daeyab.tracking.MtValues.CUR_LAT
import com.daeyab.tracking.MtValues.CUR_LNG
import com.daeyab.tracking.MtValues.MT_HEIGHT
import com.daeyab.tracking.MtValues.MT_LAT
import com.daeyab.tracking.MtValues.MT_LNG
import com.daeyab.tracking.MtValues.MT_LOCATION
import com.daeyab.tracking.MtValues.MT_MID
import com.daeyab.tracking.MtValues.MT_NAME
import com.daeyab.tracking.MtValues.getDistance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Utmk
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import jxl.Workbook
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.android.synthetic.main.weather_toolbar.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import org.osgeo.proj4j.BasicCoordinateTransform
import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.ProjCoordinate
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener,NavigationView.OnNavigationItemSelectedListener{

    val LOCATION_PERMISSION_REQUEST_CODE=1000

//    var SMS__NUM="010-0000-0000"

    lateinit var locationSource : FusedLocationSource
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var naverMap : NaverMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initDB()

        //   addFacility()
       //   addExcel()

        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        initMap()
        initToggle()
        //옆 메뉴바 구현
        mainSearchBtn.setOnClickListener (this)
        SmsSOSBtn.setOnClickListener(this)
        CallSOSBtn.setOnClickListener(this)
        //nextBtn.setOnClickListener(this)
  //      getMountainInfo.setOnClickListener(this)
      //  initWeather()
        // 파일에서 읽어와서 firebase에 넣는 함수
     //   readExcel()
    //    readExcel2()

    }

    private fun initWeather() {
        val mFormat=SimpleDateFormat("yyyy년 mm월 dd일 ")
        Log.d("weather",mFormat.toString())
        val task=WeatherAsyncTask(this)
        val URL="http://api.openweathermap.org/data/2.5/weather?units=metric&appid=dac6f28aca1849959452b5052bc69fad&lat=${CUR_LAT}&lon=${CUR_LNG}"
        task.execute(URL(URL))
        val currentTime: Date = Calendar.getInstance().getTime()
        val date_text: String =
            SimpleDateFormat("yyyy년 MM월 dd일 EE요일", Locale.getDefault())
                .format(currentTime)
        todayDateTxt.text=date_text
    }

    fun setUpWeatherTxt(weather:String, minTemp:String, temp:String, maxTemp:String){
        runOnUiThread {
            todayTempTxt.text="\n최고 온도 : ${maxTemp}도\n현재 온도 : ${temp}도\n최저 온도 : ${minTemp}도 "
            val url="http://openweathermap.org/img/wn/"+weather+"@2x.png"
            Glide.with(this).load(url).into(todayWeatherImg)
        }
    }

    private fun initDB() {
        val DBfile=this.getDatabasePath("final_db.db")
        if(!DBfile.parentFile.exists())
            DBfile.mkdir()
        if(!DBfile.exists()){
            val file=resources.openRawResource(R.raw.final_db)
            val fileSize=file.available()
            val buffer=ByteArray(fileSize)
            file.read(buffer)
            file.close()
            DBfile.createNewFile()
            val output=FileOutputStream(DBfile)
            output.write(buffer)
            output.close()
        }

    }


    private fun addExcel() {
        //엑셀에서 산 데이터를 읽어와서 파일이 있으면 대표 좌표를 설정하여 값에 넣어주는 것
        val inputStream:InputStream=baseContext.resources.assets.open("MNT_CODE.xls")
        val workBook :Workbook= Workbook.getWorkbook(inputStream)
        val sheet=workBook.getSheet(0);
        val ROW_SIZE=2932
        val mountainDBHelper=MountainDBHelper(this)
 //       mountainDBHelper.onCreate(final_)
//        mountainDBHelper.create()
        val factory : CRSFactory = CRSFactory()
        val beforeCrs=factory.createFromName("EPSG:5186")
        val afterCrs=factory.createFromName("EPSG:5179")
        val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)

        //이름, 주소, ID, 대표 좌표
        for(row in 1 until ROW_SIZE) {
            val mtName = sheet.getCell(1, row).contents
            val mtLocation = sheet.getCell(2, row).contents
            val mtCode = sheet.getCell(3, row).contents
            val facilityDBHelper = FacilityDBHelper(this)

            var lat=0.0
            var lng=0.0
            val list=facilityDBHelper.getFacilities(mtCode)
            if(list.size>0){
                lat=list[0].lat
                lng=list[0].lng
            }
            else{

                //시설 값이 없으면 모든 경로 파일에서 해당 산의 파일을 찾음
                val assetManager = resources.assets
                val ROUTE_FILE_NAME = "geoson/PMNTN_" + mtName + "_" + mtCode + ".json"
                try {
                    val routeInputStream = assetManager.open(ROUTE_FILE_NAME)
                    val routeJsonString = routeInputStream.bufferedReader().use { it.readText() }
                    val routeJsonObject = JSONObject(routeJsonString)
                    val routeJsonArray = routeJsonObject.getJSONArray("features")
                    val paths = routeJsonArray.getJSONObject(0).getJSONObject("geometry").getJSONArray("paths").getJSONArray(0)
                    val beforeX = paths.getJSONArray(0).get(0).toString().toDouble()
                    val beforeY = paths.getJSONArray(0).get(1).toString().toDouble()
                    val beforeCoord= ProjCoordinate(beforeX,beforeY)
                    val afterCoord= ProjCoordinate()
                    transform.transform(beforeCoord,afterCoord)
                    val latLng=Utmk(afterCoord.x,afterCoord.y).toLatLng()
                    lat=latLng.latitude
                    lng=latLng.longitude
                }
                catch (e:Exception){
                    Log.d("errorr",e.message)
                }
            }
            val item=Mountain(mtName,mtLocation,mtCode,lat,lng,0,"")
            val result=mountainDBHelper.insertMountain(item)
            Log.d("Wlsakr",row.toString()+")"+item.toString())
        }

    }


    private fun addFacility() {
        val inputStream:InputStream=baseContext.resources.assets.open("MNT_CODE.xls")
        val workBook :Workbook= Workbook.getWorkbook(inputStream)
        val sheet=workBook.getSheet(0);
        val ROW_SIZE=2932
        val factory : CRSFactory = CRSFactory()
        val beforeCrs=factory.createFromName("EPSG:5186")
        val afterCrs=factory.createFromName("EPSG:5179")
        val transform: BasicCoordinateTransform = BasicCoordinateTransform(beforeCrs,afterCrs)

        for(row in 1 until ROW_SIZE){
            val mtName=sheet.getCell(1,row).contents
            val mtLocation=sheet.getCell(2,row).contents
            val mtCode=sheet.getCell(3,row).contents

            val SPOT_FILE_NAME="geoson/PMNTN_SPOT_"+mtName+"_"+mtCode+".json"
            val assetManager=resources.assets
            val facilityDBHelper=FacilityDBHelper(this)
            try {
                val spotInputStream=assetManager.open(SPOT_FILE_NAME)
                val spotJsonString=spotInputStream.bufferedReader().use { it.readText()}
                val spotJsonObject= JSONObject(spotJsonString)
                val spotJsonArray=spotJsonObject.getJSONArray("features")

                //산 시설 정보 받아오는 것
                for(i in 0 until spotJsonArray.length()){
                    val x=spotJsonArray.getJSONObject(i).getJSONObject("geometry").get("x").toString().toDouble()
                    val y=spotJsonArray.getJSONObject(i).getJSONObject("geometry").get("y").toString().toDouble()
                    val type=spotJsonArray.getJSONObject(i).getJSONObject("attributes").get("DETAIL_SPO").toString().substringBefore(",")
                    val type2=type.substringBefore(".")
                    val fID=spotJsonArray.getJSONObject(i).getJSONObject("attributes").get("PMNTN_SPOT").toString()

                    val beforeCoord= ProjCoordinate(x,y)
                    val afterCoord= ProjCoordinate()
                    transform.transform(beforeCoord,afterCoord)
                    val after=Utmk(afterCoord.x,afterCoord.y).toLatLng()

                    val facility=Facility(fID,mtCode,type2,after.latitude, after.longitude)
                    val result=facilityDBHelper.insertFacility(facility)

                }
            }
            catch (e:IOException){
                e.printStackTrace()
            }
            Log.d("WLsakr",row.toString()+mtName)
        }
    }

    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location ->
        CUR_LAT=location.latitude
        CUR_LNG=location.longitude
            initWeather()
    }
}

    private fun initToggle() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val toggle=ActionBarDrawerToggle(this,mainDrawerLayout,mainToolbar,0,0)
        mainDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        mainNavigationView.setNavigationItemSelectedListener(this)
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
        getCurrentLocation()
        naverMap.mapType=NaverMap.MapType.Terrain
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN,true)
        val uiSettings=naverMap.uiSettings
        uiSettings.isLocationButtonEnabled=true
        uiSettings.isCompassEnabled=true
        naverMap.locationSource=locationSource
        naverMap.locationTrackingMode=LocationTrackingMode.Follow

        val result=MountainDBHelper(this).getAllRecord()
         Log.d("Wls",result.toString())

        for(i in 0 until result.size){
            val marker=Marker()
            val x=result[i].lat
            val y=result[i].lng
            val height=result[i].height
            Log.d("Wls","$MT_NAME $MT_LOCATION $MT_MID $MT_LAT $MT_LNG")
            marker.setOnClickListener {
                overlay ->
                MT_NAME=result[i].name
                MT_LAT=result[i].lat
                MT_LNG=result[i].lng
                MT_MID=result[i].mID
                MT_LOCATION=result[i].location
                MT_HEIGHT=result[i].height
//                Log.d("Wls","$MT_NAME $MT_LOCATION $MT_MID $MT_LAT $MT_LNG")
                val intent=Intent(this, ShowMountainActivity::class.java)
                startActivity(intent)
                true
            }
            marker.position= LatLng(x,y)
            marker.captionText=result[i].name
            if(result[i].visited>0)
                marker.icon= OverlayImage.fromResource(R.drawable.visited_mountain)
            else{
                try {
                    if(height.toDouble()>500.0)
                        marker.icon= OverlayImage.fromResource(R.drawable.high_mountain)
                    else
                        marker.icon= OverlayImage.fromResource(R.drawable.unvisited_mountain)
                }
                catch (e:java.lang.Exception){
                    marker.icon= OverlayImage.fromResource(R.drawable.unvisited_mountain)
                }
            }

            marker.map=naverMap
            marker.minZoom=7.0
        }

        naverMap.setOnSymbolClickListener { symbol ->
            val lat=symbol.position.latitude
            val lng=symbol.position.longitude
            val str=if(symbol.caption.contains("(")){
                val subIdx=symbol.caption.lastIndexOf("(")
                symbol.caption.substring(0,subIdx-1)
            }
            else
                symbol.caption
            if(str.endsWith("산") || str.endsWith("봉")){
                var Mtfound=false
                val result=MountainDBHelper(this).getSerachedRecord(str)
                for(i in 0 until result.size) {
                    Log.d("dist", result[i].toString())
                    val x = result[i].lat
                    val y = result[i].lng
                    if(getDistance(x,y,lat,lng)<=3.0){
                        val intent=Intent(this,ShowMountainActivity::class.java)
                        val GOOGLE_URL="https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                                "location=${lat},${lng}&radius=4000&type=natural_feature&keyword="+str+"&key=AIzaSyDz0UMMZSLiWw0wn9-zeamMx2tdUp8iff0"
                        Mtfound=true
                        intent.putExtra("url",GOOGLE_URL)
                        MT_NAME=result[i].name
                        MT_LAT=result[i].lat
                        MT_LNG=result[i].lng
                        MT_MID=result[i].mID
                        MT_LOCATION=result[i].location
                        MT_HEIGHT=result[i].height

                        intent.putExtra("symbol",true)
                        startActivity(intent)
                        break
                    }
                }
                if(!Mtfound){
                    val intent=Intent(this,ShowMountainActivity::class.java)
                    intent.putExtra("noInfo",true)
                    MT_LAT=symbol.position.latitude
                    MT_LNG=symbol.position.longitude
                    MT_NAME=str
                    startActivity(intent)
                }
            }
            true
        }
     }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(locationSource.onRequestPermissionsResult(requestCode,permissions,grantResults)){
            if(!locationSource.isActivated){
                //권한 거부됨
                naverMap.locationTrackingMode=LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            R.id.item1->{
                val intent=Intent(this,LogListActivity::class.java)
                startActivity(intent)
            }
            R.id.item3->{
                val intent=Intent(this,ContractInfoActivity::class.java)
                startActivity(intent)
            }
            R.id.item4->{
                toast("건국대학교 컴퓨터공학부 201411194 김대엽")
            }
        }
        return false
    }
    override fun onBackPressed() {
        if(mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mainDrawerLayout.closeDrawers()
        }else{
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.mainSearchBtn->{
                val intent=Intent(this,SearchMountainActivity::class.java)
                intent.putExtra("searchedName",mainSearchText.text.toString())
                startActivity(intent)
            }
            R.id.SmsSOSBtn->{
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
                Log.d("dbdb",txt)
                startActivity(msgIntent)
            }
            R.id.CallSOSBtn->{
                var telnum=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("callNum","01000000000")
                val tel= Uri.parse("tel:"+ telnum)
                val callIntent=Intent(Intent.ACTION_DIAL,tel)
                startActivity(callIntent)
            }

        }
    }
}
