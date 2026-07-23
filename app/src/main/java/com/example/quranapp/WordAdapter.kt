package com.example.quranapp

import android.app.AlertDialog
import android.graphics.Color
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

        // رنگ‌دهی بر اساس قاعده
        if (!word.grammarColor.isNullOrBlank()) {
            try {
                holder.tvArabicWord.setTextColor(Color.parseColor(word.grammarColor))
            } catch (e: Exception) {
                holder.tvArabicWord.setTextColor(Color.BLACK)
            }
        } else {
            holder.tvArabicWord.setTextColor(Color.BLACK)
        }

        // کلیک برای نمایش توضیح
        holder.itemView.setOnClickListener {
            val title = word.grammarRule ?: "توضیح واژه"
            val message = buildString {
                append("واژه: ${word.arabic}\n\n")
                append("ترجمه: ${word.persian}\n\n")
                if (!word.grammarRule.isNullOrBlank()) {
                    append("قاعده: ${word.grammarRule}\n\n")
                }
                if (!word.grammarExplanation.isNullOrBlank()) {
                    append("توضیح: ${word.grammarExplanation}")
                } else {
                    append("توضیحی برای این واژه ثبت نشده است.")
                }
            }

            AlertDialog.Builder(holder.itemView.context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("بستن", null)
                .show()
        }
    }

    override fun getItemCount() = words.size

    fun updateData(newWords: List<WordTranslation>) {
        words = newWords
        notifyDataSetChanged()
    }
}
