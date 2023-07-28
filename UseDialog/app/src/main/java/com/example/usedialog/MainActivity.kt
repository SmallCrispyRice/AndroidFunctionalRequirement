package com.example.usedialog

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.usedialog.base.BaseActivity
import com.example.usedialog.databinding.ActivityMainBinding
import com.example.usedialog.util.CustomLog

class MainActivity : BaseActivity() {
    companion object{
        const val TAG = "MainActivity"
    }
    private lateinit var binding: ActivityMainBinding

    override fun onStart() {
        super.onStart()
        CustomLog.d(TAG,"onStart")
        initToAlarmDetail()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    /** 跳转报警详情界面 */
    private fun initToAlarmDetail() {
        val type = intent.getStringExtra("task")
        if (type == "toAlarmDetail"){
            val bundle = Bundle()
            bundle.putString("alarmId","1")
            findNavController(R.id.fragment_main_table).navigate(R.id.alarmDetailFragment,bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}