package com.daeyab.tracking

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.daeyab.tracking.MtValues.HOME_LAT
import com.daeyab.tracking.MtValues.HOME_LNG
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.android.synthetic.main.activity_contract_info.*
import org.jetbrains.anko.toast
import java.util.*

class ContractInfoActivity : AppCompatActivity(), OnMapReadyCallback {

    val marker1=Marker()
    val marker2=Marker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_info)

        var mmsnum=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("smsNum","010-0000-0000")
        var mmstxt=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("smsText","")
        var telnum=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("callNum","01000000000")

        HOME_LAT=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("homeLat", "37.54075")!!
        HOME_LNG=getSharedPreferences("pref", Context.MODE_PRIVATE).getString("homeLng","127.079333")!!

     //   toast(HOME_LAT+","+HOME_LNG)
        SMSNum.setText(mmsnum)
        SMSText.setText(mmstxt)
        CallNum.setText(telnum)
        initMap()

        confirmInfo.setOnClickListener{
            val intent= Intent(this,MainActivity::class.java)
            saveValue("smsText",SMSText.text.toString())
            saveValue("smsNum",SMSNum.text.toString())
            saveValue("callNum",CallNum.text.toString())
            startActivity(intent)
        }
    }


    fun saveValue(Key : String, Value : String) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString(Key, Value)
        ed.apply()
    }

    fun initMap(){
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.homeMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d("wpqkf",HOME_LAT+","+HOME_LNG)
        naverMap.cameraPosition= CameraPosition(LatLng(HOME_LAT.toDouble(),HOME_LNG.toDouble()),13.00)

        if(marker1.map==null){
            marker1.position= LatLng(HOME_LAT.toDouble(),HOME_LNG.toDouble())
            marker1.icon= OverlayImage.fromResource(R.drawable.ic_baseline_home_24)
            marker1.map=naverMap
        }
        else{
            marker2.position= LatLng(HOME_LAT.toDouble(),HOME_LNG.toDouble())
            marker2.icon= OverlayImage.fromResource(R.drawable.ic_baseline_home_24)
            marker2.map=naverMap
        }
            naverMap.setOnMapClickListener{ pointF: PointF, latLng: LatLng ->
                if(marker1.map==null){
                    marker2.map=null
                    marker1.position= latLng
                    marker1.icon= OverlayImage.fromResource(R.drawable.ic_baseline_home_24)
                    marker1.map=naverMap
                }
                else{
                    marker1.map=null
                    marker2.position= latLng
                    marker2.icon= OverlayImage.fromResource(R.drawable.ic_baseline_home_24)
                    marker2.map=naverMap
                }
                saveValue("homeLat",latLng.latitude.toString())
                saveValue("homeLng",latLng.longitude.toString())
                HOME_LAT=latLng.latitude.toString()
                HOME_LNG=latLng.longitude.toString()
                naverMap.cameraPosition= CameraPosition(LatLng(HOME_LAT.toDouble(),HOME_LNG.toDouble()),13.00)
            }
    }

}