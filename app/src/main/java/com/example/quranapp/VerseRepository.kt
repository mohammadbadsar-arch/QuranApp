package com.example.quranapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object VerseRepository {

    fun load(context: Context): List<Verse> {
        return try {
            // خواندن محتوای فایل JSON
            val jsonString = context.assets.open("Quran_Verses_100.json")
                .bufferedReader()
                .use { it.readText() }

            // تعریف نوع لیست برای Gson
            val listType = object : TypeToken<List<Verse>>() {}.type

            // تبدیل خودکار JSON به لیست کلاس Verse
            Gson().fromJson(jsonString, listType) ?: emptyList()
            
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
