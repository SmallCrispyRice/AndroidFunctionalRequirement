package com.example.usedialog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.usedialog.R
import com.example.usedialog.base.BaseBindingViewFragment
import com.example.usedialog.databinding.FragmentTestBinding


class TestFragment : BaseBindingViewFragment<FragmentTestBinding>(R.layout.fragment_test) {
    companion object{
        const val TAG = "TestFragment"
    }
    override fun initBinding(layoutInflater: LayoutInflater): FragmentTestBinding? {
        return FragmentTestBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.includeTestFragment.tvNow.text = TAG
        binding.btnToAlarmDetail.setOnClickListener {
            findNavController().navigate(R.id.action_testFragment_to_alarmDetailFragment)
        }
    }
}