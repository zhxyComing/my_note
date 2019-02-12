package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.View.TimeSetLayout.ClockActivity
import kotlinx.android.synthetic.main.activity_more_set.*

class MoreSetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.e("testkkk","" + R.style.ThemeDark)
//        setTheme(R.style.ThemeDark)
        setContentView(R.layout.activity_more_set)


        swHideDesktop.isChecked = SharedpreferenceManager.getBoolean(this, SharedpreferenceManager.IS_HIDE_DESKTOP, true)
        swOpenSlide.isChecked = SharedpreferenceManager.getBoolean(this, SharedpreferenceManager.IS_SHOW_SLIDE_VIEW, false)

        tvTheme.setOnClickListener {
            Toast.makeText(this, "主题回炉重制中\n\n小便签近期将上线全新主题", Toast.LENGTH_LONG).show()
        }

        tvAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        returnList.setOnClickListener {
            finish()
        }

        tvPassword.setOnClickListener {
            startActivity(Intent(this, SecuritySetActivity::class.java))
        }

        tvClock.setOnClickListener {
            startActivity(Intent(this, ClockActivity::class.java))
        }

        swHideDesktop.setOnCheckedChangeListener { _, isChecked ->
            SharedpreferenceManager.setBoolean(this, SharedpreferenceManager.IS_HIDE_DESKTOP, isChecked)
            if (!isChecked) {
                sendBroadcast(Intent(CLOSE_NOTE_BROADCAST))
            }
        }

        tvAllClock.setOnClickListener {
            startActivity(Intent(this@MoreSetActivity, AllClockActivity::class.java))
        }

        tvClockSet.setOnClickListener {
            startActivity(Intent(this@MoreSetActivity, ClockSetActivity::class.java))
        }

        swOpenSlide.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val dialog = PromptDialog(this@MoreSetActivity)
                dialog.setCancelable(true)
                dialog.setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                        .setAnimationEnable(true)
                        .setTitleText("引导")
                        .setContentText("隐身便签就藏在手机左侧边里，赶快把它滑出来吧~")
                        .setPositiveListener("确定") {
                            dialog.dismiss()
                        }.show()
            }
            SharedpreferenceManager.setBoolean(this, SharedpreferenceManager.IS_SHOW_SLIDE_VIEW, isChecked)
            val intent = Intent(SLIDE_VIEW_BROADCAST)
            intent.putExtra("isChecked", isChecked)
            sendBroadcast(intent)
        }
    }
}
