package com.example.quranapp

data class Verse(
    val number:Int,
    val arabic_text:String,
    val persian_translation:String,
    val surah_reference:String,
    val practical_examples:List<String>
)
