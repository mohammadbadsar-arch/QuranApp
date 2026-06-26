package com.example.quranapp

import android.media.MediaPlayer // <-- این خط اضافه شد
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var verses: List<Verse>
    lateinit var progress: ProgressManager
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verses = VerseRepository.load(this)
        progress = ProgressManager(this)
        index = progress.getIndex()

        val num = findViewById<TextView>(R.id.verseNumber)
        val ar = findViewById<TextView>(R.id.arabicText)
        val tr = findViewById<TextView>(R.id.translation)
        val ex = findViewById<TextView>(R.id.exampleText)
        val next = findViewById<Button>(R.id.nextButton)
        
        val btnBackToDashboard = findViewById<Button>(R.id.btnBackToDashboard)
        btnBackToDashboard.setOnClickListener {
            finish() 
        }
        
        val cb1 = findViewById<CheckBox>(R.id.checkBox1)
        val cb2 = findViewById<CheckBox>(R.id.checkBox2)
        val cb3 = findViewById<CheckBox>(R.id.checkBox3)
        val cb4 = findViewById<CheckBox>(R.id.checkBox4)

        // تابعی برای بررسی اینکه آیا همه چک‌باکس‌ها تیک خورده‌اند یا خیر
        fun checkAllCompleted() {
            val allChecked = cb1.isChecked && cb2.isChecked && cb3.isChecked && cb4.isChecked
            next.isEnabled = allChecked 
            
            if (allChecked) {
                next.text = "آیه بعدی"
                // --- پخش افکت صوتی هنگام باز شدن قفل ---
                try {
                    val mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.success)
                    mediaPlayer.start()
                    mediaPlayer.setOnCompletionListener { it.release() } // آزادسازی حافظه بعد از اتمام صدا
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // ----------------------------------------
            } else {
                next.text = "آیه بعدی (قفل)"
            }
        }

        val listener = CompoundButton.OnCheckedChangeListener { _, _ -> checkAllCompleted() }
        cb1.setOnCheckedChangeListener(listener)
        cb2.setOnCheckedChangeListener(listener)
        cb3.setOnCheckedChangeListener(listener)
        cb4.setOnCheckedChangeListener(listener)

        fun show() {
            val v = verses[index]
            num.text = "آیه " + v.number + " - " + v.surah_reference
            ar.text = v.arabic_text
            tr.text = v.persian_translation
            ex.text = v.practical_examples.joinToString("\n")
            
            // با رفتن به آیه جدید، چک‌باکس‌ها را ریست می‌کنیم (بدون اجرای لیسنر برای جلوگیری از پخش صدای اشتباه)
            cb1.setOnCheckedChangeListener(null)
            cb2.setOnCheckedChangeListener(null)
            cb3.setOnCheckedChangeListener(null)
            cb4.setOnCheckedChangeListener(null)

            cb1.isChecked = false
            cb2.isChecked = false
            cb3.isChecked = false
            cb4.isChecked = false

            cb1.setOnCheckedChangeListener(listener)
            cb2.setOnCheckedChangeListener(listener)
            cb3.setOnCheckedChangeListener(listener)
            cb4.setOnCheckedChangeListener(listener)
            
            checkAllCompleted()
        }

        show()

        next.setOnClickListener {
            if (index < verses.size - 1) {
                index++
                progress.saveIndex(index)
                show()
            } else {
                Toast.makeText(this, "همه آیات تمام شد", Toast.LENGTH_LONG).show()
            }
        }
    }
}
