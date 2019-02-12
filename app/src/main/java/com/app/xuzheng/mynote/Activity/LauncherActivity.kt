package com.app.xuzheng.mynote.Activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.activity_launcher.*
import java.util.*

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        /**
         * isTaskRoot 判断是否是第一个Activity，即如果不是首次启动，就跳过启动页
         *
         * 这是Android的一个奇葩bug，即回到桌面后，点击桌面图标，本应该直接回到退出页
         * 但是却莫名其妙的打开了启动页
         */
        if (!isTaskRoot) {
            finish()
            return
        }

        val task = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
                    //下一页进入的动画 和 本页退出的动画
                    overridePendingTransition(R.anim.start_in, R.anim.start_out)
                    finish()
                }
            }
        }
        val timer = Timer()
        timer.schedule(task, 2500)

        val propertyAlpha = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        val propertyTran = PropertyValuesHolder.ofFloat("translationY", 100f, 0f)
        ObjectAnimator.ofPropertyValuesHolder(launch_title, propertyAlpha, propertyTran).setDuration(1500).start()
    }
}
