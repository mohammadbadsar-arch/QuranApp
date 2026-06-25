package com.example.quranapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity:AppCompatActivity(){

    lateinit var verses:List<Verse>
    lateinit var progress:ProgressManager
    var index=0

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verses=VerseRepository.load(this)
        progress=ProgressManager(this)
        index=progress.getIndex()

        val num=findViewById<TextView>(R.id.verseNumber)
        val ar=findViewById<TextView>(R.id.arabicText)
        val tr=findViewById<TextView>(R.id.translation)
        val ex=findViewById<TextView>(R.id.exampleText)
        val next=findViewById<Button>(R.id.nextButton)
        val done=findViewById<Button>(R.id.doneButton)

        // --- تعریف چک‌باکس‌ها (مطمئن شوید این آیدی‌ها در XML شما وجود دارند) ---
        val cb1 = findViewById<CheckBox>(R.id.checkBox1)
        val cb2 = findViewById<CheckBox>(R.id.checkBox2)
        val cb3 = findViewById<CheckBox>(R.id.checkBox3)
        val cb4 = findViewById<CheckBox>(R.id.checkBox4)

        fun checkCheckboxes() {
            if (cb1.isChecked && cb2.isChecked && cb3.isChecked && cb4.isChecked) {
                next.isEnabled = true
                next.text = "آیه بعدی"
            } else {
                next.isEnabled = false
                next.text = "آیه بعدی (قفل)"
            }
        }

        // بررسی تغییر وضعیت چک‌باکس‌ها
        cb1.setOnCheckedChangeListener { _, _ -> checkCheckboxes() }
        cb2.setOnCheckedChangeListener { _, _ -> checkCheckboxes() }
        cb3.setOnCheckedChangeListener { _, _ -> checkCheckboxes() }
        cb4.setOnCheckedChangeListener { _, _ -> checkCheckboxes() }
        // ----------------------------------------------------------------------

        fun show(){
            val v=verses[index]
            num.text="آیه "+v.number+" - "+v.surah_reference
            ar.text=v.arabic_text
            tr.text=v.persian_translation
            ex.text=v.practical_examples.joinToString("\n")
            
            // هنگام نمایش آیه جدید، تمام تیک‌ها برداشته و دکمه دوباره قفل شود
            cb1.isChecked = false
            cb2.isChecked = false
            cb3.isChecked = false
            cb4.isChecked = false
            checkCheckboxes()
        }

        show()

        done.setOnClickListener{
            Toast.makeText(this,"انجام شد ✅",Toast.LENGTH_SHORT).show()
        }

        next.setOnClickListener{
            if(index<verses.size-1){
                index++
                progress.saveIndex(index)
                show()
            }else{
                Toast.makeText(this,"همه آیات تمام شد",Toast.LENGTH_LONG).show()
            }
        }
    }
}
