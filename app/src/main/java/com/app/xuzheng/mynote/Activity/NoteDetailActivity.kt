package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.app.xuzheng.mynote.Fragment.NOTE_LIST_REFRESH
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import kotlinx.android.synthetic.main.activity_note_detail.*

class NoteDetailActivity : Activity() {

    var fileName: String = ""
    var content: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        fileName = intent.getStringExtra("file_name")

        returnList.setOnClickListener {
            finish()
        }

        noteDetail.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            tvContentSize.text = noteDetail.text.length.toString()
        }
    }

    override fun onStart() {
        super.onStart()
        //后台重启要重新矫正文字
        content = NoteTextUtil.getNoteByName(this, fileName)
        noteDetail.setText(content)

        tvContentSize.text = noteDetail.text.length.toString()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        //这个数据实际上不传也可以
        val content_temp = intent?.getStringExtra("data")
        val fileName_temp = intent?.getStringExtra("file_name")

        if (content_temp != null && fileName_temp != null && fileName_temp != fileName) {
            content = content_temp
            fileName = fileName_temp
            noteDetail.setText(content)
        }
    }

    override fun onPause() {
        //信息的保存应该交给Pause
        //按照声明周期，A:onPause - B:onResume - B:onStart - A:onStop
        //★注：该声明周期要总结！！！
        NoteTextUtil.saveNoteByName(this, noteDetail.text.toString(), fileName)
        sendBroadcast(Intent(NOTE_LIST_REFRESH))
        super.onPause()
    }

    override fun onDestroy() {
        noteDetail.removeTextChangedListener(textWatcher)
        super.onDestroy()
    }
}
