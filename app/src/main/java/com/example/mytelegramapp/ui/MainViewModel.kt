package com.example.mytelegramapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mytelegramapp.data.TelegramRepository
import com.example.mytelegramapp.data.model.Session
import org.drinkless.td.libcore.telegram.TdApi

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val logMessages: LiveData<String> = TelegramRepository.logMessages
    val sessions: LiveData<List<Session>> = TelegramRepository.sessions
    val authState: LiveData<TdApi.AuthorizationState> = TelegramRepository.authState

    init {
        TelegramRepository.initialize(application)
        TelegramRepository.loadSessions(application)
    }

    fun sendPhoneNumber(phone: String) {
        TelegramRepository.sendPhoneNumber(phone)
    }

    fun sendCode(code: String) {
        TelegramRepository.sendCode(code)
    }

    fun sendPassword(password: String) {
        TelegramRepository.sendPassword(password)
    }

    fun downloadSessions() {
        TelegramRepository.downloadSessions(getApplication())
    }

    fun verifyAllSessions() {
        TelegramRepository.verifyAllSessions(getApplication())
    }

    fun sendMessages(target: String, message: String, count: Int, delay: Float) {
        TelegramRepository.sendMessagesFromAllSessions(getApplication(), target, message, count, delay)
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
