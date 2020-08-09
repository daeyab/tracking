package com.daeyab.tracking


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.route_row.view.*

class LogListAdapter(val items:MutableList<HikingLog>):RecyclerView.Adapter<LogListAdapter.LogViewHolder>(){

    inner class LogViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var nametxt=itemView.findViewById<TextView>(R.id.logNameTxt)
        var numtxt=itemView.findViewById<TextView>(R.id.logNumTxt)
        var starttxt=itemView.findViewById<TextView>(R.id.logStartTxt)
        var timetxt=itemView.findViewById<TextView>(R.id.logTimeTxt)

        init {
            }
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LogListAdapter.LogViewHolder {
        val v=LayoutInflater.from(parent.context).inflate(R.layout.log_row,parent,false)
        return LogViewHolder(v)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: LogListAdapter.LogViewHolder, position: Int) {
        holder.numtxt.text=(position+1).toString()
        holder.nametxt.text=items[position].name
        holder.starttxt.text="시작 : "+items[position].date
        holder.timetxt.text="소요 : "+items[position].time
   }
}
