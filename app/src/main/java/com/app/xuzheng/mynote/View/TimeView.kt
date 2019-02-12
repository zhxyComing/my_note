package com.app.xuzheng.mynote.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xuzheng on 2017/8/17.
 *
 * 本View需要宽高一致，因为代码统一采用宽为圆盘标准
 *
 * 后期会增加多种样式和省电模式(一分钟一刷新)
 *
 * 经测试，熄屏后就不刷新了，不存在熄屏耗电问题
 */
class TimeView : View {
    var mPaint: Paint? = null
    var isStopTime = true


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mPaint = Paint()
        mPaint?.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val radius = measuredWidth / 2f

        //表盘 背景
        mPaint?.style = Paint.Style.FILL

        mPaint?.color = Color.WHITE
        canvas?.drawCircle(radius, radius, radius, mPaint)
        mPaint?.color = Color.BLACK
        canvas?.drawCircle(radius, radius, radius - 2.5f, mPaint)

        //表盘 时间线
        for (i in 0..11) {
            mPaint?.strokeWidth = 1f
            mPaint?.color = Color.parseColor("#d50000")
            //时间 一刻
            if (i % 3 == 0) {
                //取消掉第6个指针，用来写字
                if (i == 6) {
                    mPaint?.color = Color.TRANSPARENT
                } else {
                    mPaint?.color = Color.WHITE
                }
            }
            canvas?.drawLine(radius, 5f, radius, measuredWidth / 6f, mPaint)
            canvas?.rotate(30f, radius, radius)
        }

        val date = Date()
        val time = SimpleDateFormat("HH-mm-ss").format(date)
        val strings = time.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        mPaint?.strokeWidth = 1f
        mPaint?.color = Color.parseColor("#d50000")
        val second = getSecond(strings)
        canvas?.rotate(360f / 60 * second, radius, radius)
        canvas?.drawLine(radius, radius + 20f, radius, radius - radius * (3f / 4), mPaint)
        canvas?.rotate(360f / 60 * (60 - second), radius, radius)

        mPaint?.strokeWidth = 3f
        mPaint?.color = Color.WHITE
        val minute = getMinute(strings)
        canvas?.rotate(360f / 60 * minute, radius, radius)
        canvas?.drawLine(radius, radius, radius, radius - radius * (3f / 4), mPaint)
        canvas?.rotate(360f / 60 * (60 - minute), radius, radius)

        mPaint?.strokeWidth = 5f
        val hour = getHour(strings)
        canvas?.rotate(360f / 12 * hour, radius, radius)
        canvas?.drawLine(radius, radius, radius, radius - measuredWidth / 4, mPaint)
        canvas?.rotate(360f / 12 * (12 - hour), radius, radius)

        mPaint?.color = Color.parseColor("#d50000")
        mPaint?.style = Paint.Style.FILL
        canvas?.drawCircle(radius, radius, 7f, mPaint)

        mPaint?.textAlign = Paint.Align.CENTER
        mPaint?.color = Color.WHITE
        canvas?.drawText("NOTE", radius, measuredWidth / 6f * 5.2f, mPaint)

        if (!isStopTime) {
            postInvalidateDelayed(1000)
        }
    }

    private fun getHour(strings: Array<String>): Int {
        var hour = Integer.valueOf(strings[0])
        if (hour >= 12) {
            hour -= 12
        }
        return hour
    }

    private fun getMinute(strings: Array<String>): Int {
        return Integer.valueOf(strings[1])
    }

    private fun getSecond(strings: Array<String>): Int {
        return Integer.valueOf(strings[2])
    }

    fun stopTime() {
        isStopTime = true
    }

    fun startTime() {
        //为什么要延迟500毫秒
        //为了屏蔽掉visible重绘
        //比如visible之后会进行一次重绘，如果此时我把isStopTime置为false，再加之我调用了invalidate,就会导致两次重绘
        //这样做能屏蔽掉诸如 visible，layout.x导致的多次重绘
        Timer().schedule(object : TimerTask() {
            override fun run() {
                //重启绘制
                isStopTime = false
                postInvalidate()
            }
        }, 500)
    }
}