package com.app.xuzheng.passwordview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.app.xuzheng.mynote.R

/**
 * Created by xuzheng on 2017/8/25.
 */
class TimeView : View {

    private val paint: Paint = Paint()

    private var positionX: Float? = null
    private var positionY: Float? = null

    //存储表盘相关数值
    private val num_list_large: HashMap<Int, Int> = HashMap()

    private var isActionUp = false

    private var offsetScale = OFFSET_SCALE_IN
    private var circleScale = CIRCLE_SCALE_IN

    private var followOffset = 0f

    var rect_background = Color.parseColor("#404040")
    var circle_background = Color.parseColor("#333333")
    var num_in_color = Color.WHITE
    var num_out_color = Color.WHITE
    var center_circle_color = Color.WHITE
    var circle_target_color = Color.parseColor("#03A9F4")
    var circle_follow_color = Color.WHITE

    //是否显示跟踪体
    var follow_show = true
    //跟踪体所在的圆的最大值比例 数值越大圆越大
    var follow_length_scale = 3
    //跟踪体出现消失的速度 数值越小速度越快
    var follow_speed_scale = 72

    companion object {
        //这里的scale,都是用width除来获取比例长度的

        //内圈表盘和外圈表盘的大小比例  数值越小圆越小越靠内
        private val OFFSET_SCALE_IN = 12 / 2.3
        private val OFFSET_SCALE_OUT = 12 / 1.4

        //内外圈表盘Target圆的大小比例  数值越大圆越小
        private val CIRCLE_SCALE_IN = 18f
        private val CIRCLE_SCALE_OUT = 36f
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        init()

        val typeArray = context.obtainStyledAttributes(attr, R.styleable.TimeView)
        rect_background = typeArray.getColor(R.styleable.TimeView_rect_background, rect_background)
        circle_background = typeArray.getColor(R.styleable.TimeView_circle_background, circle_background)
        num_in_color = typeArray.getColor(R.styleable.TimeView_num_in_color, num_in_color)
        num_out_color = typeArray.getColor(R.styleable.TimeView_num_out_color, num_out_color)
        center_circle_color = typeArray.getColor(R.styleable.TimeView_center_circle_color, center_circle_color)
        circle_target_color = typeArray.getColor(R.styleable.TimeView_circle_target_color, circle_target_color)
        circle_follow_color = typeArray.getColor(R.styleable.TimeView_circle_follow_color, circle_follow_color)
        follow_length_scale = typeArray.getInt(R.styleable.TimeView_follow_length_scale, follow_length_scale)
        follow_speed_scale = typeArray.getInt(R.styleable.TimeView_follow_speed_scale, follow_speed_scale)
        follow_show = typeArray.getBoolean(R.styleable.TimeView_follow_show, follow_show)
        typeArray?.recycle()
    }

    fun init() {
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
    }

