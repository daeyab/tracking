package com.daeyab.tracking

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.daeyab.tracking.MtValues.MT_MID
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.lang.ref.WeakReference
import java.net.URL

class MtInfoAsyncTask(context:ShowMountainActivity): AsyncTask<URL, Unit, Unit>() {

    val activityReference= WeakReference(context)

    override fun doInBackground(vararg params: URL?) {
        //액티비티 정보 얻기 - MountainActivity 정보 받기
        val activity=activityReference.get()
        //액티비티 정보 받아오기, 파싱하기
        var height=""

        val doc= Jsoup
            .connect(params[0].toString())
            .parser(Parser.xmlParser())
            .get()
        val mountains=doc.select("item")
        val mountainDBHelper=MountainDBHelper(activity!!.applicationContext)

        for(i in 0 until mountains.size)
                mountainDBHelper.updateHeight(mountains[i].select("mntilistno")?.text().toString(),mountains[i].select("mntihigh").text().toString())
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        val activity=activityReference.get()
        if(activity==null ||  activity.isFinishing)
            return
//        activity.adapter.notifyDataSetChanged()
    }
}