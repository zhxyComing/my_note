package com.app.xuzheng.mynote.Utils

import android.content.Context

/**
 * Created by xuzheng on 2017/7/21.
 */
fun Int.toPx(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this.toFloat() * scale + 0.5f).toInt()
}

fun Int.toDp(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this.toFloat() / scale + 0.5f).toInt()
}