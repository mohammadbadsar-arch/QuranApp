package com.example.quranapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var verseNumber: TextView
    private lateinit var arabicText: TextView
    private lateinit var translation: TextView
    private lateinit var exampleText: TextView
    
    private lateinit var cardTranslation: CardView
    private lateinit var cardExample: CardView
    private lateinit var btnShowTranslation: Button
    
    // متغیرهای جدید برای جدول کلمات
    private lateinit var wordsRecyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    
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
        
        cardTranslation = findViewById(R.id.cardTranslation)
        cardExample = findViewById(R.id.cardExample)
        btnShowTranslation = findViewById(R.id.btnShowTranslation)
        
        nextButton = findViewById(R.id.nextButton)
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)
        
        checkBox1 = findViewById(R.id.checkBox1)
        checkBox2 = findViewById(R.id.checkBox2)
        checkBox3 = findViewById(R.id.checkBox3)
        checkBox4 = findViewById(R.id.checkBox4)

        // راه‌اندازی RecyclerView برای نمایش کلمات در 3 ستون
        wordsRecyclerView = findViewById(R.id.wordsRecyclerView)
        wordsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        wordAdapter = WordAdapter(emptyList())
        wordsRecyclerView.adapter = wordAdapter

        progressManager = ProgressManager(this)
        verses = VerseRepository.load(this)

        // بازگشت اصولی به داشبورد
        btnBackToDashboard.setOnClickListener {
            val intent = Intent(this@MainActivity, DashboardActivity::class.java)
            startActivity(intent)
            finish() 
        }

        // عملکرد دکمه نمایش ترجمه
        btnShowTranslation.setOnClickListener {
            cardTranslation.visibility = View.VISIBLE
            cardExample.visibility = View.VISIBLE
            btnShowTranslation.visibility = View.GONE
            playClickSound()
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
                val currentIndex = progressManager.getIndex()
                progressManager.saveIndex(currentIndex + 1)
                Toast.makeText(this@MainActivity, "آفرین! آیه بعدی...", Toast.LENGTH_SHORT).show()
                loadVerse()
            } else {
                if (nextButton.text.contains("بررسی وضعیت")) {
                    syncProgressWithServer()
                } else {
                    sendProgressToServer()
                }
            }
        }

        nextButton.isEnabled = false
        loadVerse()

        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        if (sharedPref.getString("user_id", "") != "guest") {
            syncProgressWithServer()
        }
    }

    private fun checkAllCompleted() {
        if (isResetting) return

        val allChecked = checkBox1.isChecked &&
                checkBox2.isChecked &&
                checkBox3.isChecked &&
                checkBox4.isChecked

        if (allChecked) {
            nextButton.isEnabled = true

            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val isGuest = sharedPref.getString("user_id", "") == "guest"

            nextButton.text = if (isGuest) {
                "رفتن به آیه بعدی (آفلاین)"
            } else {
                "ارسال برای تایید معلم"
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
            Toast.makeText(
                this,
                "شناسه کاربر یافت نشد! لطفاً مجدداً وارد شوید.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        nextButton.isEnabled = false
        nextButton.text = "در حال ارسال..."

        RetrofitClient.instance.updateProgress(studentId, currentIndex.toString())
            .enqueue(object : Callback<ProgressResponse> {
                override fun onResponse(
                    call: Call<ProgressResponse>,
                    response: Response<ProgressResponse>
                ) {
                    if (response.isSuccessful && response.body()?.error == false) {
                        Toast.makeText(
                            this@MainActivity,
                            "با موفقیت ارسال شد.",
                            Toast.LENGTH_SHORT
                        ).show()
                        setUiToPendingState()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "خطا در ارسال اطلاعات",
                            Toast.LENGTH_SHORT
                        ).show()
                        nextButton.isEnabled = true
                        nextButton.text = "ارسال برای تایید معلم"
                    }
                }

                override fun onFailure(
                    call: Call<ProgressResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "خطای شبکه!",
                        Toast.LENGTH_SHORT
                    ).show()
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

        RetrofitClient.instance.getProgress(studentId)
            .enqueue(object : Callback<GetProgressResponse> {
                override fun onResponse(
                    call: Call<GetProgressResponse>,
                    response: Response<GetProgressResponse>
                ) {
                    val progressList = response.body()?.progress

                    if (response.isSuccessful && progressList != null) {
                        var maxApprovedIndex = -1
                        var isCurrentPending = false
                        val currentIndex = progressManager.getIndex()

                        for (item in progressList) {
                            val verseIdx = item.verse_index.toIntOrNull() ?: continue

                            if (item.status == "approved") {
                                if (verseIdx > maxApprovedIndex) {
                                    maxApprovedIndex = verseIdx
                                }
                            } else if (
                                item.status == "pending" &&
                                verseIdx == currentIndex
                            ) {
                                isCurrentPending = true
                            }
                        }

                        if (maxApprovedIndex >= currentIndex) {
                            progressManager.saveIndex(maxApprovedIndex + 1)
                            Toast.makeText(
                                this@MainActivity,
                                "معلم پیشرفت شما را تایید کرد!",
                                Toast.LENGTH_LONG
                            ).show()
                            loadVerse()
                        } else if (isCurrentPending) {
                            setUiToPendingState()
                        } else {
                            checkAllCompleted()
                        }
                    } else {
                        checkAllCompleted()
                    }
                }

                override fun onFailure(
                    call: Call<GetProgressResponse>,
                    t: Throwable
                ) {
                    checkAllCompleted()
                    Toast.makeText(
                        this@MainActivity,
                        "عدم اتصال به سرور جهت همگام‌سازی",
                        Toast.LENGTH_SHORT
                    ).show()
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

        // مخفی کردن ترجمه و مثال برای آیه جدید
        cardTranslation.visibility = View.GONE
        cardExample.visibility = View.GONE
        btnShowTranslation.visibility = View.VISIBLE

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
            
            // نمایش دائم برای پایان مسیر
            cardTranslation.visibility = View.VISIBLE
            cardExample.visibility = View.VISIBLE
            btnShowTranslation.visibility = View.GONE
            wordsRecyclerView.visibility = View.GONE // پنهان کردن لیست کلمات در صفحه پایان
            
            nextButton.isEnabled = false

            checkBox1.isEnabled = false
            checkBox2.isEnabled = false
            checkBox3.isEnabled = false
            checkBox4.isEnabled = false
            return
        }

        val currentVerse = verses[currentIndex]

        verseNumber.text =
            "آیه ${currentVerse.number} - ${currentVerse.surah_reference}"
        arabicText.text = currentVerse.arabic_text
        translation.text = currentVerse.persian_translation

        if (currentVerse.practical_examples.toString().isNotEmpty()) {
            exampleText.text = currentVerse.practical_examples.toString()
        } else {
            exampleText.text = "مثالی وجود ندارد."
        }

        // ===============================================
        // نمایش داده‌های ترجمه کلمه به کلمه در RecyclerView
        // ===============================================
        if (!currentVerse.word_translations.isNullOrEmpty()) {
            wordsRecyclerView.visibility = View.VISIBLE
            wordAdapter.updateData(currentVerse.word_translations)
        } else {
            wordsRecyclerView.visibility = View.GONE
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
