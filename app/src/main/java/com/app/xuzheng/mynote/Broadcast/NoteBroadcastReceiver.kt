package com.app.xuzheng.mynote.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by xuzheng on 2017/7/27.
 */

const val BROADCAST_RECEIVER = "broadcast"

class NoteBroadcastReceiver : BroadcastReceiver() {


    //在这里可以监听系统网络的变化，从而做出一些操作
    //但是，这种监听在深度rom上需要开启自启动权限，否则静态注册广播也收不到广播
    //另外，note的启动需要activity，而我们不可能每次网络变化一旦发现note关闭就启动我们的app
    //所以这段功能弃用
    //但是可以有这样的操作：
    //写一个后台的service，监听用户的操作。虽然service容易被杀，但是一旦某些系统广播触发，
    //就能重新启用service重新监听。但是前提：需要用户主动去开启自启动权限
    //可见：自启动权限很危险，它给了app注册系统广播从而频繁自启继而耗电严重的权利
    //还有的app厂商通过逆向分析别人app代码里的广播名称，继而注册给自己app相同的广播名称，实现
    //app间的相互启动。但是经过测试，这个在app被杀后，也需要自启动权限
    override fun onReceive(context: Context?, intent: Intent?) {
        //需要自启动权限 方能在app被杀后也能继续监听网络变化
    }

//    1 在普通情况下,必须要有前一个Activity的Context,才能启动后一个Activity
//    2 但是在BroadcastReceiver里面是没有Activity的Context的
//    3 对于startActivity()方法,源码中有这么一段描述:
//    Note that if this method is being called from outside of an
//    {@link android.app.Activity} Context, then the Intent must include
//    the {@link Intent#FLAG_ACTIVITY_NEW_TASK} launch flag.  This is because,
//    without being started from an existing Activity, there is no existing
//    task in which to place the new activity and thus it needs to be placed
//    in its own separate task.
//    说白了就是如果不加这个flag就没有一个Task来存放新启动的Activity.
}