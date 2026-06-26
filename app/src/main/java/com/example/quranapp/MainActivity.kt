package com.example.quranapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var verseTextView: TextView
    private lateinit var nextButton: Button
    private lateinit var btnBackToDashboard: Button
    
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var checkBox3: CheckBox
    private lateinit var checkBox4: CheckBox

    // این متغیر برای این است که وقتی آیه عوض می‌شود و تیک‌ها به صورت خودکار برداشته می‌شوند، صدای کلیک پخش نشود
    private var isResetting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // متصل کردن المان‌های UI
        verseTextView = findViewById(R.id.verseTextView)
        nextButton = findViewById(R.id.nextButton)
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)
        
        checkBox1 = findViewById(R.id.checkBox1)
        checkBox2 = findViewById(R.id.checkBox2)
        checkBox3 = findViewById(R.id.checkBox3)
        checkBox4 = findViewById(R.id.checkBox4)

        // دکمه بازگشت به داشبورد
        btnBackToDashboard.setOnClickListener {
            finish() // بستن این صفحه و برگشت به داشبورد
        }

        // تنظیم Listener برای چک‌باکس‌ها
        val checkListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            // اگر چک‌باکس تیک خورد و سیستم در حال ریست کردن خودکار نبود، صدای کلیک بیاید
            if (isChecked && !isResetting) {
                playClickSound()
            }
            checkAllCompleted()
        }

        checkBox1.setOnCheckedChangeListener(checkListener)
        checkBox2.setOnCheckedChangeListener(checkListener)
        checkBox3.setOnCheckedChangeListener(checkListener)
        checkBox4.setOnCheckedChangeListener(checkListener)

        // دکمه آیه بعدی
        nextButton.setOnClickListener {
            loadNextVerse()
        }

        // در شروع برنامه دکمه بعدی غیرفعال باشد و آیه اول لود شود
        nextButton.isEnabled = false
        loadNextVerse()
    }

    private fun checkAllCompleted() {
        // اگر سیستم در حال پاک کردن تیک‌هاست، این تابع فعلاً کاری نکند
        if (isResetting) return

        val allChecked = checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked
        
        if (allChecked) {
            nextButton.isEnabled = true
            playSuccessSound()
            
            // ذخیره پیشرفت در داشبورد
            val currentProgress = ProgressManager.getProgress(this)
            ProgressManager.saveProgress(this, currentProgress + 1)
        } else {
            nextButton.isEnabled = false
        }
    }

    private fun loadNextVerse() {
        // روشن کردن حالت ریست تا صدای کلیک و موفقیت الکی پخش نشود
        isResetting = true

        // برداشتن تیک چک‌باکس‌ها برای آیه جدید
        checkBox1.isChecked = false
        checkBox2.isChecked = false
        checkBox3.isChecked = false
        checkBox4.isChecked = false
        
        nextButton.isEnabled = false
        
        // خاموش کردن حالت ریست بعد از اتمام پاکسازی
        isResetting = false

        // اینجا می‌توانید کد لود کردن متن آیه از Repository را قرار دهید
        // مثال:
        // val verse = VerseRepository.getNextVerse()
        // verseTextView.text = verse.text
        verseTextView.text = "اینجا متن آیه بعدی نمایش داده می‌شود..."
    }

    private fun playClickSound() {
        try {
            // پخش صدای کلیک
            val mediaPlayer = MediaPlayer.create(this, R.raw.click)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSuccessSound() {
        try {
            // پخش صدای موفقیت و تکمیل شدن هر ۴ مرحله
            val mediaPlayer = MediaPlayer.create(this, R.raw.success)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
