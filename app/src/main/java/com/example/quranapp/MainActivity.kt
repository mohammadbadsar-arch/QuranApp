package com.example.quranapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // تغییر نام متغیر به arabicText برای همخوانی با activity_main.xml
    private lateinit var arabicText: TextView
    private lateinit var nextButton: Button
    private lateinit var btnBackToDashboard: Button
    
    private lateinit var checkBox1: CheckBox
    private lateinit var checkBox2: CheckBox
    private lateinit var checkBox3: CheckBox
    private lateinit var checkBox4: CheckBox

    private var isResetting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // متصل کردن المان‌های UI (استفاده از R.id.arabicText)
        arabicText = findViewById(R.id.arabicText)
        nextButton = findViewById(R.id.nextButton)
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)
        
        checkBox1 = findViewById(R.id.checkBox1)
        checkBox2 = findViewById(R.id.checkBox2)
        checkBox3 = findViewById(R.id.checkBox3)
        checkBox4 = findViewById(R.id.checkBox4)

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
            loadNextVerse()
        }

        nextButton.isEnabled = false
        loadNextVerse()
    }

       private fun checkAllCompleted() {
        if (isResetting) return

        val allChecked = checkBox1.isChecked && checkBox2.isChecked && checkBox3.isChecked && checkBox4.isChecked
        
        if (allChecked) {
            nextButton.isEnabled = true
            playSuccessSound()
            
            // اصلاح نحوه فراخوانی ProgressManager
            val progressManager = ProgressManager(this)
            val currentIndex = progressManager.getIndex()
            progressManager.saveIndex(currentIndex + 1)
            
        } else {
            nextButton.isEnabled = false
        }
    }


    private fun loadNextVerse() {
        isResetting = true

        checkBox1.isChecked = false
        checkBox2.isChecked = false
        checkBox3.isChecked = false
        checkBox4.isChecked = false
        
        nextButton.isEnabled = false
        
        isResetting = false

        // تخصیص متن به arabicText
        arabicText.text = "اینجا متن آیه بعدی نمایش داده می‌شود..."
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
