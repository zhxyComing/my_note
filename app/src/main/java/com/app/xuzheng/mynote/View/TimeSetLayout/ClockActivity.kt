package com.app.xuzheng.mynote.View.TimeSetLayout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.activity_clock.*
import android.widget.RadioButton
import android.widget.Toast
import com.app.xuzheng.mynote.Broadcast.ClockBroadcastReceiver
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import com.app.xuzheng.mynote.Utils.TimeUtils
import com.app.xuzheng.passwordview.TimeLayout
import java.text.SimpleDateFormat
import java.util.*


class ClockActivity : Activity() {

    //包含了年月日时分秒
    private var clockTime: String? = null
    private var hourMinuteSecnod: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        //表盘点击确定 可以设定时间为今天或明天（肯定不能是以前，所以是明天）
        timeLayout.setOnSureClickListener(object : TimeLayout.OnSureClickListener {
            override fun onSureClick(time: String) {

                //设定闹铃时间
                //注意：这里的time只有时分
                clockTime = TimeUtils.getClockTime(time + ":00")
                hourMinuteSecnod = time + ":00"

                //设定闹铃时间提示
                timeTip.text = clockTime + "  距今" + TimeUtils.getTimeDifference(clockTime!!)

                //根据该时间设定时间表盘
                var dayOfWeek = TimeUtils.getTodayOfWeek()
                if (isClockTimeIsToday()) {
                    (rgWeek.getChildAt(dayOfWeek - 1) as RadioButton).isChecked = true
                } else {
                    dayOfWeek += 1
                    if (dayOfWeek == 8) {
                        dayOfWeek = 1
                    }
                    (rgWeek.getChildAt(dayOfWeek - 1) as RadioButton).isChecked = true
                }
            }
        })

        //星期栏 选中任意星期几
        rgWeek.setOnCheckedChangeListener { radioGroup, id ->
            val view = radioGroup?.findViewById<RadioButton>(id)
            if (clockTime != null && hourMinuteSecnod != null) {

                val dayOfWeek = view?.text.toString()

                clockTime = getClockTimeByDayOfWeek(dayOfWeek, hourMinuteSecnod!!)
                timeTip.text = clockTime + "  距今" + TimeUtils.getTimeDifference(clockTime!!)
            } else {
                Toast.makeText(this@ClockActivity, "请先选择时间", Toast.LENGTH_SHORT).show()
                view?.isChecked = false
            }
        }

        //设置闹铃
        setClock.setOnClickListener {
            if (clockTime == null) {
                Toast.makeText(this@ClockActivity, "你还没有选择时间", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(etClockNote.text.toString())) {
                Toast.makeText(this@ClockActivity, "你还没有填写提醒事项", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            TimeUtils.setClock(this@ClockActivity, Intent(this@ClockActivity, ClockBroadcastReceiver::class.java), clockTime!!, "yyyy-MM-dd HH:mm:ss", etClockNote.text.toString())
            Toast.makeText(this@ClockActivity, "闹铃设置成功，提醒时间:" + clockTime, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    //根据选中的是星期几，设定闹铃时间
    private fun getClockTimeByDayOfWeek(dayInWeek: String, hourMinuteSecond: String): String {

        //获取当前星期几
        var dayOfWeek = TimeUtils.getTodayOfWeek()

        //获取选取的是星期几
        var selectDayOfWeek = dayOfWeekToNum(dayInWeek)

        //如果 选取>今天
        if (selectDayOfWeek > dayOfWeek) {
            return TimeUtils.getAppointDayFormTodayByHour(selectDayOfWeek - dayOfWeek, hourMinuteSecond)
        } else if (selectDayOfWeek < dayOfWeek) {//选取<今天
            return TimeUtils.getAppointDayFormTodayByHour(7 - (dayOfWeek - selectDayOfWeek), hourMinuteSecond)
        } else {//选取=今天 存在当前时间已过和未过两种情况，已过说明时间设定在下星期
            val date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd")
            val yearMonthDay = format.format(date)
            //今天的钟表时间
            val clockInNowDay = yearMonthDay + " " + hourMinuteSecond
            //如果今天的钟表时间>当前时间，那么时间的设定是正确的
            //否则+7天
            if (!TimeUtils.isBigger(clockInNowDay)) {
                return TimeUtils.getAppointDayFormTodayByHour(7, hourMinuteSecond)
            } else {
                return TimeUtils.getClockTime(hourMinuteSecond)
            }
        }
        return ""
    }

    //根据汉字返回星期数字1..7
    private fun dayOfWeekToNum(week: String): Int {
        return when (week) {
            "一" -> 1
            "二" -> 2
            "三" -> 3
            "四" -> 4
            "五" -> 5
            "六" -> 6
            "七" -> 7
            else -> {
                0
            }
        }
    }

    //用于判断钟表选定的时间是不是在今天，是今天星期盘选定为今天，不是则往后推一天
    private fun isClockTimeIsToday(): Boolean {
        if (clockTime != null) {
            //获取今天是几号
            val date = Date()
            val simpleFormat = SimpleDateFormat("dd")
            val today = simpleFormat.format(date)

            //获取时刻表选取的是几号
            val temp = clockTime!!.split(" ")
            val temp_1 = temp[0].split("-")
            val selectDay = temp_1[2]

            if (today.equals(selectDay)) {
                return true
            }
        }
        return false
    }
}
