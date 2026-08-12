package com.example.quranapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
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
    
    private var currentDisplayIndex = -1 // شاخص آیه‌ای که اکنون نمایش داده می‌شود
    private var lastDisplayedIndex: Int = -1 // برای تشخیص تغییر آیه و موضوع
    
    private lateinit var verseNumber: TextView
    private lateinit var categoryText: TextView 
    private lateinit var arabicText: TextView
    private lateinit var translation: TextView
    private lateinit var exampleText: TextView
    
    private lateinit var cardTranslation: CardView
    private lateinit var cardExample: CardView
    private lateinit var btnShowTranslation: Button
    private lateinit var btnWatchVideo: Button // اضافه شده برای دکمه ویدئو
    
    // متغیرهای جدول کلمات
    private lateinit var wordsRecyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    
    private lateinit var nextButton: Button
    private lateinit var btnBackToDashboard: Button
    private lateinit var btnPrevious: Button 
    
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var checkBox3: CheckBox
    private lateinit var checkBox4: CheckBox
    private lateinit var checkBox5: CheckBox
    private lateinit var checkBox6: CheckBox

    private var isResetting = false
    private lateinit var progressManager: ProgressManager
    private var verses: List<Verse> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verseNumber = findViewById(R.id.verseNumber)
        categoryText = findViewById(R.id.categoryText) 
        arabicText = findViewById(R.id.arabicText)
        translation = findViewById(R.id.translation)
        exampleText = findViewById(R.id.exampleText)
        
        cardTranslation = findViewById(R.id.cardTranslation)
        cardExample = findViewById(R.id.cardExample)
        btnShowTranslation = findViewById(R.id.btnShowTranslation)
        btnWatchVideo = findViewById(R.id.btnWatchVideo) // مقداردهی دکمه ویدئو
        
        nextButton = findViewById(R.id.nextButton)
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)
        btnPrevious = findViewById(R.id.btnPrevious) 
        
        checkBox1 = findViewById(R.id.checkBox1)
        checkBox2 = findViewById(R.id.checkBox2)
        checkBox3 = findViewById(R.id.checkBox3)
        checkBox4 = findViewById(R.id.checkBox4)
        checkBox5 = findViewById(R.id.checkBox5)
        checkBox6 = findViewById(R.id.checkBox6)

        // راه‌اندازی RecyclerView برای نمایش کلمات در 3 ستون
        wordsRecyclerView = findViewById(R.id.wordsRecyclerView)
        wordsRecyclerView.layoutManager = GridLayoutManager(this, 3)
        wordAdapter = WordAdapter(emptyList())
        wordsRecyclerView.adapter = wordAdapter

        progressManager = ProgressManager(this)
        verses = VerseRepository.load(this)

        // دریافت شاخص ارسال‌شده از داشبورد (در صورت وجود)
        val extraIndex = intent.getIntExtra("VERSE_INDEX", -1)
        if (extraIndex != -1) {
            currentDisplayIndex = extraIndex
        } else {
            currentDisplayIndex = progressManager.getIndex()
        }

        // بازگشت اصولی به داشبورد
        btnBackToDashboard.setOnClickListener {
            val intent = Intent(this@MainActivity, DashboardActivity::class.java)
            startActivity(intent)
            finish() 
        }

        // عملکرد دکمه نمایش ترجمه (نمایش همه بخش‌های مخفی)
        btnShowTranslation.setOnClickListener {
            cardTranslation.visibility = View.VISIBLE
            cardExample.visibility = View.VISIBLE
            
            // نمایش جدول کلمات در صورت وجود محتوا
            if (wordAdapter.itemCount > 0) {
                wordsRecyclerView.visibility = View.VISIBLE
            }
            
            btnShowTranslation.visibility = View.GONE
            playClickSound()
        }

        // عملکرد دکمه تماشای ویدئو (جدید)
        btnWatchVideo.setOnClickListener {
            if (currentDisplayIndex >= 0 && currentDisplayIndex < verses.size) {
                val videoUrl = verses[currentDisplayIndex].video_url
                if (!videoUrl.isNullOrEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "برنامه‌ای برای باز کردن لینک یافت نشد!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
        checkBox5.setOnCheckedChangeListener(checkListener)
        checkBox6.setOnCheckedChangeListener(checkListener)

        nextButton.setOnClickListener {
            val maxUnlockedIndex = progressManager.getIndex()
            val isReviewMode = currentDisplayIndex < maxUnlockedIndex

            if (isReviewMode) {
                // اگر در حالت مرور هستیم، فقط به آیه بعدی در لیست مرور می‌رویم
                currentDisplayIndex++
                loadVerse()
            } else {
                // اگر در حالت یادگیری آیه جدید هستیم
                val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                val studentId = sharedPref.getString("user_id", "")
                val isGuest = (studentId == "guest")

                if (isGuest) {
                    val currentIndex = progressManager.getIndex()
                    progressManager.saveIndex(currentIndex + 1)
                    currentDisplayIndex = currentIndex + 1
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
                 checkBox4.isChecked &&
                 checkBox5.isChecked && 
                 checkBox6.isChecked    

        if (allChecked) {
            nextButton.isEnabled = true

            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val isGuest = sharedPref.getString("user_id", "") == "guest"
            val maxUnlockedIndex = progressManager.getIndex()
            
            if (currentDisplayIndex < maxUnlockedIndex) {
                 nextButton.text = "آیه بعدی (مرور) ←"
            } else {
                nextButton.text = if (isGuest) {
                    "رفتن به آیه بعدی (آفلاین)"
                } else {
                    "ارسال برای تایید معلم"
                }
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
                            currentDisplayIndex = maxApprovedIndex + 1
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
        checkBox5.isChecked = true
        checkBox6.isChecked = true

        checkBox1.isEnabled = false
        checkBox2.isEnabled = false
        checkBox3.isEnabled = false
        checkBox4.isEnabled = false
        checkBox5.isEnabled = false
        checkBox6.isEnabled = false
        
        isResetting = false

        nextButton.isEnabled = true
        nextButton.text = "بررسی وضعیت تایید (رفرش)"
    }

    private fun loadVerse() {
        if (verses.isEmpty()) return

        isResetting = true
        val maxUnlockedIndex = progressManager.getIndex()
        
        if (currentDisplayIndex == -1) {
            currentDisplayIndex = maxUnlockedIndex
        }

        val currentIndex = currentDisplayIndex
        val isReviewMode = currentIndex < maxUnlockedIndex

        // مدیریت دکمه آیه قبلی
        if (currentIndex > 0) {
            btnPrevious.visibility = View.VISIBLE
            btnPrevious.setOnClickListener {
                currentDisplayIndex--
                loadVerse()
            }
        } else {
            btnPrevious.visibility = View.GONE
        }

        if (currentIndex >= verses.size) {
            verseNumber.text = "پایان مسیر"
            categoryText.visibility = View.GONE 
            arabicText.text = "تبریک! شما همه آیات را یاد گرفتید."
            translation.text = ""
            exampleText.text = ""
            
            cardTranslation.visibility = View.VISIBLE
            cardExample.visibility = View.VISIBLE
            btnShowTranslation.visibility = View.GONE
            btnWatchVideo.visibility = View.GONE // پنهان کردن دکمه ویدئو در پایان مسیر
            wordsRecyclerView.visibility = View.GONE 
            btnPrevious.visibility = View.VISIBLE 
            
            nextButton.isEnabled = false
            checkBox1.isEnabled = false
            checkBox2.isEnabled = false
            checkBox3.isEnabled = false
            checkBox4.isEnabled = false
            checkBox5.isEnabled = false
            checkBox6.isEnabled = false
            
            isResetting = false
            return
        }

        // مخفی کردن بخش‌های ترجمه برای آیه جدید
        cardTranslation.visibility = View.GONE
        cardExample.visibility = View.GONE
        wordsRecyclerView.visibility = View.GONE
        btnShowTranslation.visibility = View.VISIBLE

        if (isReviewMode) {
            checkBox1.isChecked = true; checkBox1.isEnabled = false
            checkBox2.isChecked = true; checkBox2.isEnabled = false
            checkBox3.isChecked = true; checkBox3.isEnabled = false
            checkBox4.isChecked = true; checkBox4.isEnabled = false
            checkBox5.isChecked = true; checkBox5.isEnabled = false
            checkBox6.isChecked = true; checkBox6.isEnabled = false

            nextButton.isEnabled = true
            nextButton.text = "آیه بعدی (مرور) ←"
        } else {
            checkBox1.isChecked = false; checkBox1.isEnabled = true
            checkBox2.isChecked = false; checkBox2.isEnabled = true
            checkBox3.isChecked = false; checkBox3.isEnabled = true
            checkBox4.isChecked = false; checkBox4.isEnabled = true
            checkBox5.isChecked = false; checkBox5.isEnabled = true
            checkBox6.isChecked = false; checkBox6.isEnabled = true
            
            nextButton.isEnabled = false
            nextButton.text = "آیه بعدی"
        }

        isResetting = false

        val currentVerse = verses[currentIndex]
        
        // بررسی وضعیت ویدئو و نمایش دکمه در صورت وجود لینک (اضافه شده)
        if (!currentVerse.video_url.isNullOrEmpty()) {
            btnWatchVideo.visibility = View.VISIBLE
        } else {
            btnWatchVideo.visibility = View.GONE
        }
        
        val newCategory = currentVerse.category?.trim()

        if (lastDisplayedIndex != -1 && currentIndex > lastDisplayedIndex) {
            val oldCategory = verses[lastDisplayedIndex].category?.trim()
            if (!oldCategory.isNullOrEmpty() &&
                !newCategory.isNullOrEmpty() &&
                oldCategory != newCategory
            ) {
                showCategoryChangeDialog(oldCategory, newCategory)
            }
        }
        lastDisplayedIndex = currentIndex
        
        verseNumber.text = "آیه ${currentIndex + 1} - ${currentVerse.surah_reference}"

        // اعمال منطق نمایش دسته‌بندی موضوعی آیه
        if (!newCategory.isNullOrEmpty()) {
            categoryText.text = "موضوع: $newCategory"
            categoryText.visibility = View.VISIBLE
        } else {
            categoryText.visibility = View.GONE
        }
        
        arabicText.text = currentVerse.arabic_text
        translation.text = currentVerse.persian_translation

        if (!currentVerse.practical_examples.isNullOrEmpty()) {
            exampleText.text = currentVerse.practical_examples.joinToString("\n- ", prefix = "- ")
        } else {
            exampleText.text = "مثالی وجود ندارد."
        }
        
        if (!currentVerse.word_translations.isNullOrEmpty()) {
            wordAdapter.updateData(currentVerse.word_translations)
        } else {
            wordAdapter.updateData(emptyList())
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
    
    private fun showCategoryChangeDialog(oldCategory: String, newCategory: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("🎉 تبریک! پایان یک بخش")
            .setMessage("شما آیات مربوط به موضوع «$oldCategory» را با موفقیت به پایان رساندید.\n\nآماده‌اید وارد بخش جدید شوید؟\nموضوع جدید: «$newCategory»")
            .setPositiveButton("بزن بریم!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
