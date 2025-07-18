package com.example.mytelegramapp

import android.app.Application
import org.drinkless.td.libcore.telegram.TdApi

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("tdjni")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }
}
