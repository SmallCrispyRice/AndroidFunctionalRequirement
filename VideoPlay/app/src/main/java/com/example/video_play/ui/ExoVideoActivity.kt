package com.example.video_play.ui

import android.animation.ValueAnimator
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.video_play.R
import com.example.video_play.base.BaseActivity
import com.example.video_play.databinding.ActivityExoVideoBinding
import com.example.video_play.util.*
import com.example.video_play.widget.VerticalProgressLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs


/** Android12版本
 * 在全屏（及横屏后），如果直接退出应用下次进入应用依然为横屏 */
class ExoVideoActivity : BaseActivity() {
    companion object {
        const val TAG = "ExoVideoActivity"
        const val VOLUME = "volume"
        const val BRIGHTNESS = "brightness"
        const val PLAY_SPEED = "play_speed"
        const val VIDEO_POSITION = "video_id_position"
        const val GESTURE_TYPE_VOLUME = "video_volume"//音量修改
        const val GESTURE_TYPE_BRIGHTNESS = "video_brightness"//亮度修改
        const val GESTURE_TYPE_PROGRESS = "video_progress"//视频进度修改
        const val GESTURE_TYPE_NULL = "null"
        const val GESTURE_RESPONSE_VIEW_WIGHT_PERCENT = 0.8f//手水平划动时，控制视频进度变化的虚拟的进度条长度占屏幕宽度的百分比 。虚拟的进度条想象中的，实际不存在
        const val MAX_CHANGE_VIDEO_PROGRESS = 1 * 60 * 60 * 1000
        const val MAX_PLAY_SPEED  = 2f//视频最快倍速 目前2倍速
        const val DEF_TIME = 100L//防止手指快速按下抬起 对 手势判断影响的时间
        const val MIN_MOVE_DISTANCE = 30//判断手指移动方向最小距离
    }

    private lateinit var binding: ActivityExoVideoBinding
    private var exoPlayer: SimpleExoPlayer? = null

    private val url = "https://www.w3school.com.cn/example/html5/mov_bbb.mp4"

    private val sharedPreferencesUtils = SharedPreferencesUtils(this)
    private val retriever: MediaMetadataRetriever = MediaMetadataRetriever()

    private var originalWidth: Int = 0
    private var originalHeight: Int = 0
    private var playerViewWidth: Int = 0//视频控件宽度
    private var playerViewHeight: Int = 0//视频控件高度

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var downX = 0f
    private var downY = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var gestureType = GESTURE_TYPE_NULL//判断手势类型
    private var isVisibleSetView = false//视频设置界面是否显示
    private var isVisibleGestureView = false//手势界面是否显示，亮度 音量 视频进度
    private var valueAnimator: ValueAnimator? = null
    private var volumeChange = 0f//音量变化
    private var volume = 0f//音量
    private var brightnessChange = 0f//亮度变化
    private var brightness = 0f//亮度
    private var fingerDownVideoPosition = 0L //手指按下时视频位置
    private var skipVideoDuration = 0L//跳过的视频时间
    private var seekToPosition = 0L//视频要跳转到的位置

