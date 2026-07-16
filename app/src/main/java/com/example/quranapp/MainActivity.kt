package com.example.quranapp

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var verseNumber: TextView
    private lateinit var arabicText: TextView
    private lateinit var translation: TextView
    private lateinit var exampleText: TextView
    
    private lateinit var nextButton: Button
    private lateinit var btnBackToDashboard: Button
    
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var checkBox3: CheckBox
    private lateinit var checkBox4: CheckBox

    private var isResetting = false
    private lateinit var progressManager: ProgressManager
    private var verses: List<Verse> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verseNumber = findViewById(R.id.verseNumber)
        arabicText = findViewById(R.id.arabicText)
        translation = findViewById(R.id.translation)
        exampleText = findViewById(R.id.exampleText)
        
        nextButton = findViewById(R.id.nextButton)
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)
        
        checkBox1 = findViewById(R.id.checkBox1)
        checkBox2 = findViewById(R.id.checkBox2)
        checkBox3 = findViewById(R.id.checkBox3)
        checkBox4 = findViewById(R.id.checkBox4)

        progressManager = ProgressManager(this)
        verses = VerseRepository.load(this)

        btnBackToDashboard.setOnClickListener {
            finish() 
        }

        val checkListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isResetting) {
                playClickSound()
            }
            checkAllCompleted()
        }

        checkBox1.setOnCheckedChangeListener(checkListener)
        checkBox2.setOnCheckedChangeListener(checkListener)
        checkBox3.setOnCheckedChangeListener(checkListener)
        checkBox4.setOnCheckedChangeListener(checkListener)

        nextButton.setOnClickListener {
            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val studentId = sharedPref.getString("user_id", "")
            val isGuest = (studentId == "guest")

            if (isGuest) {
                // --- منطق حالت آفلاین (بدون نیاز به سرور) ---
                val currentIndex = progressManager.getIndex()
                progressManager.saveIndex(currentIndex + 1) // ذخیره آیه جدید در گوشی
                Toast.makeText(this@MainActivity, "آفرین! آیه بعدی...", Toast.LENGTH_SHORT).show()
                loadVerse() // بارگذاری مستقیم آیه بعدی
            } else {
                // --- منطق حالت آنلاین (کاربر واقعی) ---
                if (nextButton.text.contains("بررسی وضعیت")) {
                    syncProgressWithServer()
                } else {
                    sendProgressToServer()
                }
            }
        }

        nextButton.isEnabled = false
        
        // اول آیه را لود می‌کنیم
        loadVerse()
        
        // فقط اگر کاربر واقعی بود وضعیت را از سرور استعلام می‌گیریم
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        if (sharedPref.getString("user_id", "") != "guest") {
            syncProgressWithServer()
        }
    }

    private fun checkAllCompleted() {
        if (isResetting) return

        val allChecked = checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked
        
        if (allChecked) {
            nextButton.isEnabled = true
            
            // چک کردن حالت مهمان
            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val isGuest = sharedPref.getString("user_id", "") == "guest"
            
            if (isGuest) {
                nextButton.text = "رفتن به آیه بعدی (آفلاین)"
            } else {
                nextButton.text = "ارسال برای تایید معلم"
            }
            playSuccessSound()
        } else {
            nextButton.isEnabled = false
            nextButton.text = "آیه بعدی"
        }
    }

    private fun sendProgressToServer() {
        val currentIndex = progressManager.getIndex()
        
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val studentId = sharedPref.getString("user_id", "")

        if (studentId.isNullOrEmpty()) {
            Toast.makeText(this, "شناسه کاربر یافت نشد! لطفاً مجدداً وارد شوید.", Toast.LENGTH_LONG).show()
            return
        }

        nextButton.isEnabled = false
        nextButton.text = "در حال ارسال..."

        RetrofitClient.instance.updateProgress(studentId, currentIndex.toString())
            .enqueue(object : Callback<ProgressResponse> {
                override fun onResponse(call: Call<ProgressResponse>, response: Response<ProgressResponse>) {
                    if (response.isSuccessful && response.body()?.error == false) {
                        Toast.makeText(this@MainActivity, "با موفقیت ارسال شد.", Toast.LENGTH_SHORT).show()
                        // پس از ارسال موفق، UI را به حالت انتظار می‌بریم
                        setUiToPendingState()
                    } else {
                        Toast.makeText(this@MainActivity, "خطا در ارسال اطلاعات", Toast.LENGTH_SHORT).show()
                        nextButton.isEnabled = true
                        nextButton.text = "ارسال برای تایید معلم"
                    }
                }

                override fun onFailure(call: Call<ProgressResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "خطای شبکه!", Toast.LENGTH_SHORT).show()
                    nextButton.isEnabled = true
                    nextButton.text = "ارسال برای تایید معلم"
                }
            })
    }

    private fun syncProgressWithServer() {
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val studentId = sharedPref.getString("user_id", "")
        if (studentId.isNullOrEmpty()) return

        nextButton.text = "در حال ارتباط با سرور..."
        nextButton.isEnabled = false

        RetrofitClient.instance.getProgress(studentId).enqueue(object : Callback<GetProgressResponse> {
            override fun onResponse(call: Call<GetProgressResponse>, response: Response<GetProgressResponse>) {
                val progressList = response.body()?.progress
                
                if (response.isSuccessful && progressList != null) {
                    var maxApprovedIndex = -1
                    var isCurrentPending = false
                    val currentIndex = progressManager.getIndex()

                    // بررسی تمام رکوردهای دریافتی از سرور
                    for (item in progressList) {
                        val verseIdx = item.verse_index.toIntOrNull() ?: continue
                        if (item.status == "approved") {
                            if (verseIdx > maxApprovedIndex) {
                                maxApprovedIndex = verseIdx
                            }
                        } else if (item.status == "pending") {
                            if (verseIdx == currentIndex) {
                                isCurrentPending = true
                            }
                        }
                    }

                    // اگر معلم آیه فعلی (یا آیات بعدی) را تایید کرده است:
                    if (maxApprovedIndex >= currentIndex) {
                        progressManager.saveIndex(maxApprovedIndex + 1) // رفتن به آیه جدید
                        Toast.makeText(this@MainActivity, "معلم پیشرفت شما را تایید کرد!", Toast.LENGTH_LONG).show()
                        loadVerse() // لود کردن آیه جدید
                    } 
                    // اگر آیه فعلی در سرور هنوز در انتظار تایید است:
                    else if (isCurrentPending) {
                        setUiToPendingState()
                    } 
                    // اگر هیچکدام (حالت عادی یادگیری):
                    else {
                        checkAllCompleted() 
                    }
                } else {
                    checkAllCompleted()
                }
            }

            override fun onFailure(call: Call<GetProgressResponse>, t: Throwable) {
                checkAllCompleted() // بازگرداندن دکمه به حالت قبل در صورت خطا
                Toast.makeText(this@MainActivity, "عدم اتصال به سرور جهت همگام‌سازی", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUiToPendingState() {
        isResetting = true
        checkBox1.isChecked = true
        checkBox2.isChecked = true
        checkBox3.isChecked = true
        checkBox4.isChecked = true
        
        checkBox1.isEnabled = false
        checkBox2.isEnabled = false
        checkBox3.isEnabled = false
        checkBox4.isEnabled = false
        isResetting = false

        nextButton.isEnabled = true
        nextButton.text = "بررسی وضعیت تایید (رفرش)"
    }

    private fun loadVerse() {
        if (verses.isEmpty()) return

        isResetting = true
        
        // فعال کردن مجدد چک باکس‌ها برای آیه جدید
        checkBox1.isEnabled = true
        checkBox2.isEnabled = true
        checkBox3.isEnabled = true
        checkBox4.isEnabled = true

        checkBox1.isChecked = false
        checkBox2.isChecked = false
        checkBox3.isChecked = false
        checkBox4.isChecked = false
        
        nextButton.isEnabled = false
        nextButton.text = "آیه بعدی"
        
        isResetting = false

        val currentIndex = progressManager.getIndex() 
        
        if (currentIndex >= verses.size) {
            verseNumber.text = "پایان مسیر"
            arabicText.text = "تبریک! شما همه آیات را یاد گرفتید."
            translation.text = ""
            exampleText.text = ""
            nextButton.isEnabled = false
            
            // مخفی کردن چک‌باکس‌ها در صورت اتمام همه آیات (اختیاری)
            checkBox1.isEnabled = false
            checkBox2.isEnabled = false
            checkBox3.isEnabled = false
            checkBox4.isEnabled = false
            return 
        }

        val currentVerse = verses[currentIndex]

        verseNumber.text = "آیه ${currentVerse.number} - ${currentVerse.surah_reference}"
        arabicText.text = currentVerse.arabic_text
        translation.text = currentVerse.persian_translation

        if (currentVerse.practical_examples.toString().isNotEmpty()) {
            exampleText.text = currentVerse.practical_examples.toString()
        } else {
            exampleText.text = "مثالی وجود ندارد."
        }
    }

    private fun playClickSound() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.click)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSuccessSound() {
        try {
            val mediaPlayer = MediaPlayer.create(this, R.raw.success)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
