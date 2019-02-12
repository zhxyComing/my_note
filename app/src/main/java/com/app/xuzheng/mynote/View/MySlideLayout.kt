package com.app.xuzheng.myslidelayout

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Scroller
import java.util.*

/**
 * Created by xuzheng on 2017/9/13.
 *
 * 侧边栏view 可以添加多个侧边栏，即多个视图，互不影响。点击侧边便签条才能对应拉出。
 *
 * 本App里MySlideView失去了本来的用途
 *
 * 学习适配器模式
 */
const val TIP_WIDTH = 30
const val TIP_HEIGHT = 960
const val TIP_POSITION = 15

class MySlideLayout : FrameLayout {

    private var drawView: MyDrawView? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    private fun init() {
        //这里添加DrawView，确保DrawView显示在上层
        if (drawView == null) {
            drawView = MyDrawView(context)
            addView(drawView)
        }
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        super.addView(child, params)

        if (drawView == null) {
            drawView = MyDrawView(context)
            addView(drawView)
        } else {
            //重新添加，使其置顶层
            removeView(drawView)
            addView(drawView)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)

        if (childCount > 2) { //一个用户自己添加的主界面View，一个组件自身的DrawView
            throw Exception("child view only <= 1")
        }
//        for (i in 0..childCount - 1) {
//            val view = getChildAt(i)
//            if (view is MyDrawView) {
//                view.layout(0, 0, measuredWidth, measuredHeight)
//            }
//        }
//        for (i in 0..childCount - 1) {
//            val view = getChildAt(i)
//            if (view is MyDrawView) {
//                continue
//            }
//            view.layout(0, 0, measuredWidth, measuredHeight)
//        }

        (0..childCount - 1)
                .map { getChildAt(it) }
                .forEach { it.layout(0, 0, measuredWidth, measuredHeight) }

    }

    fun addSlideView(view: View) {
        val tipView = TipView(context)
//        tipView.cardElevation = 4f
//        tipView.radius = 2f
        tipView.setCardBackgroundColor(Color.parseColor("#B0BEC5"))
        tipView.alpha = 0f
        drawView?.addDrawView(tipView, view)
        invalidate()
    }

    fun removeSlideView(view: View) {
        drawView?.removeView(view)
        invalidate()
    }


    private inner class MyDrawView : FrameLayout {

        private var tipPosition: Int? = null
        private var tipHeight: Int? = null
        private var tipWidth: Int? = null

        private val listTip: ArrayList<TipView> = ArrayList()

        var isOpenDraw = false

        var mScroller: Scroller? = null

        private val listDraw: ArrayList<View> = ArrayList()

        private var lastShow = 0

        //因为要控制容器的左右移动，所以左右滑动事件必须抢夺，否则有View覆盖其上时会无法移动

        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
            init()
        }

        private fun init() {
            mScroller = Scroller(context)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            tipHeight = measuredHeight / 10
            tipHeight = TIP_HEIGHT
//            tipPosition = measuredHeight / 20
            tipPosition = TIP_POSITION
//            tipWidth = measuredHeight / 10
            tipWidth = TIP_WIDTH
        }

        //保证了一个侧边View必定对应一个tipView
        fun addDrawView(tipView: TipView, view: View) {

            tipView.setOnTouchListener { view, motionEvent ->
                //tipTouch回调，需要完善，目前只是测试
                onTipTouchListener?.onTipTouch(motionEvent)

                when (motionEvent.action) {

                    MotionEvent.ACTION_DOWN -> {
                        isOpenDraw = true
                        //隐藏掉上一个
                        listDraw[lastShow].visibility = View.GONE
                        //显示这一次的
                        listDraw[listTip.indexOf(tipView)].visibility = View.VISIBLE
                        //记录这一次的
                        lastShow = listTip.indexOf(tipView)
                    }

                    MotionEvent.ACTION_UP -> {
                        isOpenDraw = false
                        listDraw[listTip.indexOf(tipView)].visibility = View.GONE
                    }
                }
                true
            }
            view.visibility = View.GONE

            listTip.add(tipView)
            listDraw.add(view)
            addView(tipView)
            addView(view)
        }

        fun removeDrawView(view: View) {
            listTip.remove(listTip[listDraw.indexOf(view)])
            listDraw.remove(view)
            removeView(listTip[listDraw.indexOf(view)])
            removeView(view)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//            super.onLayout(changed, left, top, right, bottom)
            for (i in 0..childCount - 1) {
                val view = getChildAt(i)
                if (view != null) {
                    if (view is TipView) {
                        view.layout(0 - 10, tipPosition!!, tipWidth!!, tipPosition!! + tipHeight!!)
                        tipPosition = tipPosition!! + 2 * tipHeight!!
                    } else {
                        view.layout(-measuredWidth + 10, 0 + 10, 0 - 10, measuredHeight - 10)
                    }
                }
            }

        }


