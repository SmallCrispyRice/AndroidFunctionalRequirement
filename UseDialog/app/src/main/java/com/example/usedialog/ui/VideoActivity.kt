package com.example.usedialog.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.usedialog.R
import com.example.usedialog.base.BaseActivity
import com.example.usedialog.databinding.ActivityVideoBinding

class VideoActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_video)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }

    private fun initView() {
        binding.btnSwitchOrientation.setOnClickListener {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT){
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }else{
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }
}