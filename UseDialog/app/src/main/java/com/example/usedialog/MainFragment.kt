package com.example.usedialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.usedialog.base.BaseFragment
import com.example.usedialog.bean.NavItem
import com.example.usedialog.databinding.FragmentMainBinding


class MainFragment : BaseFragment() {
    companion object{
        const val TAG = "MainFragment"
    }
    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val navItems = mutableListOf<NavItem>()
        navItems.add(NavItem("测试",R.id.action_mainFragment_to_testFragment))
        val adapter = ShowAdapter(navItems,findNavController())

        binding?.apply {
            rvMainShow.adapter = adapter
            includeMainFragment.tvNow.text = TAG
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.unbind()
    }
}