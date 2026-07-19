package com.example.quranapp

// مدل جدید برای کلمات
data class WordTranslation(
    val arabic: String,
    val persian: String
)

// مدل بروزشده آیه
data class Verse(
    val number: Int,
    val arabic_text: String,
    val persian_translation: String,
    val surah_reference: String,
    val practical_examples: List<String>,
    val word_translations: List<WordTranslation>? = null // فیلد جدید اضافه شد
)
