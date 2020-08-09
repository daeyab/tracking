package com.daeyab.tracking

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL

class WeatherAsyncTask(context:Activity): AsyncTask<URL, Unit, Boolean>() {
    //메모리 누수를 위한 weakReference
    val activityReference=WeakReference(context)
    val searchedInfo= mutableListOf<Facility>()
    //parsing 하는 부분

    override fun doInBackground(vararg params: URL?): Boolean? { //가변인자 받는 것
        //html 정보 받아오기, 파싱하기
        val activity=activityReference.get() as MainActivity//액티비티 정보 얻는 방법 - 참조하고 있는 객체 정보를 받아옴, mainActivity 정보 획득함
        //parsing 하는 부분

        //JSON
        val doc=Jsoup.connect(params[0].toString()).ignoreContentType(true).get() //JSON object 받아올 수 있음
        val jsonObject= JSONObject(doc.text())
        Log.d("weather",jsonObject.toString())
        val weather=jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon")
 //       Log.d("weather",weather)
        val maxTemp=jsonObject.getJSONObject("main").getDouble("temp_max").toString()
        val minTemp=jsonObject.getJSONObject("main").getDouble("temp_min").toString()
        val temp=jsonObject.getJSONObject("main").getDouble("temp").toString()

        activity?.setUpWeatherTxt(weather,minTemp,temp,maxTemp)
        //날씨 정보 받아오기 성공 !! 여기서부터 시작 그리고 이제 위젯으로 띄워주면 됨!!

        Log.i("weather","$maxTemp $minTemp $temp")
        return true
    }

    override fun onProgressUpdate(vararg values: Unit?) {
        super.onProgressUpdate(*values)
    }

    //다 끝난 경우
    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        val activity=activityReference.get() //백그라운드 끝나고 호출하는거라 activity가 끝나있을수도
        if(activity==null || activity.isFinishing)
            return
    }

}

