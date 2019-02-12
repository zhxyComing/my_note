package com.app.xuzheng.mynote.Service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Build

/**
 * Created by xuzheng on 2017/7/20.
 * 灰色保活
 * 通过运行在主进程的前台service，来提高主进程Aty退到后台时的优先级，避免被杀
 *
 * 一般前台Service都有一个Notification
 *
 * 灰色保活通过先后启动两个服务，而这两个服务会启动同一个id的Notification，
 * 之后后启动的Service关闭掉所有的Notification，并关掉自身Service
 * 这样就利用系统漏洞创造出了一个没有Notification的前台Service进程
 *
 * 猜想：前台Service一旦关掉Notification，就不是前台Service了
 * 所以可以通过启动同一个Notification的方式，让另一个Service去关掉，
 * 并只降另一个Service为后台Service，之后关掉该Service即可
 * 这样原先的Service还是前台Service
 */

const val NOTE_SERVICE_ID = 1001

class NoteService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
//        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(NOTE_SERVICE_ID, Notification())
        } else {
            val innerIntent = Intent(this, NoteInnerService::class.java)
            startService(innerIntent)
            startForeground(NOTE_SERVICE_ID, Notification())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    //相当于静态内部类
    class NoteInnerService : Service() {
        override fun onBind(intent: Intent): IBinder? {
            return null
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            startForeground(NOTE_SERVICE_ID, Notification())
            stopForeground(true)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
    }

    /**
     * 创建普通前台Service的方法
     * 创建Notification的方法
     */
    /*
    override fun onDestroy() {
        //stopForeground(true)
        super.onDestroy()
    }

    private fun showNotification() {
        val builder = Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("测试题目")
                .setContentText("测试内容")
        //创建点击跳转Intent
        val intent = Intent(this, MainActivity::class.java)
        //创建任务栈
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(intent)
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        //设置跳转Intent到通知中
        builder.setContentIntent(pendingIntent)
        //获取通知服务
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //构建通知
        val notification = builder.build()
        //显示通知
        manager.notify(0, notification)
        //启动为前台服务
        startForeground(0, notification)
    }
    */
}