package com.example.quranapp

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.View
import android.content.Context
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
        val btnLogout = findViewById<Button>(R.id.btnLogout) // اضافه شدن دکمه خروج

        // کلیک روی دکمه برای رفتن به صفحه آیات
        btnStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // عملیات خروج از حساب
        btnLogout.setOnClickListener {
            // پاک کردن اطلاعات کاربر از حافظه اصلی (AppPrefs که در صفحه لاگین ذخیره می‌شود)
            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().remove("user_id").apply()

            // پاک کردن حافظه UserPrefs (برای اطمینان کامل)
            val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            userPrefs.edit().remove("student_id").apply()

            // بازگشت به صفحه لاگین و بستن کامل صفحات قبلی
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
// ۱. این متد را در DashboardActivity.kt برای تنظیم لیست اضافه کنید:
private fun setupPassedVersesList(maxApprovedIndex: Int) {
    val rvPassedVerses = findViewById<RecyclerView>(R.id.rvPassedVerses)
    rvPassedVerses.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

    // بارگذاری لیست همه آیات از فایل برای نمایش نام و شماره سوره
    val allVerses = VerseRepository.load(this)
    val passedList = mutableListOf<Pair<Int, String>>()

    for (i in 0 until maxApprovedIndex) {
        if (i < allVerses.size) {
            passedList.add(Pair(i, "آیه ${i + 1} - ${allVerses[i].surah_reference}"))
        }
    }

    // تنظیم آداپتور ساده برای لیست
    rvPassedVerses.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val tv = holder.itemView.findViewById<TextView>(android.R.id.text1)
            val item = passedList[position]
            tv.text = "✓ ${item.second}"
            tv.textSize = 15f
            
            // با کلیک روی هر آیه، به MainActivity می‌رویم و شماره آیه را ارسال می‌کنیم
            holder.itemView.setOnClickListener {
                val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                intent.putExtra("VERSE_INDEX", item.first) // ارسال شاخص آیه انتخاب‌شده
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = passedList.size
    }
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
                    // ایمن‌سازی در برابر Null
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
                // در صورت نیاز به لاگ زدن خطا
            }
        })
    }
}
