package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.os.Bundle
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.activity_more_set.*

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        returnList.setOnClickListener { finish() }
    }
}
