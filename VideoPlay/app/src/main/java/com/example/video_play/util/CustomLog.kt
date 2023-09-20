package com.example.video_play.util

import android.util.Log
import com.example.video_play.BuildConfig

object CustomLog {
    private var isDebug = BuildConfig.DEBUG
    private var defaultTag = "测试"

    fun d(tag:String,message:String){
        if (isDebug){
            Log.d(tag,message)
        }
    }

    fun d(message:String){
        if (isDebug){
            Log.d(defaultTag,message)
        }
    }

    fun i(tag:String,message:String){
        if (isDebug){
            Log.i(tag,message)
        }
    }

    fun e(tag:String,message:String){
        if (isDebug){
            Log.e(tag,message)
        }
    }

    fun v(tag:String,message:String){
        if (isDebug){
            Log.v(tag,message)
        }
    }
}