package com.example.quranapp

import android.content.Context

class ProgressManager(context:Context){
 private val pref=context.getSharedPreferences("progress",Context.MODE_PRIVATE)
 fun getIndex():Int=pref.getInt("index",0)
 fun saveIndex(i:Int){ pref.edit().putInt("index",i).apply() }
}
