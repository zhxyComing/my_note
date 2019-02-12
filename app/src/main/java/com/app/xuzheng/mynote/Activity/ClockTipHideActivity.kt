package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.app.xuzheng.mynote.R
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import java.text.SimpleDateFormat
import java.util.*
import android.os.Vibrator
import android.os.PowerManager
import android.view.WindowManager


class ClockTipHideActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock_tip_hide)

        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        val clockTime = format.format(Date())
        val content = NoteTextUtil.getClockContentByName(this@ClockTipHideActivity, clockTime)
        //对于某些手机，设定了闹铃，一旦熄灭屏幕，他只有在重新亮屏时才能receiver到..这种情况下我无法正确获取时间，也就无法正确获取提醒文件，不如直接让他关掉。
        if (content == null) {
            finish()
        } else {
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or //这个在锁屏状态下
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            //点亮屏幕
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val screen = pm.isScreenOn
            if (!screen) {//如果灭屏
                //相关操作
                val wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG")
                //屏幕会持续点亮
                wakeLock.acquire()
            }

            val dialog = PromptDialog(this)
            dialog.setCancelable(false)
            dialog.setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                    .setAnimationEnable(true)
                    .setTitleText("消息提醒")
                    .setContentText(content)
                    .setPositiveListener("我知道了") { dialog ->
                        run {
                            cancelVibrate()
                            dialog.dismiss()
                        }
                    }.show()

            dialog.setOnDismissListener {
                finish()
            }

            //设置震动  默认是打开的
            val isVibrate = SharedpreferenceManager.getBoolean(this, SharedpreferenceManager.IS_OPEN_VIBRATE, true)
            if (isVibrate) {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val longArray = LongArray(6)
                longArray[0] = 0 //等待
                longArray[1] = 200 //震动
                longArray[2] = 200 //等待
                longArray[3] = 200 //震动
                longArray[4] = 200 //等待
                longArray[5] = 200 //震动
                vibrator.vibrate(longArray, 2)

                //最多震动10s
                Thread(Runnable {
                    Thread.sleep(5000)
                    runOnUiThread {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.cancel()
                    }
                }).start()

            }
        }
    }

    fun cancelVibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
    }
}
