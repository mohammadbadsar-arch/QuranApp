package com.example.quranapp

import android.app.Application
import cat.ereza.customactivityoncrash.config.CaocConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // فعال‌سازی صفحه نمایش ارور
        CaocConfig.Builder.create()
            .trackActivities(true)
            .showErrorDetails(true) // نمایش جزئیات ارور
            .apply()
    }
}
