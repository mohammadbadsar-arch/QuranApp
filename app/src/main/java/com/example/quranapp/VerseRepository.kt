package com.example.quranapp

import android.content.Context
import org.json.JSONArray

object VerseRepository {

 fun load(context:Context):List<Verse>{
  val text=context.assets.open("Quran_Verses_100.json").bufferedReader().use{it.readText()}
  val arr=JSONArray(text)
  val list= mutableListOf<Verse>()
  for(i in 0 until arr.length()){
   val o=arr.getJSONObject(i)
   val exArr=o.getJSONArray("practical_examples")
   val ex= mutableListOf<String>()
   for(j in 0 until exArr.length()) ex.add(exArr.getString(j))
   list.add(Verse(o.getInt("number"),o.getString("arabic_text"),o.getString("persian_translation"),o.getString("surah_reference"),ex))
  }
  return list
 }
}
