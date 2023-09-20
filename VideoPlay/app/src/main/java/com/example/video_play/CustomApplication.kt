package com.example.video_play

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class CustomApplication: Application() {
    companion object{
        @SuppressLint("CustomContext")
        lateinit var context: Context //可以在任意地方使用context CustomApplication.context
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}