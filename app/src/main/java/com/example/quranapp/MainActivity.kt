package com.example.quranapp

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
        
        // --- اضافه شدن دکمه بازگشت به داشبورد ---
        val btnBackToDashboard = findViewById<Button>(R.id.btnBackToDashboard)
        btnBackToDashboard.setOnClickListener {
            finish() // بستن این صفحه و بازگشت به داشبورد
        }
        // ----------------------------------------
        
        // پیدا کردن چک‌باکس‌ها
        val cb1 = findViewById<CheckBox>(R.id.checkBox1)
        val cb2 = findViewById<CheckBox>(R.id.checkBox2)
        val cb3 = findViewById<CheckBox>(R.id.checkBox3)
        val cb4 = findViewById<CheckBox>(R.id.checkBox4)

        // تابعی برای بررسی اینکه آیا همه چک‌باکس‌ها تیک خورده‌اند یا خیر
        fun checkAllCompleted() {
            val allChecked = cb1.isChecked && cb2.isChecked && cb3.isChecked && cb4.isChecked
            next.isEnabled = allChecked // فعال یا غیرفعال کردن دکمه بر اساس تیک‌ها
            
            if (allChecked) {
                next.text = "آیه بعدی"
            } else {
                next.text = "آیه بعدی (قفل)"
            }
        }

        // اختصاص دادن لیسنر (شنونده) به تمام چک‌باکس‌ها
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
            
            // با رفتن به آیه جدید، چک‌باکس‌ها را ریست می‌کنیم
            cb1.isChecked = false
            cb2.isChecked = false
            cb3.isChecked = false
            cb4.isChecked = false
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
