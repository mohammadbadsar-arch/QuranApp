package com.example.quranapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var progressManager: ProgressManager
    private val totalVerses = 100 
    
    private lateinit var rvPassedVerses: RecyclerView
    private lateinit var passedVerseAdapter: PassedVerseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        progressManager = ProgressManager(this)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.max = totalVerses

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnStart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().remove("user_id").apply()
            val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            userPrefs.edit().remove("student_id").apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // راه‌اندازی لیست مرور
        rvPassedVerses = findViewById(R.id.rvPassedVerses)
        rvPassedVerses.layoutManager = GridLayoutManager(this, 5) // 5 ستون برای مرور آیات
        passedVerseAdapter = PassedVerseAdapter(0)
        rvPassedVerses.adapter = passedVerseAdapter

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        checkTeacherApproval()
    }

    private fun updateUI() {
        val currentIndex = progressManager.getIndex()
        findViewById<ProgressBar>(R.id.progressBar).progress = currentIndex
        findViewById<TextView>(R.id.tvProgressText).text = "شما $currentIndex آیه از $totalVerses آیه را یاد گرفته‌اید"
        
        // آپدیت کردن آداپتور با تعداد آیاتی که تا الان یاد گرفته
        passedVerseAdapter.updateCount(currentIndex)
    }

    private fun checkTeacherApproval() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val studentId = sharedPreferences.getString("student_id", "") ?: ""
        if (studentId.isEmpty()) return

        RetrofitClient.instance.getProgress(studentId).enqueue(object : Callback<GetProgressResponse> {
            override fun onResponse(call: Call<GetProgressResponse>, response: Response<GetProgressResponse>) {
                if (response.isSuccessful) {
                    val progressList = response.body()?.progress ?: return
                    val maxApprovedIndex = progressList.filter { it.status == "approved" }
                        .maxOfOrNull { it.verse_index.toString().toIntOrNull() ?: -1 } ?: -1

                    if (maxApprovedIndex >= 0) {
                        val nextUnlockedVerse = maxApprovedIndex + 1
                        val currentLocalIndex = progressManager.getIndex()
                        if (nextUnlockedVerse > currentLocalIndex) {
                            progressManager.saveIndex(nextUnlockedVerse)
                            Toast.makeText(this@DashboardActivity, "معلم پیشرفت شما را تایید کرد!", Toast.LENGTH_SHORT).show()
                            updateUI() 
                        }
                    }
                }
            }
            override fun onFailure(call: Call<GetProgressResponse>, t: Throwable) {}
        })
    }

    // آداپتور داخلی برای نمایش آیات آموخته شده در داشبورد
    inner class PassedVerseAdapter(private var passedCount: Int) : RecyclerView.Adapter<PassedVerseAdapter.ViewHolder>() {

        fun updateCount(newCount: Int) {
            this.passedCount = newCount
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val root = view
            val tvVerseNumber: TextView = view.findViewById(R.id.tvVerseNumber)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_passed_verse, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvVerseNumber.text = "${position + 1}"
            
            holder.root.setOnClickListener {
                val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                intent.putExtra("REVIEW_VERSE_INDEX", position) // ارسال ایندکس آیه برای مرور
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = passedCount
    }
}
