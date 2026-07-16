package com.example.quranapp

import android.content.Context

class ProgressManager(context: Context) {
    // فایل SharedPreferences برای ذخیره پیشرفت
    private val progressPref = context.getSharedPreferences("progress", Context.MODE_PRIVATE)
    
    // فایل SharedPreferences برای خواندن آیدی کاربری که الان لاگین کرده
    private val appPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    // گرفتن شناسه کاربر فعلی (اگر نبود، پیش‌فرض guest در نظر گرفته می‌شود)
    private fun getUserId(): String {
        return appPrefs.getString("user_id", "guest") ?: "guest"
    }

    fun getIndex(): Int {
        val userId = getUserId()
        // پیشرفت را اختصاصی برای همین کاربر می‌خوانیم
        return progressPref.getInt("index_$userId", 0)
    }

    fun saveIndex(i: Int) {
        val userId = getUserId()
        // پیشرفت را اختصاصی برای همین کاربر ذخیره می‌کنیم
        progressPref.edit()
            .putInt("index_$userId", i)
            .apply()
    }
}
