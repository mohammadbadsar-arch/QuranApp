package com.example.quranapp

import android.content.Context
import org.json.JSONArray

object VerseRepository {

    fun load(context: Context): List<Verse> {
        val text = context.assets.open("Quran_Verses_100.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(text)
        val list = mutableListOf<Verse>()
        
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            
            // در اینجا به جای آرایه، مقدار را به عنوان متن (String) می‌گیریم
            val exampleText = o.getString("practical_examples")
            val ex = listOf(exampleText) 
            
            list.add(
                Verse(
                    o.getInt("number"),
                    o.getString("arabic_text"),
                    o.getString("persian_translation"),
                    o.getString("surah_reference"),
                    ex
                )
            )
        }
        return list
    }
}
