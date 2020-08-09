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

class GooglePlaceAsyncTask(context:ShowMountainActivity=ShowMountainActivity()): AsyncTask<URL, Unit, Boolean>() {
    //메모리 누수를 위한 weakReference
    val activityReference=WeakReference(context)
    val searchedResult= mutableListOf<Pair<Int,Place>>()

    //parsing 하는 부분

    override fun doInBackground(vararg params: URL?): Boolean? {

        val activity=activityReference.get() //액티비티 정보 얻는 방법 - 참조하고 있는 객체 정보를 받아옴, mainActivity 정보 획득함
        //parsing 하는 부분
        //JSON
        for(i in 0 until params.size){
            val doc=Jsoup.connect(params[i].toString()).ignoreContentType(true).get() //JSON object 받아올 수 있음
       //     Log.i("rnrmf","JSON:"+doc.text())
            val jsonObject= JSONObject(doc.text())
            //    Log.i("rnrmf","jsonObject:"+jsonObject)
            //예외처리
            val itemsArray=jsonObject.getJSONArray("results")
//        Log.i("rnrmf","len:"+itemsArray.toString())
            for(j in 0 until itemsArray.length()){
                val obj=itemsArray.getJSONObject(j)
                val name=obj.getString("name")
                var rating=0.0
                if(obj.has("rating")){
                    rating=obj.getDouble("rating")
                }
                val lat= obj.getJSONObject("geometry").getJSONObject("location").getDouble("lat")
                val lng=obj.getJSONObject("geometry").getJSONObject("location").getDouble("lng")
                //         val address=obj.getJSONObject("address").getString("parcel")
                Log.d("rnrmf",name+" "+lat.toString()+" "+lng.toString()+" "+rating.toString())
                searchedResult.add(Pair(i,Place(name,lat,lng,rating)))
            }
            activity?.getResult(searchedResult)
        }
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