    private lateinit var clGestureType:ConstraintLayout
    private lateinit var clVideoTop:ConstraintLayout
    private lateinit var clVideoBottom:ConstraintLayout
    private lateinit var imgGestureType: ImageView
    private lateinit var tvMsg1: TextView
    private lateinit var tvMsg2: TextView
    private lateinit var controlView: PlayerControlView
    private lateinit var vpLayout:VerticalProgressLayout
    private var fingerDownTime:Long = 0L
    private var fingerMoveTIme:Long = 0L

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            CustomLog.d(TAG, "playbackState:$playbackState")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            CustomLog.d(TAG, "playWhenReady:$playWhenReady")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_exo_video)
        initData()
        initScreenTop()
        initView()
        play()
    }

    override fun onDestroy() {
        verticalScreen()//MuMu模拟器上 若直接横屏退出app，下次进入就是横屏
        super.onDestroy()
        binding.unbind()
        CustomLog.d("exoPlayer?.currentPosition:${exoPlayer?.currentPosition ?: 0}")
        sharedPreferencesUtils.saveLong(VIDEO_POSITION, exoPlayer?.currentPosition ?: 0)
        sharedPreferencesUtils.saveFloat(VOLUME, exoPlayer?.volume ?: 1f)
        sharedPreferencesUtils.saveFloat(BRIGHTNESS, brightness)
        sharedPreferencesUtils.saveFloat(PLAY_SPEED, exoPlayer?.playbackParameters?.speed?:1f)
        exoPlayer?.release()
        exoPlayer?.removeListener(listener)
        retriever.release()//视频截图
    }

    private fun initData() {
        // 设置缓冲区参数
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                10000,
                15000,
                10000,
                10000
            )
            .build()
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
        exoPlayer?.addListener(listener)
        exoPlayer?.volume = SharedPreferencesUtils(this).getFloat(VOLUME, 1f)//设置控件音量
        exoPlayer?.playbackParameters = PlaybackParameters(SharedPreferencesUtils(this).getFloat(PLAY_SPEED,1f))
        volume = SharedPreferencesUtils(this).getFloat(VOLUME, 1f)
        brightness = SharedPreferencesUtils(this).getFloat(BRIGHTNESS, 1f)
    }

    /** 初始化手机状态栏 */
    private fun initScreenTop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //隐藏图标和文字
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    private fun initView() {
        vpLayout = binding.playerView.findViewById(R.id.vp_layout)
        clGestureType = binding.playerView.findViewById(R.id.cl_gesture_type)
        clVideoTop = binding.playerView.findViewById(R.id.cl_video_top)
        clVideoBottom = binding.playerView.findViewById(R.id.cl_video_bottom)
        imgGestureType = binding.playerView.findViewById(R.id.img_gesture_type)
        tvMsg1 = binding.playerView.findViewById(R.id.tv_msg1)
        tvMsg2 = binding.playerView.findViewById(R.id.tv_msg2)
        controlView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_controller)

        binding.playerView.videoSurfaceView?.alpha = brightness//设置视频亮度

        binding.playerView.apply {
            findViewById<TextView>(R.id.tv_video_title)?.text = "视频标题"
            //顶部左上角返回键
            findViewById<ImageView>(R.id.btn_video_return).setOnClickListener {
                val rotation = getScreenRotation()
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                    exitFullscreen()
                } else {
                    finish()
                }
            }
            //全屏按钮
            findViewById<ImageView>(R.id.btn_fullscreen).setOnClickListener {
                val rotation = getScreenRotation()
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
                    exitFullscreen()
                } else {
                    enterFullscreen()
                }
            }

            //还是得拦截，不然点一下抬起就触发显示控制视频的布局
            setOnTouchListener(object : View.OnTouchListener {
                //playerView触摸方法
                override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                    view?.id
                    event?.apply {
                        when (action) {
                            MotionEvent.ACTION_DOWN -> {
                                startX = event.x
                                startY = event.y
                                downX = event.x
                                downY = event.y
                                playerViewWidth = binding.playerView.width
                                playerViewHeight = binding.playerView.height
                                gestureType = GESTURE_TYPE_NULL
                                fingerDownVideoPosition = (player?.currentPosition ?: 0L)
                                skipVideoDuration = 0L
                                fingerDownTime = System.currentTimeMillis()
                            }
                            MotionEvent.ACTION_MOVE -> {
                                endX = event.x
                                endY = event.y
                                moveX = event.x
                                moveY = event.y
                                fingerMoveTIme = System.currentTimeMillis()
                                //防止手指按下抬起过快，对手势行为判断造成误判
                                if (fingerMoveTIme - fingerDownTime > DEF_TIME){
                                    //判断当前是什么事件 进度 音量 亮度
                                    when (gestureType) {
                                        GESTURE_TYPE_VOLUME -> {
                                            volumeChange = (startY - endY) / (playerViewHeight.toFloat() / 2f)//音量变化
                                            volume += volumeChange
                                            if (volume > 1f){
                                                volume = 1f
                                            }else if (volume < 0f){
                                                volume = 0f
                                            }
                                            player?.volume = volume
                                            tvMsg1.text = "${(volume*100).toInt()}%"
                                            imgGestureType.setImageResource(R.mipmap.volume_64_white)
                                            gestureViewSet(gestureType)
                                        }
                                        GESTURE_TYPE_BRIGHTNESS -> {
                                            //修改亮度暂时没能成功  修改透明度的方式不好使
                                            brightnessChange = (startY - endY) / (playerViewHeight.toFloat() / 2f)//音量变化
                                            brightness += brightnessChange
                                            if (brightness > 1f){
                                                brightness = 1f
                                            }else if (brightness < 0f){
                                                brightness = 0f
                                            }
                                            binding.playerView.videoSurfaceView?.alpha = brightness
                                            tvMsg1.text = "${(brightness*100).toInt()}%"
                                            imgGestureType.setImageResource(R.mipmap.light_64_white)
                                            gestureViewSet(gestureType)
                                        }
                                        GESTURE_TYPE_PROGRESS -> {
                                            gestureViewSet(gestureType)
                                            //变化进度View宽度
                                            skipVideoDuration = ((moveX - downX) / (playerViewWidth * GESTURE_RESPONSE_VIEW_WIGHT_PERCENT) * (player?.duration ?: 0L)).toLong()
                                            seekToPosition = fingerDownVideoPosition + skipVideoDuration
                                            if (seekToPosition > (player?.duration ?: 0L)){
                                                seekToPosition = player?.duration ?: 0L
                                                skipVideoDuration = (player?.duration ?: 0L) - fingerDownVideoPosition
                                            }else if (seekToPosition < 0L){
                                                seekToPosition = 0
                                                skipVideoDuration = fingerDownVideoPosition
                                            }
                                            tvMsg1.text = "${TimeUtil.formatMillisToHMS(seekToPosition)}/${TimeUtil.formatMillisToHMS(player?.duration ?: 0L)}"
                                            if (downX - moveX > 0) {
                                                tvMsg2.text = "-${TimeUtil.formatMillisToHMS(abs(skipVideoDuration))}"
                                            } else {
                                                tvMsg2.text = "+${TimeUtil.formatMillisToHMS(abs(skipVideoDuration))}"
                                            }
                                        }
                                        else -> { //手势类型没有识别出来
                                            //通过水平和竖直方向移动距离判断是那种手势方式
                                            if(abs(downX - moveX) > MIN_MOVE_DISTANCE){
                                                gestureType = GESTURE_TYPE_PROGRESS
                                            }else if(abs(downY - moveY) > MIN_MOVE_DISTANCE){//上下划
                                                if (startX < playerViewWidth / 2){
                                                    gestureType = GESTURE_TYPE_BRIGHTNESS
                                                }else{
                                                    gestureType = GESTURE_TYPE_VOLUME
                                                }
                                            }
                                        }
                                    }
                                }

                                //下面将startX重写赋值，可以获取到每次移动时候手指划动变化，
                                //但视频进度的逻辑与下面不一样。因此使用down与move时的xy坐标
                                startX = endX
                                startY = endY
                            }
                            MotionEvent.ACTION_UP -> {
                                when(gestureType){//松手时候改变视频进度，若放在移动中，触发比较频繁。可根据需求修改
                                    GESTURE_TYPE_PROGRESS ->{
                                        exoPlayer?.seekTo(seekToPosition)
                                    }
                                }
                                //手指抬起时候，一些界面消失
                                if (isVisibleSetView) {//设置界面存在，先处理设置界面
                                    videoSetViewSwitch(1000)
                                } else if (vpLayout.visibility == View.VISIBLE){
                                    vpLayout.visibility = View.GONE
                                } else if(isVisibleGestureView){//手势控件
                                    if (clVideoTop.visibility == View.GONE){//若顶部栏原本不可见，后面隐藏视频控制界面
                                        controlView.hide()
                                    }
                                    clVideoTop.visibility = View.VISIBLE //这里将顶部与底部控件可见，为了手势操作完后，点击屏幕显示顶部与底部视频栏
                                    clVideoBottom.visibility = View.VISIBLE
                                    clGestureType.visibility = View.GONE
                                    imgGestureType.visibility = View.GONE
                                    tvMsg1.visibility = View.GONE
                                    tvMsg2.visibility = View.GONE
                                    isVisibleGestureView = false
                                } else {
                                    if (controlView.isVisible) {
                                        controlView.hide()
                                    } else {
                                        controlView.show()
                                    }
                                }
                            }
                        }
                    }
                    return true
                }
            })

            //视频倍速
            findViewById<ImageView>(R.id.btn_video_speed).apply {
                setOnClickListener {
                    if (vpLayout.visibility == View.GONE){
                        vpLayout.visibility = View.VISIBLE
                    }
                }
            }

            val playSpeed = (exoPlayer?.playbackParameters?.speed ?: 1f)
            vpLayout.setProgress(playSpeed / MAX_PLAY_SPEED)
            vpLayout.tvProgress.text = "%.1f".format(playSpeed)+"倍速"//初始化
            //视频倍速不能0，否则闪退
            vpLayout.setOnListener(object :VerticalProgressLayout.OnListener{
                override fun getProgress(progress: Float) {
                    var speed = "%.1f".format(MAX_PLAY_SPEED * progress).toFloat()
                    if (speed == 0f){
                        speed = 0.1f
                    }
                    vpLayout.tvProgress.text = "${speed}倍速"
                }

                override fun onFingerUp(progress: Float) {
                    var speed = "%.1f".format(MAX_PLAY_SPEED * progress).toFloat()
                    if (speed == 0f){
                        speed = 0.1f
                    }
                    exoPlayer?.playbackParameters = PlaybackParameters(speed)
                }
            })

            //Media截图 高版本Android截图准确，低版本不准确
            findViewById<TextView>(R.id.btn_screenshot_by_media).setOnClickListener {
                val fileName = "media_${System.currentTimeMillis()}.jpg"
                val filePath = File(filesDir,fileName).path
                screenshotByMedia(url,exoPlayer?.currentPosition?:0L,filePath)
            }

            //FFmpeg截图 目前来看FFmpeg截取的最精确，且截图后图片比较小
            findViewById<TextView>(R.id.btn_screenshot_by_ffmpeg).setOnClickListener {
                val fileName = "ffmpeg_${System.currentTimeMillis()}.jpg"
                val filePath = File(filesDir,fileName).path
                screenshotByFFmpeg(url,exoPlayer?.currentPosition?:0L,filePath)
            }

            //Glide截图
            findViewById<TextView>(R.id.btn_screenshot_by_glide).setOnClickListener {
                val fileName = "glide_${System.currentTimeMillis()}.jpg"
                val filePath = File(filesDir,fileName).path
                screenshotByGlide(url,exoPlayer?.currentPosition?:0L,filePath)
            }

            //视频设置界面
            findViewById<ImageView>(R.id.btn_set).setOnClickListener {
                videoSetViewSwitch(1000)
            }
        }
    }


    /** 获取屏幕方向 */
    private fun getScreenRotation(): Int {
        var rotaion = Surface.ROTATION_0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (display!=null){
                rotaion = display!!.rotation
            }else{
                Surface.ROTATION_0
            }
        } else {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            rotaion = windowManager.defaultDisplay.rotation
        }
        return rotaion
    }

    private fun screenshotByGlide(url: String, timeInMillis: Long, outputPath: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .frame(timeInMillis * 1000)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveBitmapToFile(resource,outputPath)
                }
            })
    }

    private fun saveBitmapToFile(bitmap: Bitmap, outputPath: String) {
        val file = File(outputPath)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            if (file.exists()){
                ToastUtil.show("截图成功")
            }else{
                ToastUtil.show("截图失败")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            ToastUtil.show("截图失败")
        }
    }

    /** MediaMetadataRetriever方式截图 */
    private fun screenshotByMedia(url: String, timeInMillis: Long, outputPath: String){
        retriever.setDataSource(url, HashMap())
//            retriever.setDataSource(url)//不添加HashMap，会报错
        val time = timeInMillis * 1000 //单位微秒。毫秒乘1000
        val videoWidth = exoPlayer?.videoFormat?.width ?: 0
        val videoHeight = exoPlayer?.videoFormat?.height ?: 0
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            bitmap = retriever.getScaledFrameAtTime(//高版本没有问题
                time,
                MediaMetadataRetriever.OPTION_CLOSEST,
                videoWidth,
                videoHeight
            )
        }else{
            //低版本截图对应进度有问题 获取帧不准确
            bitmap = retriever.getFrameAtTime(time,MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        }
        saveBitmapToFile(bitmap!!,outputPath)
    }

    /** 截图 - 通过FFmpeg 为了适配低版本Android */
    private fun screenshotByFFmpeg(url: String, timeInMillis: Long, outputPath: String) {
        val time = TimeUtil.formatMillisToHMSM(timeInMillis)
        //关于时间不能直接是毫秒，得转换成00:00:05这种形式  也可以是整数的s，带小数秒
        //由于时整形的秒，导致截图与实际播放不一样，因为实际播放的画面不一定时整秒
        //如果对应图片文件已存在会报错
        val command = "-ss $time -i $url -vframes 1 -q:v 2 $outputPath"
        val session = FFmpegKit.execute(command)
        if (ReturnCode.isSuccess(session.returnCode)) {
            //success
            ToastUtil.show("截图成功")
            CustomLog.d(TAG, "成功")
        } else if (ReturnCode.isCancel(session.returnCode)) {
            //cancel
            ToastUtil.show("截图失败")
            CustomLog.d(TAG, "取消")
        } else {
            //fail
            CustomLog.d(TAG, "失败")
        }
    }

    /**
     * 离开全屏
     */
    private fun exitFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.apply {
            //退出全屏时候，将宽高重新赋值回来
            val layoutParams = playerView.layoutParams
            layoutParams.width = originalWidth
            layoutParams.height = originalHeight
            playerView.layoutParams = layoutParams
//            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT//保证视频的宽高比前提下，尽可能充满PlayerView
//            llVideoGroup.removeView(playerView)
//            llVideoGroup.addView(playerView)
//            playerView.player = exoPlayer
        }
    }

    /**
     * 进入全屏
     */
    private fun enterFullscreen() {
        if(originalWidth == 0){//最开始放在initData中，由于没有加载视频，View宽高为0
            originalWidth = binding.playerView.width
            originalHeight = binding.playerView.height
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // 设置 ExoPlayerView 的全屏下相关设置
        binding.apply {
//            llVideoGroup.removeView(playerView)
//            llVideoGroup.addView(playerView)
//            playerView.player = exoPlayer

            //横屏时，将宽度赋值为MATCH_PARENT，不然竖屏指定了宽度为固定值切换为横屏，宽度不会改变
            val layoutParams = playerView.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            playerView.layoutParams = layoutParams
//            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL//拉伸或压缩视频填充PlayerView，不考虑视频宽高比，会导致视频变形
//            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    /** 视频播放 */
    private fun play() {
        val position = sharedPreferencesUtils.getLong(VIDEO_POSITION)
        CustomLog.d(TAG, "position:${position}")
        val mediaItem = MediaItem.Builder().setUri(url).build()
        val mediaSource =
            ProgressiveMediaSource.Factory(DefaultDataSourceFactory(this, "ExoPlayer"))
                .createMediaSource(mediaItem)
        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.prepare()
        CustomLog.d("(exoPlayer?.duration:${exoPlayer?.duration}")
//        if ((exoPlayer?.duration?:0) >= position){//这里获取为负数，可能是媒体源没有加载完，有说通过监听器监听的，这里偷懒就不使用了
//            exoPlayer?.seekTo(position)
//        }
        exoPlayer?.seekTo(position)//为了成功指定播放进度，需要在调用prepare()方法之后进行
        exoPlayer?.playWhenReady = true
        binding.playerView.player = exoPlayer
    }

    /** 屏幕竖屏 */
    private fun verticalScreen() {
        CustomLog.d("verticalScreen 屏幕竖屏")
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * 视频设置界面切换
     * duration 动画持续事件
     */
    private fun videoSetViewSwitch(duration: Long) {
        findViewById<LinearLayout>(R.id.cl_video_set).apply {
            playerViewWidth = binding.playerView.width
            if (valueAnimator == null || valueAnimator?.isRunning == false) {//动画存在或动画未运行
                valueAnimator = ValueAnimator.ofInt(
                    if (isVisibleSetView) this.width else 0,
                    if (isVisibleSetView) 0 else this.width
                )
                valueAnimator?.addUpdateListener {
                    x = (playerViewWidth - it.animatedValue as Int).toFloat()
                }
                valueAnimator?.duration = duration
                valueAnimator?.start()
                isVisibleSetView = !isVisibleSetView
            }

            //此处设置点击方法，防止覆盖的设置按钮点击事件影响。点击拦截
            setOnClickListener {

            }
        }
    }

    /** 手势View该显示内容设置 */
    private fun gestureViewSet(type:String) {
        if (!isVisibleGestureView) {
            clGestureType.visibility = View.VISIBLE
            when(type){
                GESTURE_TYPE_BRIGHTNESS, GESTURE_TYPE_VOLUME->{
                    imgGestureType.visibility = View.VISIBLE
                    tvMsg1.visibility = View.VISIBLE
                }
                GESTURE_TYPE_PROGRESS ->{
                    imgGestureType.visibility = View.GONE
                    tvMsg1.visibility = View.VISIBLE
                    tvMsg2.visibility = View.VISIBLE
                }
            }
            if (!controlView.isVisible) {
                controlView.show()
                clVideoTop.visibility = View.GONE
                clVideoBottom.visibility = View.GONE
            }
            isVisibleGestureView = true
        }
    }
}