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
            sendProgressToServer()
        }

        nextButton.isEnabled = false
        loadVerse()
    }

    private fun checkAllCompleted() {
        if (isResetting) return

        val allChecked = checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked
        
        if (allChecked) {
            nextButton.isEnabled = true
            nextButton.text = "ارسال برای تایید معلم"
            playSuccessSound()
        } else {
            nextButton.isEnabled = false
            nextButton.text = "آیه بعدی"
        }
    }

    private fun sendProgressToServer() {
        val currentIndex = progressManager.getIndex()
        
        // دریافت آیدی دانش‌آموز
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val studentId = sharedPref.getString("user_id", "") // پیش‌فرض را خالی گذاشتیم تا عدد ۱ الکی ارسال نشود

        // اگر آیدی کاربر ذخیره نشده بود (نیاز به لاگین مجدد)
        if (studentId.isNullOrEmpty()) {
            Toast.makeText(this, "شناسه کاربر یافت نشد! لطفاً از برنامه خارج شده و دوباره وارد شوید.", Toast.LENGTH_LONG).show()
            nextButton.isEnabled = true
            nextButton.text = "ارسال برای تایید معلم"
            return
        }

        nextButton.isEnabled = false
        nextButton.text = "در حال ارسال..."

        RetrofitClient.instance.updateProgress(studentId, currentIndex.toString())
            .enqueue(object : Callback<ProgressResponse> {
                override fun onResponse(call: Call<ProgressResponse>, response: Response<ProgressResponse>) {
                    if (response.isSuccessful && response.body()?.error == false) {
                        Toast.makeText(this@MainActivity, "با موفقیت ارسال شد. در انتظار تایید معلم...", Toast.LENGTH_LONG).show()
                        nextButton.text = "در انتظار تایید..."
                    } else {
                        Toast.makeText(this@MainActivity, "خطا در ارسال اطلاعات", Toast.LENGTH_SHORT).show()
                        nextButton.isEnabled = true
                        nextButton.text = "تلاش مجدد"
                    }
                }

                override fun onFailure(call: Call<ProgressResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "خطای شبکه!", Toast.LENGTH_SHORT).show()
                    nextButton.isEnabled = true
                    nextButton.text = "تلاش مجدد"
                }
            })
    }

    private fun loadVerse() {
        if (verses.isEmpty()) return

        isResetting = true

        checkBox1.isChecked = false
        checkBox2.isChecked = false
        checkBox3.isChecked = false
        checkBox4.isChecked = false
        
        nextButton.isEnabled = false
        nextButton.text = "آیه بعدی"
        
        isResetting = false

        val currentIndex = progressManager.getIndex() 
        
        if (currentIndex >= verses.size) {
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
