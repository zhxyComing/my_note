package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.os.Bundle
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.activity_clock_set.*

class ClockSetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock_set)

        swOpenVibrate.isChecked = SharedpreferenceManager.getBoolean(this@ClockSetActivity, SharedpreferenceManager.IS_OPEN_VIBRATE, true)
        swOpenVibrate.setOnCheckedChangeListener { _, checked ->
            SharedpreferenceManager.setBoolean(this@ClockSetActivity, SharedpreferenceManager.IS_OPEN_VIBRATE, checked)
        }
    }
}
