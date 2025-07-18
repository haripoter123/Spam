package com.example.mytelegramapp.ui.verify

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytelegramapp.data.model.Session
import com.example.mytelegramapp.data.model.VerificationStatus
import com.example.mytelegramapp.databinding.ItemSessionBinding

class VerifyAdapter(private var sessions: List<Session>) : RecyclerView.Adapter<VerifyAdapter.VerifyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerifyViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VerifyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerifyViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun updateData(newSessions: List<Session>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    class VerifyViewHolder(private val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: Session) {
            binding.sessionNameText.text = session.fileName

            when (session.status) {
                VerificationStatus.NOT_VERIFIED -> {
                    binding.sessionStatusText.text = "Не перевірено"
                    binding.sessionStatusText.setTextColor(Color.parseColor("#FFC107")) // Жовтий
                }
                VerificationStatus.VERIFYING -> {
                    binding.sessionStatusText.text = "Перевірка..."
                    binding.sessionStatusText.setTextColor(Color.parseColor("#03A9F4")) // Синій
                }
                VerificationStatus.VALID -> {
                    binding.sessionStatusText.text = "Валідна"
                    binding.sessionStatusText.setTextColor(Color.parseColor("#4CAF50")) // Зелений
                }
                VerificationStatus.INVALID -> {
                    binding.sessionStatusText.text = "Невалідна"
                    binding.sessionStatusText.setTextColor(Color.parseColor("#F44336")) // Червоний
                }
            }
        }
    }
}
