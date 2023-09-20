package com.example.video_play.util

import android.content.Context
import android.content.SharedPreferences

/** 通过SharedPreferences存储少量本地数据工具类
 * 需要读写权限 */
class SharedPreferencesUtils(private val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("projectName", Context.MODE_PRIVATE)
    }

    fun saveString(key: String, value: String){
        sharedPreferences.edit().putString(key,value).apply()
    }

    /** 获取String 默认值空字符串 */
    fun getString(key: String, defaultLong: String = ""): String? {
        return sharedPreferences.getString(key, defaultLong)
    }

    fun saveInt(key: String, value: Int){
        sharedPreferences.edit().putInt(key,value).apply()
    }

    /** 获取Int 默认值0 */
    fun getInt(key: String, defaultLong: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultLong)
    }

    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    /** 获取Long 默认值0 */
    fun getLong(key: String, defaultLong: Long = 0): Long {
        return sharedPreferences.getLong(key, defaultLong)
    }

    fun saveFloat(key: String, value: Float){
        sharedPreferences.edit().putFloat(key,value).apply()
    }

    /** 获取Float 默认值0f */
    fun getFloat(key: String, defaultLong: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultLong)
    }

    fun saveBoolean(key: String, value: Boolean){
        sharedPreferences.edit().putBoolean(key,value).apply()
    }

    /** 获取Boolean 默认值false */
    fun getBoolean(key: String, defaultLong: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultLong)
    }

    private val editor: SharedPreferences.Editor by lazy {
        sharedPreferences.edit()
    }
}