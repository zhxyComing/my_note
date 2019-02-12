package com.app.xuzheng.mynote.View

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

/**
 * Created by xuzheng on 2017/9/7.
 */
class MyScrollView : ScrollView {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        init()
    }

    fun init(){

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}