package com.daeyab.tracking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class HikingLogDBHelper(val context:Context):SQLiteOpenHelper(context, DB_NAME, null,DB_VERSION) {
    companion object{
        val DB_NAME="final_db.db"
        val DB_VERSION=1
        val TABLE_NAME="log"
        val NUM="num"
        val NAME="name"
        val DATE="date"
        val TIME="time"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val create_table="create table if not exists "+ TABLE_NAME+ "("+
                NUM+" integer primary key autoincrement, "+
                NAME+" text, "+
                DATE+" text," +
                TIME+" text)"
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

    fun insertMountain(name:String, date:String, time:String):Boolean{
        val values=ContentValues()
        values.put(NAME,name)
        values.put(DATE,date)
        values.put(TIME,time)

        val db=this.writableDatabase
        if(db.insert(TABLE_NAME,null,values).toInt()!=-1){
            db.close()
            return true
        }
        db.close()
        return false
    }
    fun getAllRecord():MutableList<HikingLog>{
        val strsql="select * from "+ TABLE_NAME
        val list= mutableListOf<HikingLog>()
        val db=this.readableDatabase
        val cursor=db.rawQuery(strsql, null)
        if(cursor.count>0){
            cursor.moveToFirst()
            val colcount=cursor.columnCount
            do {
                var num=0
                var name=""
                var date=""
                var time=""
                for( i in 0 until colcount){
                    num=cursor.getInt(0)
                    name=cursor.getString(1)
                    date=cursor.getString(2)
                    time=cursor.getString(3)
                }
                val log=HikingLog(num,name,date,time)
//                //Log.d("dvb",mountain.toString())
                list.add(log)
            }
            while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return list
    }
}