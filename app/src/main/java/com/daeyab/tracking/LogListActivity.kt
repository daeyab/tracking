package com.daeyab.tracking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_log_list.*
import kotlinx.android.synthetic.main.activity_show_route.*

class LogListActivity : AppCompatActivity() {

    lateinit var  adapter:LogListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_list)
        init()
    }

    private fun init() {
        val hikingLogDBHelper=HikingLogDBHelper(this)
        val list=hikingLogDBHelper.getAllRecord()
        adapter= LogListAdapter(list)

        logRecyclerView.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        logRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        logRecyclerView.adapter=adapter
    }
}