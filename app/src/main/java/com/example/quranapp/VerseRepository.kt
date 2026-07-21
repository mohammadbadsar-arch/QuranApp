package com.example.quranapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// یک کلاس کمکی برای گرفتن آرایه از داخل کلید verses
data class QuranResponse(
    @SerializedName("verses") val verses: List<Verse>
)

object VerseRepository {

    fun load(context: Context): List<Verse> {
        return try {
            // خواندن محتوای فایل جیسون از پوشه assets
            val jsonString = context.assets.open("Quran_Verses_100.json")
                .bufferedReader()
                .use { it.readText() }

            // پارس کردن فایل با استفاده از کلاس کمکی
            val response = Gson().fromJson(jsonString, QuranResponse::class.java)
            
            // برگرداندن لیست آیات (اگر خالی بود لیست خالی برمی‌گردد)
            response?.verses ?: emptyList()
            
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
