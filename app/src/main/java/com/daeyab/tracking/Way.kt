package com.daeyab.tracking

data class Way(val sName:String,val eName:String, val time:Int, val distance:Int, val cost:Int, val stations:MutableList<Station>)
