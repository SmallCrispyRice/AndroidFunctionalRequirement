package com.example.video_play

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.video_play.base.BaseActivity
import com.example.video_play.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
    }

    private fun initView() {

    }
}