        //这个move的触发，已经建立在抢夺事件成功，即左右滑动的基础上，所以无需再判断滑动方向
        private var touchLastX: Float = 0f

        override fun onTouchEvent(ev: MotionEvent?): Boolean {
            if (!isOpenDraw) {
                //不点Tip不让移动
                return false
            }
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchLastX = ev?.rawX
                }
                MotionEvent.ACTION_MOVE -> {
                    if (touchLastX == 0f) {
                        touchLastX = ev?.rawX
                        return true
                    }

                    var disX = ev.rawX - touchLastX

                    /**
                     * 手指右滑 disX > 0
                     */
                    //手指右滑，左边超界
                    if (disX > 0 && scrollX - disX < -measuredWidth) {
                        scrollTo(-measuredWidth, 0)
                        //手指左滑 disX < 0
                    } else if (disX < 0 && scrollX - disX > 0) {
                        scrollTo(0, 0)
                    } else {
                        scrollBy(-disX.toInt(), 0)
                    }

                    touchLastX = ev?.rawX
                }

                MotionEvent.ACTION_UP -> {
                    //滑回去
                    if (scrollX >= -measuredWidth / 4 * 3) {
                        mScroller?.startScroll(scrollX, 0, -scrollX, 0)

                        //只有滑回去，才能设置不能滑动状态
                        isOpenDraw = false

                        //隐藏时的回调  200后回调，因为无法准确获取完全隐藏的时机，需要优化，这里只是测试代码
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                (context as Activity).runOnUiThread {
                                    onHideListener?.onHide()
                                }
                            }
                        }, 100)

                    } else { //滑出来
                        mScroller?.startScroll(scrollX, 0, -(measuredWidth + scrollX), 0)
                        onShowListener?.onShow()
                    }
                    invalidate()
                    touchLastX = 0f
                }
            }
            return true
        }

        override fun computeScroll() {
            super.computeScroll()
            if (mScroller!!.computeScrollOffset()) {
                scrollTo(mScroller!!.currX, mScroller!!.currY)
                invalidate()
            }
        }

        private var lastX: Float = 0f
        private var lastY: Float = 0f
        override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = ev?.rawX
                    lastY = ev?.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (lastX == 0f && lastY == 0f) {
                        lastX = ev?.rawX
                        lastY = ev?.rawY
                        return super.onInterceptTouchEvent(ev)
                    }

                    val disX = ev?.rawX.minus(lastX)
                    val disY = ev?.rawY.minus(lastY)

                    //左右移动，抢夺事件
                    if (Math.abs(disX) > Math.abs(disY) && Math.abs(disX) > 10 && isOpenDraw) {
                        return true
                    }
                }
            }
            return super.onInterceptTouchEvent(ev)
        }

        fun hideScroll() {
            mScroller?.startScroll(scrollX, 0, -scrollX, 0)
            invalidate()

            //只有滑回去，才能设置不能滑动状态
            isOpenDraw = false

            //隐藏时的回调  200后回调，因为无法准确获取完全隐藏的时机，需要优化，这里只是测试代码
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    (context as Activity).runOnUiThread {
                        onHideListener?.onHide()
                    }
                }
            }, 100)
        }
    }

    private class TipView : CardView {

        private var mPaint: Paint? = null

        constructor(context: Context) : super(context) {
            init()
        }

        private fun init() {
            //不清楚为什么文字不显示
//            mPaint = Paint()
//            mPaint?.isAntiAlias = true
//            mPaint?.textSize = 10f
//            mPaint?.textAlign = Paint.Align.CENTER
//            mPaint?.color = Color.WHITE
//            mPaint?.style = Paint.Style.STROKE
//            mPaint?.strokeWidth = 1f
        }
    }

    interface OnHideListener {
        fun onHide()
    }

    private var onHideListener: OnHideListener? = null

    fun setOnHideListener(onHideListener: OnHideListener) {
        this.onHideListener = onHideListener
    }

    interface OnShowListener {
        fun onShow()
    }

    private var onShowListener: OnShowListener? = null

    fun setOnShowListener(onShowListener: OnShowListener) {
        this.onShowListener = onShowListener
    }

    interface OnTipTouchListener {
        fun onTipTouch(ev: MotionEvent)
    }

    private var onTipTouchListener: OnTipTouchListener? = null

    fun setOnTipTouchListener(onTipTouchListener: OnTipTouchListener) {
        this.onTipTouchListener = onTipTouchListener
    }

    fun hideSlideView() {
        drawView?.hideScroll()
    }
}