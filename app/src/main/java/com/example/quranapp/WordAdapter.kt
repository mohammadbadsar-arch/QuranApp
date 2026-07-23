package com.example.quranapp

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordAdapter(
    private var words: List<WordTranslation> = emptyList()
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

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

        // اعمال رنگ گرامری
        word.grammarColor?.takeIf { it.isNotBlank() }?.let { color ->
            try {
                holder.tvArabicWord.setTextColor(Color.parseColor(color))
            } catch (e: IllegalArgumentException) {
                holder.tvArabicWord.setTextColor(Color.BLACK)
            }
        } ?: holder.tvArabicWord.setTextColor(Color.BLACK)

        // باز کردن توضیحات با کلیک
        holder.itemView.setOnClickListener {
            val explanation = listOfNotNull(
                word.grammarRule?.takeIf { it.isNotBlank() }?.let { "قاعده: $it" },
                word.grammarExplanation?.takeIf { it.isNotBlank() }?.let { "توضیح: $it" }
            ).joinToString("\n\n")

            if (explanation.isNotBlank()) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle(word.arabic)
                    .setMessage(explanation)
                    .setPositiveButton("باشه", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = words.size

    fun updateData(newWords: List<WordTranslation>) {
        words = newWords
        notifyDataSetChanged()
    }
}
