package com.app.xuzheng.mynote.Manager

import android.content.Context

/**
 * Created by xuzheng on 2017/8/1.
 */
object SharedpreferenceManager {

    val IS_SHOW_LIMIT_TIP = "isShowLimitTip"
    val NOW_THEME = "nowTheme"
    val NOW_NOTE = "nowNote"
    val PASS_WORD = "password"
    val ACCOUNT = "account"
    val IS_HIDE_DESKTOP = "isHideDesktop"
    val CLOCK_REQUEST_CODE = "requestCode"
    val IS_OPEN_VIBRATE = "isOpenVibrate"
    val IS_SHOW_SLIDE_VIEW = "isShowSlideView"

    private val FILE_NAME = "my_note_sf"

    //存储和读写和读写string 数据
    fun getString(context: Context, key: String): String {
        val sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sf.getString(key, "")
    }

    //存储和读写和读写string 数据
    fun getString(context: Context, key: String, defValue: String): String {
        val sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sf.getString(key, defValue)
    }

    fun setString(context: Context, key: String, values: String) {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(key, values)
        editor.commit()
    }


    fun getInt(context: Context, key: String): Int {
        val sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sf.getInt(key, 0)
    }

    fun setInt(context: Context, key: String, values: Int) {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(key, values)
        editor.commit()
    }

    fun getBoolean(context: Context, key: String): Boolean {
        val sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sf.getBoolean(key, false)
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        val sf = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sf.getBoolean(key, defaultValue)
    }

    fun setBoolean(context: Context, key: String, values: Boolean) {
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean(key, values)
        editor.commit()
    }
}