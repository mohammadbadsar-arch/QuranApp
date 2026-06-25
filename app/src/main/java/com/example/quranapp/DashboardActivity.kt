package com.example.quranapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var progressManager: ProgressManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        progressManager = ProgressManager(this)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvProgressText = findViewById<TextView>(R.id.tvProgressText)
        val btnStart = findViewById<Button>(R.id.btnStart)

        // تعداد کل آیات (فعلا ثابت 100 در نظر می‌گیریم)
        val totalVerses = 100
        
        // ایندکس فعلی کاربر را می‌گیریم (همان تعداد آیاتی که یاد گرفته)
        val currentIndex = progressManager.getIndex()

        // آپدیت کردن نوار پیشرفت و متن
        progressBar.max = totalVerses
        progressBar.progress = currentIndex
        tvProgressText.text = "شما $currentIndex آیه از $totalVerses آیه را یاد گرفته‌اید"

        // کلیک روی دکمه برای رفتن به صفحه آیات
        btnStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // این متد باعث می‌شود وقتی از صفحه MainActivity به این صفحه برمی‌گردیم، پیشرفت آپدیت شود
    override fun onResume() {
        super.onResume()
        val currentIndex = progressManager.getIndex()
        findViewById<ProgressBar>(R.id.progressBar).progress = currentIndex
        findViewById<TextView>(R.id.tvProgressText).text = "شما $currentIndex آیه از 100 آیه را یاد گرفته‌اید"
    }
}
