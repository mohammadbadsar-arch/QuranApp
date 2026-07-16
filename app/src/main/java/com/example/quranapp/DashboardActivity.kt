package com.example.quranapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var progressManager: ProgressManager
    private val totalVerses = 100 // تعداد کل آیات

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        progressManager = ProgressManager(this)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.max = totalVerses

        val btnStart = findViewById<Button>(R.id.btnStart)

        // کلیک روی دکمه برای رفتن به صفحه آیات
        btnStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // نمایش وضعیت اولیه کاربر هنگام باز شدن صفحه
        updateUI()
    }

    // این متد هر بار که کاربر به این صفحه برمی‌گردد اجرا می‌شود
    override fun onResume() {
        super.onResume()
        // ابتدا بر اساس دیتای ذخیره شده در گوشی آپدیت می‌کنیم
        updateUI()
        // سپس از سرور چک می‌کنیم که آیا معلم تایید جدیدی داشته یا خیر
        checkTeacherApproval()
    }

    // یک تابع کمکی برای آپدیت کردن نوار و متن پیشرفت (برای جلوگیری از تکرار کد)
    private fun updateUI() {
        val currentIndex = progressManager.getIndex()
        findViewById<ProgressBar>(R.id.progressBar).progress = currentIndex
        findViewById<TextView>(R.id.tvProgressText).text = "شما $currentIndex آیه از $totalVerses آیه را یاد گرفته‌اید"
    }

    // تابع بررسی وضعیت تایید از سرور
    private fun checkTeacherApproval() {
        // گرفتن آیدی دانش آموز
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val studentId = sharedPreferences.getString("student_id", "") ?: ""

        if (studentId.isEmpty()) return

        // اصلاح: استفاده از RetrofitClient.instance به جای apiService
        RetrofitClient.instance.getProgress(studentId).enqueue(object : Callback<GetProgressResponse> {
            override fun onResponse(call: Call<GetProgressResponse>, response: Response<GetProgressResponse>) {
                if (response.isSuccessful) {
                    // ایمن‌سازی در برابر Null (حذف علامت !! که خطرناک بود)
                    val progressList = response.body()?.progress ?: return
                    
                    // پیدا کردن بالاترین شماره آیه‌ای که تایید شده (approved) و تبدیل مقدار به عدد
                    val maxApprovedIndex = progressList.filter { it.status == "approved" }
                        .maxOfOrNull { it.verse_index.toString().toIntOrNull() ?: -1 } ?: -1

                    if (maxApprovedIndex >= 0) {
                        // اگر تایید شده بود، قفل آیه بعدی باز می‌شود
                        val nextUnlockedVerse = maxApprovedIndex + 1
                        val currentLocalIndex = progressManager.getIndex()
                        
                        // اگر آیه بعدی بزرگتر از پیشرفت فعلی گوشی است، یعنی معلم تایید جدیدی داده
                        if (nextUnlockedVerse > currentLocalIndex) {
                            progressManager.saveIndex(nextUnlockedVerse)
                            Toast.makeText(this@DashboardActivity, "معلم پیشرفت شما را تایید کرد!", Toast.LENGTH_SHORT).show()
                            // چون پیشرفت جدید داریم، دوباره صفحه را آپدیت می‌کنیم
                            updateUI() 
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GetProgressResponse>, t: Throwable) {
                // اگر خواستید می‌توانید خطا را اینجا هندل کنید (مثلا لاگ بیندازید)
            }
        })
    }
}
