package com.example.usedialog.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.usedialog.R
import com.example.usedialog.base.BaseBindingViewFragment
import com.example.usedialog.databinding.FragmentAlarmDetailBinding


class AlarmDetailFragment : BaseBindingViewFragment<FragmentAlarmDetailBinding>(R.layout.fragment_alarm_detail) {
    companion object{
        const val TAG = "AlarmDetailFragment"
    }

    override fun initBinding(layoutInflater: LayoutInflater): FragmentAlarmDetailBinding? {
        return FragmentAlarmDetailBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.includeAlarmDetailFragment.tvNow.text = TAG
        val alarmId = arguments?.getString("alarmId")
        binding.tvAlarmDetail.text = "报警id:${alarmId}"
        binding.btnToVideo.setOnClickListener {
            startActivity(Intent(requireActivity(),VideoActivity::class.java))
        }
    }
}