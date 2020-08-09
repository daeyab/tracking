package com.daeyab.tracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StationAdapter(val items:MutableList<Station>):RecyclerView.Adapter<StationAdapter.StationViewHolder>(){

    inner class StationViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var starttxt=itemView.findViewById<TextView>(R.id.stationStartTxt)
        var endtxt=itemView.findViewById<TextView>(R.id.stationEndTxt)
        var bytxt=itemView.findViewById<TextView>(R.id.startionByTxt)
        var timetxt=itemView.findViewById<TextView>(R.id.stationTimeTxt)
        var disttxt=itemView.findViewById<TextView>(R.id.stationDistTxt)
        var img=itemView.findViewById<ImageView>(R.id.transportationImg)

        init {
            }
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StationViewHolder {
        val v=LayoutInflater.from(parent.context).inflate(R.layout.station_row,parent,false)
        return StationViewHolder(v)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: StationAdapter.StationViewHolder, position: Int) {
        if(items[position].by.contains("선")){
            holder.starttxt.text=items[position].sName+"역"
            holder.endtxt.text="->"+items[position].eName+"역"
            holder.bytxt.text=items[position].by
            holder.img.setImageResource(R.drawable.subway)
        }
        else{
            holder.starttxt.text=items[position].sName
            holder.endtxt.text=items[position].eName
            holder.bytxt.text=items[position].by+"번"
            holder.img.setImageResource(R.drawable.bus)
        }
        holder.timetxt.text=items[position].time.toString()+"분"
        holder.disttxt.text=items[position].distance.toString()+"m"
    }
}
