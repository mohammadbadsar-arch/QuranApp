package com.example.quranapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

        // متصل کردن المان‌های UI
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

        // مقداردهی منیجر و دریافت آیات از آبجکت سینگلتون
        progressManager = ProgressManager(this)
        verses = VerseRepository.load(this) // اصلاح شد

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
            // اگر متد در ProgressManager شما نام دیگری دارد، اینجا تغییر دهید
            val currentIndex = progressManager.getCurrentVerseIndex() 
            if (currentIndex < verses.size - 1) {
                progressManager.saveCurrentVerseIndex(currentIndex + 1)
                loadVerse()
            }
        }

        nextButton.isEnabled = false
        loadVerse()
    }

    private fun checkAllCompleted() {
        if (isResetting) return

        val allChecked = checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked
        
        if (allChecked) {
            nextButton.isEnabled = true
            playSuccessSound()
        } else {
            nextButton.isEnabled = false
        }
    }

    private fun loadVerse() {
        if (verses.isEmpty()) return

        isResetting = true

        checkBox1.isChecked = false
        checkBox2.isChecked = false
        checkBox3.isChecked = false
        checkBox4.isChecked = false
        
        nextButton.isEnabled = false
        
        isResetting = false

        // اگر متد دریافت اندیس متفاوت است، نام آن را بروز کنید
        val currentIndex = progressManager.getCurrentVerseIndex() 
        
        // اطمینان از اینکه اندیس از محدوده خارج نشود
        if (currentIndex >= verses.size) {
           return 
        }

        val currentVerse = verses[currentIndex]

        // نمایش اطلاعات آیه
        verseNumber.text = "آیه ${currentVerse.number} - ${currentVerse.surah_reference}"
        arabicText.text = currentVerse.arabic_text
        translation.text = currentVerse.persian_translation
        
        // نمایش مثال‌های کاربردی
        // توجه: اگر در فایل Verse.kt متغیر practical_examples از نوع String است، متد joinToString را پاک کنید
        // و مستقیما بنویسید: exampleText.text = currentVerse.practical_examples
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
