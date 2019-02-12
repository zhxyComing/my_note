package com.app.xuzheng.mynote.Activity

import android.app.Application
import android.content.Context
import android.widget.EditText

/**
 * Created by xuzheng on 2017/7/17.
 */
class MyNoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        context = this
    }

    companion object {
        //Application
        var context: Context? = null

        //用于判断HideActivity是否打开；
        // 具体用途是为了判断本次点击全局便签是为了启动键盘还是同步光标
        var isHideActivityShow = false
            set(value) {
                field = value
                etNote?.isCursorVisible = value  //控制光标的显示，只有在HideView显示时光标才能显示
                etSearch?.isCursorVisible = value
            }

        //全局便签 输入 DesktopLayout中的EditText
        var etNote: EditText? = null

        //全局搜索et
        var etSearch : EditText? = null

        var isCopyOrPasteLayoutShow = false

        //便签是否处于隐藏小化状态
        var isDeaktopSmall = false

        var isSlideViewShow = false
    }

}