    //取小的，即永远是正方形
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //size永远是真实的尺寸！
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        //这里View尺寸以小的为准
        if (widthSpecSize < heightSpecSize) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        } else {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.alpha = 255
        val width = measuredWidth.toFloat()

        //measureWidth和measureHeight是onMeasure得出的高度，所以等高
        //1.绘制背景矩形
        paint.color = rect_background
        paint.style = Paint.Style.FILL
        canvas?.drawRect(0f, 0f, width, width, paint)

        //2.绘制背景圆盘
        val circle_margin = width / 12
        paint.color = circle_background
        canvas?.drawCircle(width / 2, width / 2, width / 2 - circle_margin, paint)

        //3.绘制两圈时刻
        var ang = 0
        num_list_large.clear()

        //3.1,外圈时刻
        val time_out = (width / 12 * 1.5).toFloat()
        paint.color = num_out_color
        paint.textSize = width / 36

        //60个刻度 但是只有每6度绘制一次
        for (i in 1..60) {
            canvas?.rotate(6f, width / 2, width / 2)
            if (i % 5 == 0) {
                canvas?.drawText(i.toString(), width / 2, time_out, paint)
            }
            //在外圈，取这里的数
            if (!isScaleIn) {
                ang += 6
                if (ang == 360) {
                    ang = 0
                }
                num_list_large.put(ang, i)
            }
        }

        //3.2.绘制内圈时刻
        val time_in = (width / 12 * 2.5).toFloat()
        paint.color = num_in_color
        paint.textSize = width / 24

        //12个刻度
        for (i in 1..12) {
            canvas?.rotate(30f, width / 2, width / 2)
            canvas?.drawText(i.toString(), width / 2, time_in, paint)

            //在内圈，取这里的数
            //往列表里添加该角度对应的数值，以后后续查找；注意360为0
            if (isScaleIn) {
                ang += 30
                if (ang == 360) {
                    ang = 0
                }
                num_list_large.put(ang, i)
            }
        }

        //4.绘制中心点
        paint.color = center_circle_color
        canvas?.drawCircle(width / 2, width / 2, width / 72, paint)


        //一旦触摸，positionXY都不为null，以下方法就会执行
        if (positionX != null && positionY != null) {

            paint.color = circle_follow_color

            val offset = (width / offsetScale).toFloat()

            val posXLength = positionX!! - width / 2
            val posYLength = width / 2 - positionY!!
            val posZLength = Math.hypot(posXLength.toDouble(), posYLength.toDouble())
            val tarZLength = width / 2 - offset - posZLength

            val tarXLength = posXLength / posZLength * tarZLength
            var tarYLength = posYLength / posZLength * tarZLength

            //5.1.绘制跟踪圆
            if (follow_show) {

                if (isOnTouch) { //触摸伸长
                    if (followOffset == 0f) {
                        followOffset = width / 2
                    } else if (followOffset > width / follow_length_scale) {
                        followOffset -= width / follow_speed_scale
                    }
                }


                if (!isOnTouch && followOffset != 0f) { //松手回归
                    val temp = followOffset + width / follow_speed_scale
                    if (temp < width / 2 + width / follow_speed_scale) {
                        followOffset = temp
                        invalidate()
                    }
                }

                //bug：初次进入，当点击事件时，重绘只触发一次，此时isOnTouch=false（UP重置了），followoffset=0
                if (followOffset != 0f) {
                    val _tarZLength = width / 2 - followOffset - posZLength
                    val _tarXLength = posXLength / posZLength * _tarZLength
                    var _tarYLength = posYLength / posZLength * _tarZLength

                    val y = positionY!! - _tarYLength
                    val x = positionX!! + _tarXLength

                    canvas?.drawCircle(x.toFloat(), y.toFloat(), width / 72, paint)
                    canvas?.drawLine(width / 2, width / 2, x.toFloat(), y.toFloat(), paint)
                }
            }
            //5.2.绘制表盘Target圆
            paint.color = circle_target_color
            paint.alpha = 120
            //得到目前角度
            var angle = 180 * Math.atan(tarXLength / tarYLength) / Math.PI
            //得到整点角度;这里的x直接用手指按下的positionX即可
            if (isScaleIn) {
                angle = getIntegralPoint(angle, positionX!!.toDouble(), width / 2)
            } else {
                angle = getIntegralPointDetail(angle.toInt(), positionX!!.toDouble(), positionY!!.toDouble(), width / 2).toDouble()
            }

            //根据整点角度计算目标坐标
            val tarXLength_1 = Math.sin(2 * Math.PI / 360 * angle) * (width / 2 - offset)
            val tarYLength_1 = Math.cos(2 * Math.PI / 360 * angle) * (width / 2 - offset)
            val x_1 = width / 2 + tarXLength_1
            val y_1 = width / 2 - tarYLength_1
            canvas?.drawCircle(x_1.toFloat(), y_1.toFloat(), width / circleScale, paint)

            //5.3.手抬起，响应回调
            if (isActionUp) {
                isActionUp = false
//                followOffset = 0f
                val num = num_list_large[angle.toInt()]
                num?.let {
                    onNumCheckedListener?.onChecked(num)
                }
            }
        }

    }

    private var isOnTouch = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        isTargetCircleHide = false
        isOnTouch = true
        //只是单纯的点击而不滑动，该方法执行频率很低，导致画面看起来卡顿
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                positionX = event.x
                positionY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {

                isActionUp = true
                isOnTouch = false

                positionX = event.x
                positionY = event.y
                invalidate()
            }
        }
        return true
    }

    //12格的
    fun getIntegralPoint(angle: Double, x: Double, center: Float): Double {
        var offset = 0.0
        if (x < center) {
            offset = 180.0
        }
        if (angle >= 0 && angle < 15) {
            return 0.0 + offset
        } else if (angle >= 15 && angle < 45) {
            return 30.0 + offset
        } else if (angle >= 45 && angle < 75) {
            return 60.0 + offset
        } else if (angle >= 75 && angle < 90) {
            return 90.0 + offset
        } else if (angle >= -90 && angle < -75) {
            return 90.0 + offset
        } else if (angle >= -75 && angle < -45) {
            return 120.0 + offset
        } else if (angle >= -45 && angle < -15) {
            return 150.0 + offset
        } else if (angle >= -15 && angle < 0) {
            var res = 180.0 + offset
            if (res == 360.0) {
                res = 0.0
            }
            return res
        }
        return 0.0
    }

    //60格的 实现不需要，这个需要判断第几象限，否则会在边界出问题
    fun getIntegralPointDetail(angle: Int, x: Double, y: Double, center: Float): Int {
        //右侧象限，即第一第二象限
        var num = 1
        if (center in y..x) {
            num = 1
        } else if (x >= center && y > center) {
            num = 2
        } else if (x < center && y > center) {
            num = 3
        } else if (x < center && y <= center) {
            num = 4
        }
        var res = 0
        when (num) {
            1 -> res = angle
            2, 3 -> res = angle + 180
            4 -> res = if (angle + 360 == 360) 0 else angle + 360
        }
        return res / 6 * 6
    }

    interface OnNumCheckedListener {
        fun onChecked(num: Int)
    }

    var onNumCheckedListener: OnNumCheckedListener? = null

    //初始在内圈
    var isScaleIn = true

    fun changeOut() {
        isScaleIn = false
        offsetScale = OFFSET_SCALE_OUT
        circleScale = CIRCLE_SCALE_OUT
    }

    fun changeIn() {
        isScaleIn = true
        offsetScale = OFFSET_SCALE_IN
        circleScale = CIRCLE_SCALE_IN
    }

    private var isTargetCircleHide = false
    fun changeOutAndHideTargetCircle(){
        changeOut()
        isTargetCircleHide = true
        invalidate()
    }

    fun reload() {
        positionX = null
        positionY = null
        invalidate()
    }

    fun refresh() {
        invalidate()
    }
}