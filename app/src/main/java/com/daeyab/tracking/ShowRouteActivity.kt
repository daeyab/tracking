package com.daeyab.tracking

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.daeyab.tracking.MtValues.CUR_LAT
import com.daeyab.tracking.MtValues.CUR_LNG
import com.daeyab.tracking.MtValues.HOME_LAT
import com.daeyab.tracking.MtValues.HOME_LNG
import com.daeyab.tracking.MtValues.MT_LAT
import com.daeyab.tracking.MtValues.MT_LNG
import com.daeyab.tracking.MtValues.MT_NAME
import com.odsay.odsayandroidsdk.API
import com.odsay.odsayandroidsdk.ODsayData
import com.odsay.odsayandroidsdk.ODsayService
import com.odsay.odsayandroidsdk.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_show_route.*
import kotlinx.android.synthetic.main.route_row.*
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.toast
import org.json.JSONException
import org.w3c.dom.Text

class ShowRouteActivity : AppCompatActivity() {

    lateinit var  adapter:RouteAdapter
    var checked= mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_route)
        init()
        initLayout()
    }

    private fun initLayout() {
        recyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    fun init(){
        val ODSAY_KEY="+2/6kyyCI/o5aNkC5r5Tq83xmF819vIVBf0WANmKDyQ"
        val odsayService= ODsayService.init(this,ODSAY_KEY)
        odsayService.setReadTimeout(5000)
        odsayService.setConnectionTimeout(5000)
        val onResultCallbackListener =object : OnResultCallbackListener {
            override fun onSuccess(oDsayData: ODsayData?, api: API?) {
                val wayList= mutableListOf<Way>()
                try {
                    if (api == API.SEARCH_PUB_TRANS_PATH) {
//                                val s=oDsayData?.json?.getJSONObject()
                        Log.d("rudfh", "size: "+oDsayData?.json?.toString())
                        val jsonArray = oDsayData!!.json.getJSONObject("result").getJSONArray("path")
                        Log.d("rudfh","size: "+jsonArray.length().toString())
                        for (i in 0 until jsonArray.length()) {
                            val stationList = mutableListOf<Station>()
                            val start = jsonArray.getJSONObject(i).getJSONObject("info").getString("firstStartStation")
                            val end = jsonArray.getJSONObject(i).getJSONObject("info").getString("lastEndStation")
                            val time = jsonArray.getJSONObject(i).getJSONObject("info").getInt("totalTime")
                            val cost = jsonArray.getJSONObject(i).getJSONObject("info").getInt("payment")
                            val dist = jsonArray.getJSONObject(i).getJSONObject("info").getInt("trafficDistance")
                            Log.d("rudfh"," $i -> $start to $end , $time 분 $cost 원 $dist m !!!")
                            val subPath = jsonArray.getJSONObject(i).getJSONArray("subPath")
                            for(j in 0 until subPath.length()){
                                val transType = when (subPath.getJSONObject(j).getInt("trafficType")) {
                                    1 -> "지하철"
                                    2 -> "버스"
                                    else -> "도보"
                                }
                                if(transType!="도보"){
                                    val startStation =subPath.getJSONObject(j).getString("startName")
                                    val endStation = subPath.getJSONObject(j).getString("endName")

                                    val transNum = when (transType) {
                                        "지하철"-> subPath.getJSONObject(j).getJSONArray("lane").getJSONObject(0).getString("name")
                                        "버스"-> subPath.getJSONObject(j).getJSONArray("lane").getJSONObject(0).getString("busNo")
                                        else -> "도보"
                                    }
                                    val time=subPath.getJSONObject(j).getString("sectionTime").toInt()
                                    val distance=subPath.getJSONObject(j).getString("distance").toInt()
                                    val station=Station(startStation,endStation,transNum,time,distance)
                                    Log.d("rudfh",station.toString()+"!!!\n")
                                    stationList.add(station)

                                }
                            }
                            wayList.add(Way(start,end,time,dist,cost,stationList))
                        }
                    }
                    adapter= RouteAdapter(wayList)
                    adapter.itemClickListener=object : RouteAdapter.OnItemClickListener {
                        override fun onItemClick(
                            holder: RouteAdapter.RouteViewHolder,
                            view: View,
                            data: Way,
                            position: Int
                        ) {
                            if(checked.contains(position)){
                                val stationAdapter=StationAdapter(wayList[position].stations)
                                innerRecyclerView.adapter=stationAdapter
                                //        innerRecyclerView.visibility=View.VISIBLE
                                holder.innerrecyclerview.visibility=View.GONE
                          //      recyclerView.adapter=adapter
                                checked.remove(position)
                            }
                            else{
                                val stationAdapter=StationAdapter(wayList[position].stations)
                                innerRecyclerView.adapter=stationAdapter
                         //       innerRecyclerView.visibility=View.GONE
                                holder.innerrecyclerview.visibility=View.VISIBLE
                                //recyclerView.adapter=adapter
                                checked.add(position)
                            }
                        }

                    }
                    recyclerView.adapter=adapter

                }
                catch (e: JSONException){
                    routeTogoTxt.text=" 가는 경로를 찾을 수 없습니다."
                    nodata.visibility=View.VISIBLE
                    toast("죄송합니다. 경로를 찾을 수 없습니다.")
                    Log.d("rudfh",e.message)
                    e.printStackTrace()
                }
            }

            override fun onError(p0: Int, p1: String?, p2: API?) {
                routeTogoTxt.text=" 가는 경로를 찾을 수 없습니다."
                nodata.visibility=View.VISIBLE
                toast("죄송합니다. 경로를 찾을 수 없습니다.")
            }
        }
        if(intent.getBooleanExtra("home",false)){
            routeTogoTxt.text="집 가는 방법"
            odsayService.requestSearchPubTransPath("${CUR_LNG}","${CUR_LAT}","${HOME_LNG.toDouble()}","${HOME_LAT.toDouble()}","0","0","0",onResultCallbackListener)

        }
        else{
            routeTogoTxt.text="$MT_NAME 가는 방법"
            odsayService.requestSearchPubTransPath("${CUR_LNG}","${CUR_LAT}","${MT_LNG}","$MT_LAT","0","0","0",onResultCallbackListener)
        }

    }
}