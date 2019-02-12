package com.app.xuzheng.mynote.Utils

/**
 * Created by xuzheng on 2017/8/2.
 */
interface DownLoadProgressListener {
    fun begin()

    fun done(var1: String)

    fun error()

    fun progress(var1: Int)

    fun cancel()
}