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
        val done = findViewById<Button>(R.id.doneButton)
        
        // پیدا کردن المان‌های پیشرفت
        val progressStatsText = findViewById<TextView>(R.id.progressStatsText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        fun show() {
            val v = verses[index]
            num.text = "آیه " + v.number + " - " + v.surah_reference
            ar.text = v.arabic_text
            tr.text = v.persian_translation
            ex.text = v.practical_examples.joinToString("\n")
            
            // --- کدهای محاسبه و نمایش پیشرفت ---
            val totalVerses = verses.size
            val passedVerses = index // چون ایندکس از صفر شروع می‌شود، برابر با تعداد آیات رد شده است
            val remainingVerses = totalVerses - passedVerses
            val percentage = if (totalVerses > 0) (passedVerses * 100) / totalVerses else 0
            
            // نمایش متن آمار
            progressStatsText.text = "گذرانده: $passedVerses | باقیمانده: $remainingVerses | پیشرفت: $percentage%"
            
            // پر کردن نوار پیشرفت
            progressBar.progress = percentage
        }

        show()

        done.setOnClickListener {
            Toast.makeText(this, "انجام شد ✅", Toast.LENGTH_SHORT).show()
        }

        next.setOnClickListener {
            if (index < verses.size - 1) {
                index++
                progress.saveIndex(index)
                show()
            } else {
                Toast.makeText(this, "همه آیات تمام شد", Toast.LENGTH_LONG).show()
                // تنظیم پیشرفت روی 100% در پایان
                progressBar.progress = 100
                progressStatsText.text = "گذرانده: ${verses.size} | باقیمانده: 0 | پیشرفت: 100%"
            }
        }
    }
}
