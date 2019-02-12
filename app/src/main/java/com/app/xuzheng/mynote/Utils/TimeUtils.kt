package com.app.xuzheng.mynote.Utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by xuzheng on 2017/9/8.
 */
object TimeUtils {

    //string转long
    @Throws(ParseException::class)
    fun stringToLong(strTime: String, formatType: String): Long {
        val date = stringToDate(strTime, formatType) // String类型转成date类型
        if (date == null) {
            return 0
        } else {
            val currentTime = dateToLong(date) // date类型转成long类型
            return currentTime
        }
    }

    //日期转long
    fun dateToLong(date: Date): Long {
        return date.time
    }

    //string转日期
    @Throws(ParseException::class)
    fun stringToDate(strTime: String, formatType: String): Date {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    //设定闹铃 Intent第二个参数要传Broadcast
    fun setClock(context: Context, intent: Intent, time: String, format: String, content: String) {
        //唯一区分闹铃
        var requestCode = SharedpreferenceManager.getInt(context, SharedpreferenceManager.CLOCK_REQUEST_CODE)
        val sender = PendingIntent.getBroadcast(
                context, requestCode, intent, 0)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = stringToLong(time, format)

        // Schedule the alarm!
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
        } else {
//            Toast.makeText(context, "Android版本4.4及以上才能使用哦~", Toast.LENGTH_SHORT).show()
            am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
        }

        NoteTextUtil.saveClock(context, content, time, requestCode)

        requestCode++
        SharedpreferenceManager.setInt(context, SharedpreferenceManager.CLOCK_REQUEST_CODE, requestCode)
    }

    fun cancelClock(context: Context, intent: Intent, requestCode: Int) {
        val sender = PendingIntent.getBroadcast(
                context, requestCode, intent, 0)
        // Schedule the alarm!
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        am.cancel(sender)
    }

    //两个时间比较大小 参数格式：yyyy-MM-dd HH:mm:ss
    fun isDateOneBigger(str1: String, str2: String): Boolean {
        var isBigger = false
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var dt1: Date? = null
        var dt2: Date? = null
        try {
            dt1 = sdf.parse(str1)
            dt2 = sdf.parse(str2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        if (dt1!!.time > dt2!!.time) {
            isBigger = true
        } else if (dt1.time < dt2.time) {
            isBigger = false
        }
        return isBigger
    }

    //判定该时间是否比现在靠后 参数格式：yyyy-MM-dd HH:mm:ss
    fun isBigger(str1: String): Boolean {
        var isBigger = false
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var dt1: Date? = null
        try {
            dt1 = sdf.parse(str1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        if (dt1!!.time > Date()!!.time) {
            isBigger = true
        } else if (dt1.time < Date().time) {
            isBigger = false
        }
        return isBigger
    }

    //判定该时间是否比现在靠后 参数格式：yyyy-MM-dd HH:mm:ss
    fun isBigger(str1: String, format: String): Boolean {
        var isBigger = false
        val sdf = SimpleDateFormat(format)
        var dt1: Date? = null
        try {
            dt1 = sdf.parse(str1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        if (dt1!!.time > Date()!!.time) {
            isBigger = true
        } else if (dt1.time < Date().time) {
            isBigger = false
        }
        return isBigger
    }

    //获取从今天起指定向前向后推迟的日期  返回格式 "yyyy-MM-dd"
    fun getAppointDayFromToady(delay: Int): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DATE)
        calendar.set(Calendar.DATE, day + delay)

        val format = SimpleDateFormat("yyyy-MM-dd")
        val res = format.format(calendar.time)
        return res
    }

    //根据时分秒获取从今天起指定推迟或提前的日期  返回格式 yyyy-MM-dd HH:mm:ss
    fun getAppointDayFormTodayByHour(delay: Int, hourMinuteSecond: String): String {
        val calendar = Calendar.getInstance()

        val date = Date()
        val simpleFormat = SimpleDateFormat("yyyy-MM-dd")
        val yearMonthDay = simpleFormat.format(date)
        val time = yearMonthDay + " " + hourMinuteSecond

        calendar.time = stringToDate(time, "yyyy-MM-dd HH:mm:ss")
        val day = calendar.get(Calendar.DATE)
        calendar.set(Calendar.DATE, day + delay)

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val res = format.format(calendar.time)
        return res
    }

    fun getNowDay(): String {
        val format = SimpleDateFormat("yyyy-MM-dd")
        val res = format.format(Date())
        return res
    }

    //拼接年月日  time是"HH:mm:ss" 比当前时间>,才能作为闹钟的时间
    fun getClockTime(time: String): String {
        var completeTime = getNowDay() + " " + time
        if (isBigger(completeTime)) {
            return completeTime
        } else {
            return getAppointDayFromToady(1) + " " + time
        }
    }

    //得到目标时间到现在的时间差 参数格式 "yyyy-MM-dd HH:mm:ss"
    fun getTimeDifference(time: String): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val endDate = df.parse(time)
        val startDate = Date(System.currentTimeMillis())

        val dis = endDate.time - startDate.time

        val hour = (dis / (1000 * 60 * 60)).toInt()
        val minute = ((dis % (1000 * 60 * 60)) / (1000 * 60)).toInt() + 1 //不到1分钟，按1分钟算

        return "" + hour + "小时$minute" + "分钟"
    }

    //获取今天是星期几 以数字1..7显示
    fun getTodayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        var weekNow = calendar.get(Calendar.DAY_OF_WEEK) - 1
        if (weekNow == 0) {
            weekNow = 7
        }
        return weekNow
    }
}