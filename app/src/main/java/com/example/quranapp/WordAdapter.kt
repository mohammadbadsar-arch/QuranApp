package com.example.quranapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordAdapter(private var words: List<WordTranslation>) :
    RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvArabicWord: TextView = view.findViewById(R.id.tvArabicWord)
        val tvPersianWord: TextView = view.findViewById(R.id.tvPersianWord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.tvArabicWord.text = word.arabic
        holder.tvPersianWord.text = word.persian
    }

    override fun getItemCount() = words.size

    fun updateData(newWords: List<WordTranslation>) {
        words = newWords
        notifyDataSetChanged()
    }
}
