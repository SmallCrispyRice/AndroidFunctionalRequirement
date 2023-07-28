package com.example.usedialog.util

import android.widget.Toast
import com.example.usedialog.CustomApplication

object ToastUtil {
    fun show(msg:String) {
        Toast.makeText(CustomApplication.context,msg,Toast.LENGTH_SHORT).show()
    }
}