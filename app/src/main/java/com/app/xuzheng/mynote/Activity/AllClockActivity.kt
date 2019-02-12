package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.app.xuzheng.mynote.ClockInfoAdapter
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import kotlinx.android.synthetic.main.activity_all_clock.*

class AllClockActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_clock)

        rvAllClick.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val list = NoteTextUtil.getClockInfoList(this)
        if (list.size == 0) {
            tvClockTip.visibility = View.VISIBLE
        }
        rvAllClick.adapter = ClockInfoAdapter(this, list)
    }
}
