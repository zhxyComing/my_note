package com.app.xuzheng.mynote.View

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.app.xuzheng.mynote.R

/**
 * Created by xuzheng on 2017/7/24.
 */
class CopyOrPasteLayout : LinearLayout {


    constructor(context: Context) : super(context) {
//        设置自身Orientation
//        super.setOrientation(LinearLayout.VERTICAL)
        orientation = LinearLayout.VERTICAL

//        设置自身LayoutParams
//        super.setLayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT))
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

//        setBackgroundColor(Color.TRANSPARENT)

        isMotionEventSplittingEnabled = false
        //加载自视图
        val view = LayoutInflater.from(context).inflate(R.layout.copy_or_paste, null)
        addView(view)

    }

}