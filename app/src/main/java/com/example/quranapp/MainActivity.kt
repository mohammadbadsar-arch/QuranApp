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
        
        // پیدا کردن چک‌باکس‌ها
        val cbRead = findViewById<CheckBox>(R.id.cbRead)
        val cbTranslate = findViewById<CheckBox>(R.id.cbTranslate)
        val cbExample = findViewById<CheckBox>(R.id.cbExample)
        val cbAction = findViewById<CheckBox>(R.id.cbAction)
        
        val next = findViewById<Button>(R.id.nextButton)
        val done = findViewById<Button>(R.id.doneButton)

        // تابعی برای بررسی وضعیت چک‌باکس‌ها
        fun checkProgress() {
            val isAllChecked = cbRead.isChecked && cbTranslate.isChecked && cbExample.isChecked && cbAction.isChecked
            next.isEnabled = isAllChecked
            next.text = if (isAllChecked) "آیه بعدی" else "آیه بعدی (قفل)"
        }

        // اختصاص دادن Listener به همه چک‌باکس‌ها
        val checkListener = CompoundButton.OnCheckedChangeListener { _, _ -> checkProgress() }
        cbRead.setOnCheckedChangeListener(checkListener)
        cbTranslate.setOnCheckedChangeListener(checkListener)
        cbExample.setOnCheckedChangeListener(checkListener)
        cbAction.setOnCheckedChangeListener(checkListener)

        fun show() {
            val v = verses[index]
            num.text = "آیه " + v.number + " - " + v.surah_reference
            ar.text = v.arabic_text
            tr.text = v.persian_translation
            ex.text = v.practical_examples.joinToString("\n")
            
            // ریست کردن چک‌باکس‌ها برای آیه جدید
            cbRead.isChecked = false
            cbTranslate.isChecked = false
            cbExample.isChecked = false
            cbAction.isChecked = false
            next.isEnabled = false
            next.text = "آیه بعدی (قفل)"
        }

        show()

        done.setOnClickListener {
            if (cbRead.isChecked && cbTranslate.isChecked && cbExample.isChecked && cbAction.isChecked) {
                Toast.makeText(this, "آفرین! مراحل این آیه کامل شد ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "لطفاً ابتدا تمام ۴ مرحله را تیک بزنید.", Toast.LENGTH_SHORT).show()
            }
        }

        next.setOnClickListener {
            if (index < verses.size - 1) {
                index++
                progress.saveIndex(index)
                show()
            } else {
                Toast.makeText(this, "تبریک! تمام آیات به پایان رسید.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
