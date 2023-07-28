package com.example.usedialog.ui

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.usedialog.MainActivity
import com.example.usedialog.R
import com.example.usedialog.databinding.CustomDialogLayoutBinding
import java.lang.Exception

class AlarmDialogFragment: DialogFragment() {
    private lateinit var binding:CustomDialogLayoutBinding
    private var animator:ObjectAnimator? = null

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CustomDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.viewAlarmDialogBg
        startAnimation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#88000000")))//去掉DialogFragment的边距
        }
        dialog.setCancelable(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(animator?.isStarted == true){
            animator?.end()
        }
    }

    private fun initView() {
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }

        binding.btnDialogNav.setOnClickListener {
            if(context is MainActivity){
                val bundle = Bundle()
                bundle.putString("alarmId","1")
                findNavController().navigate(R.id.alarmDetailFragment,bundle)
            }else{
                val intent = Intent(context,MainActivity::class.java)
                intent.putExtra("task","toAlarmDetail")
                startActivity(intent)
            }
            dismiss()
        }
    }

    private fun startAnimation() {
        animator = ObjectAnimator.ofFloat(binding.viewAlarmDialogBg, "alpha", 0f, 0.6f, 0f, 0.6f, 0f)
        animator?.duration = 1200
        animator?.interpolator = AccelerateInterpolator()
        animator?.start()
    }
}