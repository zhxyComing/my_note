package com.app.xuzheng.mynote.Utils

import com.app.xuzheng.mynote.Bean.AppInfo

/**
 * Created by xuzheng on 2017/9/12.
 */
class SortByAppName : Comparator<AppInfo> {
    //这个地方应该有更好的kotlin写法
    override fun compare(note_1: AppInfo, note_2: AppInfo): Int {

        var name_1 = note_1.appName.trim()[0]
        val name_1_temp = SystemUtil.getFirstLetter(name_1)
        if (name_1_temp != null) {
            name_1 = name_1_temp
        }

        var name_2 = note_2.appName.trim()[0]
        val name_2_temp = SystemUtil.getFirstLetter(name_2)
        if (name_2_temp != null) {
            name_2 = name_2_temp
        }

        return name_1.toLowerCase().compareTo(name_2.toLowerCase())
    }

}