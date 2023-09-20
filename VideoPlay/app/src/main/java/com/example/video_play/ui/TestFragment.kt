package com.example.video_play.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.video_play.R
import com.example.video_play.base.BaseBindingViewFragment
import com.example.video_play.databinding.FragmentTestBinding
import com.example.video_play.util.ToastUtil


class TestFragment : BaseBindingViewFragment<FragmentTestBinding>(R.layout.fragment_test) {
    companion object{
        const val TAG ="TestFragment"
    }
    override fun initBinding(layoutInflater: LayoutInflater): FragmentTestBinding? {
        return FragmentTestBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTitle()
        initView()
    }

    private fun initTitle() {
        binding.title.tvTitle.text = TAG
    }

    private fun initView() {
        binding.title.tvTitle.setOnClickListener {
            ToastUtil.show("AAA")
        }
    }
}