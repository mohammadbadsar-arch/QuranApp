package com.example.quranapp

import com.google.gson.annotations.SerializedName

data class Verse(
    val id: Int? = null,
    val category: String? = null,
    @SerializedName("arabic_text")
    val arabic_text: String = "",
    @SerializedName("persian_translation")
    val persian_translation: String = "",
    @SerializedName("surah_reference")
    val surah_reference: String = "",
    @SerializedName("practical_examples")
    val practical_examples: List<String> = emptyList(),
    @SerializedName("word_by_word")
    val word_translations: List<WordTranslation> = emptyList(),
    @SerializedName("video_url")
    val video_url: String? = null
)

data class WordTranslation(
    val arabic: String = "",
    val persian: String = "",
    @SerializedName("grammar_rule")
    val grammarRule: String? = null,
    @SerializedName("grammar_color")
    val grammarColor: String? = null,
    @SerializedName("grammar_explanation")
    val grammarExplanation: String? = null
)
