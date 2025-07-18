package com.example.mytelegramapp.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mytelegramapp.databinding.FragmentLoginBinding
import com.example.mytelegramapp.ui.MainViewModel
import org.drinkless.td.libcore.telegram.TdApi

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by activityViewModels {
        MainViewModel.Factory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.authState.observe(viewLifecycleOwner) { state ->
            if (state is TdApi.AuthorizationStateReady) {
                Toast.makeText(context, "Авторизация успешна!", Toast.LENGTH_SHORT).show()
                binding.phoneInput.text?.clear()
                binding.codeInput.text?.clear()
                binding.passwordInput.text?.clear()
            }
        }

        binding.getCodeButton.setOnClickListener {
            val phone = binding.phoneInput.text.toString()
            if (phone.isNotBlank()) {
                mainViewModel.sendPhoneNumber(phone)
            } else {
                Toast.makeText(context, "Введите номер телефона", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginButton.setOnClickListener {
            val code = binding.codeInput.text.toString()
            val password = binding.passwordInput.text.toString()

            when {
                code.isNotBlank() -> mainViewModel.sendCode(code)
                password.isNotBlank() -> mainViewModel.sendPassword(password)
                else -> Toast.makeText(context, "Введите код или пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
