package com.example.mytelegramapp.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mytelegramapp.databinding.FragmentSendBinding
import com.example.mytelegramapp.ui.MainViewModel

class SendFragment : Fragment() {

    private var _binding: FragmentSendBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.Factory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendButton.setOnClickListener {
            val target = binding.targetInput.text.toString()
            val message = binding.messageInput.text.toString()
            val countStr = binding.countInput.text.toString()
            val delayStr = binding.delayInput.text.toString()

            if (target.isBlank() || message.isBlank() || countStr.isBlank() || delayStr.isBlank()) {
                Toast.makeText(context, "Будь ласка, заповніть усі поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val count = countStr.toInt()
                val delay = delayStr.toFloat()
                mainViewModel.sendMessages(target, message, count, delay)
                Toast.makeText(context, "Запуск розсилки...", Toast.LENGTH_SHORT).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Кількість та затримка мають бути числами", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
