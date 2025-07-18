package com.example.mytelegramapp.ui.sessions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytelegramapp.data.model.Session
import com.example.mytelegramapp.databinding.ItemSessionBinding

class SessionsAdapter(private var sessions: List<Session>) : RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun updateData(newSessions: List<Session>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    class SessionViewHolder(private val binding: ItemSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: Session) {
            binding.sessionNameText.text = session.fileName
            binding.sessionStatusText.visibility = View.GONE
        }
    }
}
