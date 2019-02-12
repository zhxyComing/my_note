package com.app.xuzheng.passwordview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.app.xuzheng.mynote.R
import com.app.xuzheng.passwordview.TimeView.OnNumCheckedListener
import kotlinx.android.synthetic.main.time_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by xuzheng on 2017/8/29.
 */
class TimeLayout : FrameLayout {
    private var time: String = "00:00"
    var timeView: TimeView? = null
    var hour: TextView? = null
    var minute: TextView? = null
    var cancel: TextView? = null
    var sure: TextView? = null
    var topBack: LinearLayout? = null
    var bottomBack: LinearLayout? = null
    var timeBack: FrameLayout? = null
    var colon: TextView? = null
    var dayOrNight: TextView? = null

    var rect_background = Color.parseColor("#555555")
    var circle_background = Color.parseColor("#444444")
    var num_in_color = Color.WHITE
    var num_out_color = Color.WHITE
    var center_circle_color = Color.WHITE
    var circle_target_color = Color.parseColor("#03A9F4")
    var circle_follow_color = Color.WHITE
    var text_color = Color.WHITE

    //是否显示跟踪体
    var follow_show = true
    //跟踪体所在的圆的最大值比例 数值越大圆越大
    var follow_length_scale = 3
    //跟踪体出现消失的速度 数值越小速度越快
    var follow_speed_scale = 72

    //默认进来先设置小时
    var isHour = true

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
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
        text_color = typeArray.getColor(R.styleable.TimeView_text_color, text_color)
        typeArray?.recycle()

        init()
    }

    fun init() {
        //为了只让ViewGrop的点击事件只被目标子视图获取，则设置为false
        isMotionEventSplittingEnabled = false

        val view = LayoutInflater.from(context).inflate(R.layout.time_layout, null)

        timeView = view.findViewById(R.id.timeView)
        timeBack = view.findViewById(R.id.timeBack)
        topBack = view.findViewById(R.id.topBack)
        hour = view.findViewById(R.id.hour)
        minute = view.findViewById(R.id.minute)
        colon = view.findViewById(R.id.colon)
        cancel = view.findViewById(R.id.cancel)
        sure = view.findViewById(R.id.sure)
        bottomBack = view.findViewById(R.id.bottomBack)
        dayOrNight = view.findViewById(R.id.dayOrNight)

        dayOrNight?.setOnClickListener {
            if ("上午" == dayOrNight?.text?.toString()) {
                dayOrNight?.text = "下午"

                val format = SimpleDateFormat("HH")
                val date = format.parse(hour?.text?.toString())
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.HOUR, 12)

                val resDate = calendar.time
                val res = format.format(resDate)

                hour?.text = res
            } else {
                dayOrNight?.text = "上午"

                val format = SimpleDateFormat("HH")
                val date = format.parse(hour?.text?.toString())
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.HOUR, -12)

                val resDate = calendar.time
                val res = format.format(resDate)

                hour?.text = res
            }
            setTime()
            onSureClickListener?.onSureClick(time)
        }

        timeView?.onNumCheckedListener = object : OnNumCheckedListener {
            override fun onChecked(num: Int) {
                if (isHour) {
                    if (dayOrNight?.text?.toString() == "上午") {
                        if (num == 12) {
                            hour?.text = "00"
                        } else {
                            if (num in 1..9) {
                                hour?.text = "0" + num
                            } else {
                                hour?.text = num.toString()
                            }
                        }
                    } else {
                        var time = num + 12
                        if (time == 24) {
                            time = 12
                        }
                        hour?.text = time.toString()
                    }
                } else {
                    if (num < 10) {
                        minute?.text = "0" + num
                    } else if (num == 60) {
                        minute?.text = "00"
                    } else {
                        minute?.text = num.toString()
                    }
                }
                setTime()
                onSureClickListener?.onSureClick(time)
            }
        }

        hour?.setOnClickListener {
            changeToHour()
        }

        minute?.setOnClickListener {
            changeToMinute()
        }

        sure?.setOnClickListener {
            onSureClickListener?.onSureClick(time)
        }

        cancel?.setOnClickListener {
            onCancelClickListener?.onCancelClick(time)
        }

        addView(view)
    }

    fun changeToMinute() {
        setTime()
        isHour = false
        timeView?.changeOut()
        hour?.setTextColor(text_color)
        minute?.setTextColor(circle_target_color)
    }

    fun changeToHour() {
        setTime()
        isHour = true
        timeView?.changeIn()
        hour?.setTextColor(circle_target_color)
        minute?.setTextColor(text_color)
    }

    private fun setTime() {
        time = hour?.text.toString() + ":" + minute?.text.toString()
        Log.e("Time", time)
    }

    fun getTime(): String {
        return time
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        loadAttribute()
    }

    private fun loadAttribute() {

        timeView?.rect_background = rect_background
        timeView?.circle_background = circle_background
        timeView?.num_in_color = num_in_color
        timeView?.num_out_color = num_out_color
        timeView?.center_circle_color = center_circle_color
        timeView?.circle_target_color = circle_target_color
        timeView?.circle_follow_color = circle_follow_color
        timeView?.follow_length_scale = follow_length_scale
        timeView?.follow_speed_scale = follow_speed_scale
        timeView?.follow_show = follow_show

        timeBack?.setBackgroundColor(rect_background)

        topBack?.setBackgroundColor(circle_background)

        //以高度为比例
        hour?.setTextColor(circle_target_color)
        hour?.textSize = (measuredHeight / 30).toFloat()

        minute?.setTextColor(text_color)
        minute?.textSize = (measuredHeight / 30).toFloat()

        colon?.setTextColor(text_color)
        colon?.textSize = (measuredHeight / 30).toFloat()

//        sure?.setTextColor(circle_target_color)
        sure?.textSize = (measuredHeight / 80).toFloat()

        cancel?.setTextColor(circle_target_color)
        cancel?.textSize = (measuredHeight / 80).toFloat()

        dayOrNight?.setTextColor(num_in_color)
        dayOrNight?.textSize = (measuredHeight / 75).toFloat()

        bottomBack?.setBackgroundColor(circle_background)
    }

    fun refresh() {
        loadAttribute()
        timeView?.refresh()
    }

    interface OnSureClickListener {
        fun onSureClick(time: String)
    }

    interface OnCancelClickListener {
        fun onCancelClick(time: String)
    }

    private var onSureClickListener: OnSureClickListener? = null
    private var onCancelClickListener: OnCancelClickListener? = null

    fun setOnSureClickListener(onSureClickListener: OnSureClickListener) {
        this.onSureClickListener = onSureClickListener
    }

    fun setOnCancelListener(onCancelClickListener: OnCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener
    }
}