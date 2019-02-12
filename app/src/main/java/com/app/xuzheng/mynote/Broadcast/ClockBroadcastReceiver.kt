package com.app.xuzheng.mynote.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.xuzheng.mynote.Activity.ClockTipHideActivity

/**
 * Created by xuzheng on 2017/7/27.
 */

const val CLOCK_BROADCAST_RECEIVER = "com.app.xuzheng.mynote.Broadcast.clock_broadcast"

class ClockBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //需要自启动权限 方能在app被杀后也能继续监听网络变化
        val noteList = Intent(context, ClockTipHideActivity::class.java)
        noteList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(noteList)
    }

}