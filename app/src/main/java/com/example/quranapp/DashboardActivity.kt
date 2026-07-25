package com.example.quranapp

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
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
        
        // این خط اضافه شد تا لیست آیات پاس‌شده و دکمه مرور فعال شود
        setupPassedVersesList(currentIndex)
    }

    private fun setupPassedVersesList(maxApprovedIndex: Int) {
        val rvPassedVerses = findViewById<RecyclerView>(R.id.rvPassedVerses)
        rvPassedVerses.layoutManager = LinearLayoutManager(this)

        val allVerses = VerseRepository.load(this)
        val passedList = mutableListOf<Pair<Int, String>>()

        for (i in 0 until maxApprovedIndex) {
            if (i < allVerses.size) {
                passedList.add(Pair(i, "آیه ${i + 1} - ${allVerses[i].surah_reference}"))
            }
        }

        rvPassedVerses.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tv = holder.itemView.findViewById<TextView>(android.R.id.text1)
                val item = passedList[position]
                tv.text = "✓ ${item.second} (کلیک برای مرور)"
                tv.textSize = 15f
                
                holder.itemView.setOnClickListener {
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java)
                    intent.putExtra("VERSE_INDEX", item.first) 
                    startActivity(intent)
                }
            }

            override fun getItemCount(): Int = passedList.size
        }
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

            override fun onFailure(call: Call<GetProgressResponse>, t: Throwable) {
            }
        })
    }
}
