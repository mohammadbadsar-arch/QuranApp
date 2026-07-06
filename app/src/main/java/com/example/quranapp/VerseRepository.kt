package com.example.quranapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONException

object VerseRepository {

    fun load(context: Context): List<Verse> {
        val list = mutableListOf<Verse>()
        try {
            val text = context.assets.open("Quran_Verses_100.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(text)
            
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                
                // خواندن متن با optString برای جلوگیری از کرش در صورت نبودن فیلد
                val exampleText = o.optString("practical_examples", "")
                val ex = if (exampleText.isNotEmpty()) listOf(exampleText) else emptyList()
                
                list.add(
                    Verse(
                        o.optInt("number", 0),
                        o.optString("arabic_text", ""),
                        o.optString("persian_translation", ""),
                        o.optString("surah_reference", ""),
                        ex
                    )
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return list
    }
}
