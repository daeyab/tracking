package com.daeyab.tracking

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

class SearchRouteAsyncTask(context:ShowMountainActivity): AsyncTask<URL, Unit, Boolean>() {
    //메모리 누수를 위한 weakReference
    val activityReference=WeakReference(context)
    val searchedInfo= mutableListOf<Facility>()
    //parsing 하는 부분

    override fun doInBackground(vararg params: URL?): Boolean? {
        val activity=activityReference.get()
        val doc=Jsoup.connect(params[0].toString()).ignoreContentType(true).get()
        val jsonObject= JSONObject(doc.text())
        Log.i("rudfh","jsonObject:"+jsonObject)
        //예외처리
        val itemsArray=jsonObject.getJSONObject("response").getJSONObject("result").getJSONArray("items")
        Log.i("dbdb","len:"+itemsArray.toString())
        for(i in 0 until itemsArray.length()){
            val obj=itemsArray.getJSONObject(i)
            val name=obj.getString("title")
            val id=obj.getString("id")
            val x= obj.getJSONObject("point").getString("x")
            val y=obj.getJSONObject("point").getString("y")
            val address=obj.getJSONObject("address").getString("parcel")
            Log.d("dbdb",name+" "+x+" "+y+" "+address)

            //TODO
//            searchedInfo.add(Facility(fid=id,name=name,x = x.toDouble(),y=y.toDouble(),address = address))
            //여기에 정보 입력되어 있음
        }
        Log.d("dbdb",searchedInfo.toString())

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

