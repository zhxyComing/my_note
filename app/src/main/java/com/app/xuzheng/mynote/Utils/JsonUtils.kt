package com.app.xuzheng.mynote.Utils

import com.app.xuzheng.mynote.Bean.Account
import com.app.xuzheng.mynote.Bean.AppInfoSmall
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by xuzheng on 2017/8/15.
 */
object JsonUtils {
    fun parseToJson(list: ArrayList<AppInfoSmall>): String {
        return Gson().toJson(list)
    }

    fun parseToList(str: String): List<AppInfoSmall> {
        val list: List<AppInfoSmall> = Gson().fromJson(str, object : TypeToken<List<AppInfoSmall>>() {}.type)
        //这个好像是从小到大排序
        val res = list.sortedBy {
            it.num
        }
        return res
    }

    fun parseAccountToJson(list: ArrayList<Account>): String {
        return Gson().toJson(list)
    }

    fun parseAccountToList(str: String): List<Account> {
        val list: List<Account> = Gson().fromJson(str, object : TypeToken<List<Account>>() {}.type)
        return list
    }
}