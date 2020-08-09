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

class RouteAdapter(val items:MutableList<Way>):RecyclerView.Adapter<RouteAdapter.RouteViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(holder: RouteViewHolder,view:View,data:Way,position: Int)
    }
    var itemClickListener:OnItemClickListener?=null
    inner class RouteViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var timetxt=itemView.findViewById<TextView>(R.id.routeTimeTxt)
        var costtxt=itemView.findViewById<TextView>(R.id.routeCostTxt)
        var totaltxt=itemView.findViewById<TextView>(R.id.routeTotalTxt)
        var disttxt=itemView.findViewById<TextView>(R.id.routedistTxt)
        var innerrecyclerview=itemView.findViewById<RecyclerView>(R.id.innerRecyclerView)

        init {
            itemView.setOnClickListener{
                itemClickListener?.onItemClick(this,it,items[adapterPosition],adapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RouteAdapter.RouteViewHolder {
        val v=LayoutInflater.from(parent.context).inflate(R.layout.route_row,parent,false)
        return RouteViewHolder(v)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RouteAdapter.RouteViewHolder, position: Int) {
        holder.costtxt.text=items[position].cost.toString() +"원"
        holder.timetxt.text=items[position].time.toString() +"분"
        holder.totaltxt.text="${items[position].sName} -> ${items[position].eName}"
        holder.disttxt.text=items[position].distance.toString()+"m"
        holder.innerrecyclerview.adapter=StationAdapter(items[position].stations)
        val childLayoutManager=LinearLayoutManager(holder.itemView.context,RecyclerView.VERTICAL,false)
        holder.itemView.innerRecyclerView.apply {
            layoutManager=childLayoutManager
            adapter=StationAdapter(items[position].stations)
            setRecycledViewPool(RecyclerView.RecycledViewPool())
        }
    }
}
