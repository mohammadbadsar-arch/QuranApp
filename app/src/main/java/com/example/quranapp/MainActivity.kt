package com.example.quranapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var verses: List<Verse>
    lateinit var progress: ProgressManager
    lateinit var prefs: SharedPreferences
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verses = VerseRepository.load(this)
        progress = ProgressManager(this)
        index = progress.getIndex()
        
        // تنظیمات مربوط به علاقه‌مندی‌ها
        prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)

        val num = findViewById<TextView>(R.id.verseNumber)
        val ar = findViewById<TextView>(R.id.arabicText)
        val tr = findViewById<TextView>(R.id.translation)
        val ex = findViewById<TextView>(R.id.exampleText)
        val next = findViewById<Button>(R.id.nextButton)
        val done = findViewById<Button>(R.id.doneButton)
        val favBtn = findViewById<ImageButton>(R.id.favButton)

        fun updateFavoriteIcon(verseNumber: Int) {
            val isFav = prefs.getBoolean(verseNumber.toString(), false)
            if (isFav) {
                favBtn.setImageResource(android.R.drawable.btn_star_big_on)
            } else {
                favBtn.setImageResource(android.R.drawable.btn_star_big_off)
            }
        }

        fun show() {
            val v = verses[index]
            num.text = "آیه " + v.number + " - " + v.surah_reference
            ar.text = v.arabic_text
            tr.text = v.persian_translation
            ex.text = v.practical_examples.joinToString("\n")
            
            updateFavoriteIcon(v.number)
        }

        show()

        // تغییر وضعیت علاقه‌مندی
        favBtn.setOnClickListener {
            val verseNumber = verses[index].number.toString()
            val isCurrentlyFav = prefs.getBoolean(verseNumber, false)
            
            prefs.edit().putBoolean(verseNumber, !isCurrentlyFav).apply()
            updateFavoriteIcon(verses[index].number)
            
            val msg = if (!isCurrentlyFav) "به علاقه‌مندی‌ها اضافه شد ❤️" else "از علاقه‌مندی‌ها حذف شد 💔"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        done.setOnClickListener {
            Toast.makeText(this, "عالی بود! ✅", Toast.LENGTH_SHORT).show()
            // اینجا بعدا سیستم امتیازدهی اضافه می‌شود
        }

        next.setOnClickListener {
            if (index < verses.size - 1) {
                index++
                progress.saveIndex(index)
                show()
            } else {
                Toast.makeText(this, "همه آیات تمام شد 🎉", Toast.LENGTH_LONG).show()
            }
        }
    }
}
