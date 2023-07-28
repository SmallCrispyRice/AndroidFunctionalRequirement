package com.example.usedialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.example.usedialog.util.CustomLog

class CustomApplication: Application(),Application.ActivityLifecycleCallbacks {
    companion object{
        const val TAG = "CustomApplication"
        @SuppressLint("CustomContext")
        lateinit var context: Context //可以在任意地方使用context CustomApplication.context
    }

    private var activityCount = 0
    private var nowActivityName:String? = null
    private var isInBackground = true

    fun getNowActivityName(): String? {
        return nowActivityName
    }

    fun getIsInBackground():Boolean{
        return isInBackground
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        activityCount++
        if (isInBackground){
            isInBackground = false
        }
        nowActivityName = activity.javaClass.name
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount == 0 && !isInBackground){
            isInBackground = true
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}