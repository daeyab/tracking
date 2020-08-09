package com.daeyab.tracking

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import org.jetbrains.anko.wrapContent

class MountainDBHelper(val context:Context):SQLiteOpenHelper(context, DB_NAME, null,DB_VERSION) {
    companion object{
        val DB_NAME="final_db.db"
        val DB_VERSION=1
        val TABLE_NAME="mountains"
        val NUM="num"
        val MID="mid"
        val LAT="lat"
        val LNG="lng"
        val NAME="name"
        val LOCATION="location"
        val VISITED="visited"
        val HEIGHT="( - )"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("errorr","생성되었음!")
        val create_table="create table if not exists "+ TABLE_NAME+ "("+
                NUM+" integer primary key autoincrement, "+
                MID+" text,"+
                NAME+" text, "+
                LOCATION+" text," +
                LAT+" real," +
                LNG+" real,"+
                VISITED+" integer,"+
                HEIGHT+" text)"
        Log.d("errorr",create_table+"?")
        db?.execSQL(create_table)
    }
    fun create(){
        onCreate(this.writableDatabase)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val drop_table="drop table if exists "+ TABLE_NAME
        db?.execSQL(drop_table)
        onCreate(db)
    }

    fun insertMountain(mountain: Mountain):Boolean{
        val values=ContentValues()
        values.put(NAME,mountain.name)
        values.put(MID,mountain.mID)
        values.put(LOCATION,mountain.location)
        values.put(LAT,mountain.lat)
        values.put(LNG,mountain.lng)
        values.put(VISITED,mountain.visited)
        values.put(HEIGHT,mountain.height)

        val db=this.writableDatabase
        if(db.insert(TABLE_NAME,null,values).toInt()!=-1){
            db.close()
            return true
        }
        db.close()
        return false
    }
    fun getAllRecord():MutableList<Mountain>{
        val strsql="select * from "+ TABLE_NAME
        val list= mutableListOf<Mountain>()
        val db=this.readableDatabase
        val cursor=db.rawQuery(strsql, null)
        if(cursor.count>0){
            cursor.moveToFirst()
            val colcount=cursor.columnCount
            do {
                var lat=0.0
                var lng=0.0
                var name=""
                var location=""
                var mid=""
                var visited=0
                var height=""
                for( i in 0 until colcount){
                    mid=cursor.getString(1)
                    name=cursor.getString(2)
                    location=cursor.getString(3)
                    lat=cursor.getDouble(4)
                    lng=cursor.getDouble(5)
                    visited=cursor.getInt(6)
                    if(cursor.getString(7)==null)
                        height="( - )"
                    else
                        height=cursor.getString(7)

                }
                val mountain=Mountain(name, location, mid, lat, lng, visited,height)
//                Log.d("dvb",mountain.toString())
                list.add(mountain)

            }
            while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return list
    }
    fun getSerachedRecord(name:String):MutableList<Mountain>{
        val list= mutableListOf<Mountain>()
        val db=this.readableDatabase
        val strsql="select * from mountains where name LIKE '%"+name+"%'"

        val cursor=db.rawQuery(strsql,null)
        if(cursor.count>0){
            cursor.moveToFirst()
            val colcount=cursor.columnCount
            do {
                var mid=""
                var name=""
                var location=""
                var lat=0.0
                var lng=0.0
                var visited=0
                var height=""

                for( i in 0 until colcount){
                    mid=cursor.getString(1)
                    name=cursor.getString(2)
                    location=cursor.getString(3)
                    lat=cursor.getDouble(4)
                    lng=cursor.getDouble(5)
                    visited=cursor.getInt(6)
                    if(cursor.getString(7)==null)
                        height="( - )"
                    else
                        height=cursor.getString(7)

                }
                val mountain=Mountain(name,location,mid,lat,lng,visited,height)
                list.add(mountain)

            }
            while (cursor.moveToNext())
        }
        return list
    }
    fun updateVisited(mid:String){
        val list= mutableListOf<Mountain>()
        val db=this.readableDatabase
        val strsql="update mountains set visited=1 where mid='"+mid+"'"
        db.execSQL(strsql)
    }

    fun alterTable(){
        val db=this.writableDatabase
        val strsql="alter table mountain add column height text"
        Log.d("eoduqdl", "alter")
        db.execSQL(strsql)
    }
    fun updateHeight(mid:String, height:String){
        val db=this.readableDatabase
        val strsql="update mountains set height='"+height+"' where mid='"+mid+"'"
        Log.d("eoduqdl", "update"+mid.toString()+height)
        db.execSQL(strsql)

    }
}