package com.app.xuzheng.mydrawview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Scroller

/**
 * Created by xuzheng on 2017/8/30.
 *
 */
open class DrawerLayout : FrameLayout {

    private var mScroller: Scroller? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        init()
    }

    fun init() {
        setWillNotDraw(false)

        mScroller = Scroller(context)
    }

    companion object {
        val MOVE_MODE = true
        val SWITCH_MODE = false
    }

    /**
     * l,r,t,b 是该组件在父组件中的位置坐标 和子组件没有必然联系！
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)

        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            if (child != null && child.visibility != View.GONE) {
                child.layout(measuredWidth * i, 0, measuredWidth + measuredWidth * i, measuredHeight)
            }
        }

    }

    var lastX = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (isOnMoveTouch) {
            /**
             * 供外部使用的滑动组件的方法，当然也可以实现其它功能
             * 一旦处于滑动模式，只响应onMoveTouch方法
             * 否则只响应onTouchEvent原始方法
             * 两者是二选一
             * 而不是像setOnTouchListener返回false，onTouchEvent返回true，两者是并存关系，onTouchListener中响应了事件，onTouchEvent能接着响应
             */
            onMoveTouchEventListener?.onMoveTouch(event)
            return true
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
            }
            MotionEvent.ACTION_MOVE -> {
                if (lastX == 0f) {
                    lastX = event.rawX
                    return true
                }
                var disX = event.rawX - lastX
                /**
                 * 手指向左滑动  scrollX负数  因为画布整体左移动了
                 *
                 * disX < 0 手指向左滑动
                 *
                 * disX > 0 手指向右滑动
                 *
                 * 要求：预估算 ：左滑不能> realWidth * (count-1) ，右滑不能 < 0
                 */
                //手指向左滑动 并且 下一步越界
                if (disX < 0 && scrollX - disX > measuredWidth * (childCount - 1)) {
                    scrollTo(measuredWidth * (childCount - 1), 0)
                } else if (disX > 0 && scrollX - disX < 0) {
                    scrollTo(0, 0)
                } else {
                    scrollBy(-disX.toInt(), 0)
                }

                lastX = event.rawX
            }
            MotionEvent.ACTION_UP -> {
                val num = scrollX / measuredWidth
                val dis = scrollX % measuredWidth
                //左视图多 左视图滑出
                if (dis <= measuredWidth / 2) {
                    onSwitchCheckedListener?.onSwitchChecked(lastPosition, num)
                    lastPosition = num
                    mScroller?.startScroll(scrollX, 0, -dis, 0)
                } else { //右视图多 右视图滑出
                    onSwitchCheckedListener?.onSwitchChecked(lastPosition, num + 1)
                    lastPosition = num + 1
                    mScroller?.startScroll(scrollX, 0, measuredWidth - dis, 0)
                }
                invalidate()
                lastX = 0f
            }
        }
        return true
    }

    fun setOnMoveTouchEvent(onMoveTouchEventListener: OnMoveTouchEventListener) {
        this.onMoveTouchEventListener = onMoveTouchEventListener
    }

    interface OnMoveTouchEventListener {
        fun onMoveTouch(ev: MotionEvent?)
    }

    private var onMoveTouchEventListener: OnMoveTouchEventListener? = null

    var isOnMoveTouch = SWITCH_MODE

    interface OnSwitchCheckedListener {
        fun onSwitchChecked(lastPosition: Int, nowPosition: Int)
    }

    private var onSwitchCheckedListener: OnSwitchCheckedListener? = null
    private var lastPosition = 0

    fun getNowPosition(): Int {
        return lastPosition
    }

    fun setOnSwitchCheckedListener(onSwitchCheckedListener: OnSwitchCheckedListener) {
        this.onSwitchCheckedListener = onSwitchCheckedListener
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller!!.computeScrollOffset()) {
            scrollTo(mScroller!!.currX, mScroller!!.currY)
            invalidate()
        }
    }

    var startX: Float = 0f
    var startY: Float = 0f

    /**
     * 这里限定了20，恰好限制了在慢速滑动下，事件不会抢夺
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.rawX
                startY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val disX = ev.rawX - startX
                val disY = ev.rawY - startY
                //移动模式，只要move就抢夺子View事件
                if (Math.abs(disX) > Math.abs(disY) && Math.abs(disX) > 10) {//左右滑
                    return true
                } else if (Math.abs(disX) < Math.abs(disY) && Math.abs(disY) > 20 && isOnMoveTouch) {
                    return true
                }

                startX = ev.rawX
                startY = ev.rawY
            }
            MotionEvent.ACTION_UP -> {

            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun switchToPosition(position: Int) {
        onSwitchCheckedListener?.onSwitchChecked(lastPosition, position)
        lastPosition = position
        mScroller?.startScroll(scrollX, 0, position * measuredWidth, 0)
        invalidate()
    }
}