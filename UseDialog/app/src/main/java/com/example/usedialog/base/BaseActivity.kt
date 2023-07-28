package com.example.usedialog.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.usedialog.CustomApplication
import com.example.usedialog.MainActivity
import com.example.usedialog.R
import com.example.usedialog.service.BackgroundService
import com.example.usedialog.ui.AlarmDialogFragment
import com.example.usedialog.util.CustomLog
/** message
 * 关于弹框，
 * 目前通知在定时方法内，如果在onPause关闭定时，也就不能弹通知了。*/
open class BaseActivity: AppCompatActivity() {
    companion object{
        const val TAG = "BaseActivity"
    }
    private var alarmCount = 0
    private val handler = Handler(Looper.myLooper()!!)//需要更进一步了解Handler原理
    //为了关闭通知，manager放在外面
    private val notificationId = 1
    private var alarmDialogFragment: AlarmDialogFragment? = null
    private var notificationManager:NotificationManager? = null
    private var bgServiceIntent:Intent? = null//前台服务

    private var nowClassName = ""

    /** 弹框定时任务 */
    private val dialogRunnable = object : Runnable {
        override fun run() {
            //在定时方法里面 javaClass.simpleName 不能获取当前所处Activity的名称
            if (nowClassName == "VideoActivity"){ //视频界面不弹弹框
                CustomLog.d(TAG,"不使用弹框 ${nowClassName}")
            }else{
                CustomLog.d(TAG,"使用弹框 ${nowClassName}")
                useDialog()//如果下面添加useDialog()方法会报alarmDialogFragment已经存在的错误
                handler.postDelayed(this, 10000)
            }
        }
    }

    /** 通知定时任务 */
    private val notificationRunnable = object :Runnable{
        override fun run() {
            useNotificationPI()
            handler.postDelayed(this,10000)
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        initWindow()
        return super.onCreateView(name, context, attrs)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        CustomLog.d(TAG,"onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) 当前类:${javaClass.simpleName}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CustomLog.d(TAG,"onCreate(savedInstanceState: Bundle?) 当前类:${javaClass.simpleName}")
        //不知为何 ，继承基类的activity的onCreate，触发的是这个onCreate(savedInstanceState: Bundle?)
        initData()
    }

    override fun onStart() {
        super.onStart()
        CustomLog.d(TAG,"onStart 当前类:${javaClass.simpleName}")
        nowClassName = javaClass.simpleName
        handler.postDelayed(dialogRunnable, 3000)
        initService()
    }

    override fun onResume() {
        super.onResume()
        CustomLog.d(TAG,"onResume 当前类:${javaClass.simpleName}")
    }

    override fun onRestart() {
        super.onRestart()
        CustomLog.d(TAG,"onRestart 当前类:${javaClass.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        CustomLog.d(TAG,"onPause 当前类:${javaClass.simpleName}")
    }

    override fun onStop() {
        super.onStop()
        CustomLog.d(TAG,"onStop 当前类:${javaClass.simpleName}")
        val customApplication = applicationContext as CustomApplication
        val nowActivityName = customApplication.getNowActivityName()
        val activitySimpleName = nowActivityName?.substringAfterLast(".")
        CustomLog.d(TAG,"activitySimpleName:$activitySimpleName")

        //那么处于后台的时候才发送通知
        val isInBackground = (this@BaseActivity.applicationContext as CustomApplication).getIsInBackground()
        if (isInBackground && activitySimpleName.equals(javaClass.simpleName)){// 处于后台 且 切换至后台app的activity页面名称等于当前基类里面获取activity类名
            handler.postDelayed(notificationRunnable,3000)
            CustomLog.d(TAG,"使用通知 $nowClassName")
        }else{
            CustomLog.d(TAG,"关闭所有定时任务 $nowClassName")
            closeAllTask()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CustomLog.d(TAG,"onDestroy 当前类:${javaClass.simpleName}")
        closeAllTask()
        this.stopService(bgServiceIntent)
    }

    /** 关闭所有定时任务 */
    private fun closeAllTask() {
        handler.removeCallbacks(dialogRunnable)
        handler.removeCallbacks(notificationRunnable)
    }

    /** 初始化数据 - 关于弹框*/
    private fun initData() {
        notificationManager = notificationManager ?: this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmDialogFragment = alarmDialogFragment ?: AlarmDialogFragment()
    }

    /** 使用通知 - 通过pendingIntent实现跳转，缺点是任意界面进入报警详情界面，点击返回键只能返回MainFragment */
    private fun useNotificationPI() {
        var pendingIntent:PendingIntent? = null
        if(javaClass.simpleName == "MainActivity"){//主界面
            val bundle = Bundle()
            bundle.putString("alarmId","1")
            pendingIntent = NavDeepLinkBuilder(this)
                .setGraph(R.navigation.main_navigation)
                .setDestination(R.id.alarmDetailFragment)
                .setArguments(bundle)
                .createPendingIntent()
        }else {//其他界面时候切换后台通知
            val intent = Intent(this@BaseActivity,MainActivity::class.java)
            intent.putExtra("task","toAlarmDetail")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            pendingIntent = TaskStackBuilder.create(this@BaseActivity)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this.applicationContext, "normal")
            .setContentTitle("标题")
            .setContentText("通知次数:${++alarmCount}")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setTimeoutAfter(5000)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager?.notify(notificationId,notification)
    }

    /** 弹框使用 - 因为此处涉及到fragment等生命周期，进入其他activity内时候，在前的activity使用useDialog会因为生命周期问题闪退*/
    private fun useDialog() {
        //弹出多个同种弹框
//        alarmDialogFragment = AlarmDialogFragment()
//        alarmDialogFragment?.show(supportFragmentManager,"testDialog")

        //不弹出多个同种弹框，一次只弹一个，若弹框存在不弹新框
        if (alarmDialogFragment?.isVisible == false){//如果不加这一句，当弹框存在时候在调用alarmDialogFragment.show的时候会报错，因为alarmDialogFragment已经存在
            alarmDialogFragment?.show(supportFragmentManager,"testDialog")
        }else{
            //更新弹框内信息
        }

        val dialog = AlarmDialogFragment()
        dialog.show(supportFragmentManager,"tag")
    }

    /** 关闭报警弹框 */
    private fun closeAlarmDialog() {
        if (alarmDialogFragment?.isVisible == true) {
            alarmDialogFragment?.dismiss()//要关闭的弹框
        }
    }

    //状态栏透明,且组件占据了状态栏
    private fun initWindow() {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    /** 初始化服务 */
    private fun initService() {
        CustomLog.d(TAG,"开启前台服务")
        bgServiceIntent = bgServiceIntent ?: Intent(this, BackgroundService::class.java)
        this.startService(bgServiceIntent)
    }
}