package com.daeyab.tracking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class FacilityDBHelper(val context:Context):SQLiteOpenHelper(context, DB_NAME, null,DB_VERSION) {
    companion object{
        val DB_NAME="final_db.db"
        val DB_VERSION=1
        val TABLE_NAME="facilities"
        val NUM="num"
        val FID="fid"
        val MID="mid"
        val TYPE="type"
        val LAT="lat"
        val LNG="lng"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val create_table="create table if not exists "+ TABLE_NAME+ "("+
                NUM+" integer primary key autoincrement, "+
                FID+" text,"+
                MID+" text, "+
                TYPE+" text, "+
                LAT+" real,"+
                LNG+" real)"
        db?.execSQL(create_table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val drop_table="drop table if exists "+ TABLE_NAME
        db?.execSQL(drop_table)
        onCreate(db)
    }

    fun insertFacility(facility: Facility):Boolean{
        val values=ContentValues()
        values.put(FID,facility.fID)
        values.put(MID,facility.mID)
        values.put(TYPE,facility.type)
        values.put(LAT,facility.lat)
        values.put(LNG,facility.lng)
        val db=this.writableDatabase
        if(db.insert(TABLE_NAME,null,values).toInt()!=-1){
            db.close()
            return true
        }
        db.close()
        return false
    }
    fun getFacilities(mID:String):MutableList<Facility>{
        val list= mutableListOf<Facility>()
        val db=this.readableDatabase
        val strsql="select fid,type,lat,lng from facilities where mid='"+mID+"'"

        val cursor=db.rawQuery(strsql,null)
        if(cursor.count>0){
            cursor.moveToFirst()
            val colcount=cursor.columnCount
            do {
                var fid=""
                var type=""
                var lat=0.0
                var lng=0.0
                for( i in 0 until colcount){
                    fid=cursor.getString(0)
                    type=cursor.getString(1)
                    lat=cursor.getDouble(2)
                    lng=cursor.getDouble(3)
                }
                val facility=Facility(fid,mID,type,lat,lng)
                list.add(facility)
            }
            while (cursor.moveToNext())
        }
        return list
    }
}