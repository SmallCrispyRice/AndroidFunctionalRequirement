package com.example.video_play.util

import android.widget.Toast
import com.example.video_play.CustomApplication

object ToastUtil {
    fun show(msg:String) {
        Toast.makeText(CustomApplication.context,msg,Toast.LENGTH_SHORT).show()
    }
}