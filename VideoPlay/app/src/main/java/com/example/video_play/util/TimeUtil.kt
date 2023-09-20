package com.example.video_play.util

import java.util.concurrent.TimeUnit
/** 时间工具类 - 需要测试下 */
object TimeUtil {

    /** 毫秒(long)转换为时:分:秒 00:00:00样式*/
    fun formatMillisToHMS(duration: Long = 0L): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /** 毫秒(long)转换为时:分:秒:毫秒 00:00:00:000样式*/
    fun formatMillisToHMSM(duration: Long = 0L): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        val milliseconds = TimeUnit.MILLISECONDS.toMillis(duration) % 1000
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds)
    }
}