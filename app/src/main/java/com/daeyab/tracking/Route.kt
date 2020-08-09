package com.daeyab.tracking

import android.os.Parcelable

data class Route(val rID:String,val mID:String, val name:String, val level:String, val path:MutableList<Pair<Double,Double>> ){
    constructor():this("rID","mID","NAME","LEVEL", mutableListOf<Pair<Double,Double>>())

}