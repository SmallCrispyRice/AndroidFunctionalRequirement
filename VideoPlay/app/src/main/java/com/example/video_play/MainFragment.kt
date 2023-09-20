package com.example.video_play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.video_play.base.BaseFragment
import com.example.video_play.bean.NavItem
import com.example.video_play.databinding.FragmentMainBinding


class MainFragment : BaseFragment() {
    companion object{
        const val TAG = "MainFragment"
    }
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val navItems = mutableListOf<NavItem>()
        navItems.add(NavItem("测试", R.id.action_mainFragment_to_testFragment))
        navItems.add(NavItem("使用ExoPlayer视频播放", R.id.action_mainFragment_to_exoVideoActivity))
        val adapter = ShowAdapter(navItems,findNavController())
        binding.rvMainShow.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}