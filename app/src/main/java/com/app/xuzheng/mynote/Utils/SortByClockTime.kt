package com.app.xuzheng.mynote.Utils

import com.app.xuzheng.mynote.Bean.ClockInfo

/**
 * Created by xuzheng on 2017/9/12.
 */
class SortByClockTime : Comparator<ClockInfo> {
    //这个地方应该有更好的kotlin写法
    override fun compare(clock_1: ClockInfo, clock_2: ClockInfo): Int {

        val date_1 = TimeUtils.stringToLong(clock_1.fileName,"yyyy-MM-dd-HH-mm-ss")
        val date_2 = TimeUtils.stringToLong(clock_2.fileName,"yyyy-MM-dd-HH-mm-ss")

        return date_1.compareTo(date_2)
    }
}