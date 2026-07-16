package com.example.quranapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // آدرس IP برای اتصال شبیه‌ساز اندروید استودیو به لوکال‌هاست سیستم شما
    private const val BASE_URL = "http://10.145.50.17/quran_api/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // الان این کلاس به درستی شناخته می‌شود
            .build()

        retrofit.create(ApiService::class.java)
    }
}
