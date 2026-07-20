package com.example.quranapp

import com.google.gson.annotations.SerializedName

data class Verse(
    val id: Int? = null, // در فایل متنی شما فیلد id وجود دارد
    val number: Int,
    @SerializedName("arabic_text")
    val arabic_text: String,
    @SerializedName("persian_translation")
    val persian_translation: String,
    @SerializedName("surah_reference")
    val surah_reference: String,
    @SerializedName("practical_examples")
    val practical_examples: List<String>, 
    @SerializedName("word_by_word")
    val word_translations: List<WordTranslation>? = null 
)

data class WordTranslation(
    val arabic: String,
    val persian: String
)
