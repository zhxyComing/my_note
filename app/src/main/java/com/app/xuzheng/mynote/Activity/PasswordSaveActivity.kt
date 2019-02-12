package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.Toast
import com.app.xuzheng.mynote.AccountAdapter
import com.app.xuzheng.mynote.Bean.Account
import com.app.xuzheng.mynote.Fragment.NOTE_LIST_REFRESH
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.JsonUtils
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import kotlinx.android.synthetic.main.activity_password_save.*

class PasswordSaveActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_save)

        val list: ArrayList<Account> = ArrayList()
        val list_account = SharedpreferenceManager.getString(this, SharedpreferenceManager.ACCOUNT)
        if (!TextUtils.isEmpty(list_account)) {
            list.addAll(JsonUtils.parseAccountToList(list_account))
        }
        //我觉得，这里的list并不是java里的引用传递，而是值传递，所以导致不刷新
        //测试结果果然如此...
        //★ 测试java的adapter，测试kotlin的listView的adapter
        //bug原因：...adapter用的addAll，而不是=
        accountList.adapter = AccountAdapter(this, list, accountDis, passwordDis)
//        accountList.layoutManager = GridLayoutManager(this, 2)
        accountList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        tvSaveAccount.setOnClickListener {
            val account = etAccount.text.toString()
            val password = etPassword.text.toString()
            val source = etSource.text.toString()
            if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(source)) {
                (accountList.adapter as AccountAdapter).mList.add(Account(source, account, password))
                val result = JsonUtils.parseAccountToJson((accountList.adapter as AccountAdapter).mList)
                SharedpreferenceManager.setString(this, SharedpreferenceManager.ACCOUNT, result)
                etAccount.setText("")
                etPassword.setText("")
                etSource.setText("")
                refresh()
            } else {
                Toast.makeText(this, "请先完善信息", Toast.LENGTH_SHORT).show()
            }
        }

        tvSaveToDesktop.setOnClickListener {
            if (!TextUtils.isEmpty(accountDis.text) && !TextUtils.isEmpty(passwordDis.text)) {
                val res = "\n账号:" + accountDis.text.toString() + "\n密码:" + passwordDis.text.toString()
                MyNoteApplication.etNote?.let {
                    MyNoteApplication.etNote?.setText(MyNoteApplication.etNote?.text.toString() + res)
                    NoteTextUtil.saveNote(this, MyNoteApplication.etNote?.text.toString())
                    sendBroadcast(Intent(NOTE_LIST_REFRESH))
                } ?: let {
                    var text: String = NoteTextUtil.getNote(this)
                    NoteTextUtil.saveNote(this, text + res)
                    sendBroadcast(Intent(NOTE_LIST_REFRESH))
                }
                Toast.makeText(this, "账密已复制到当前桌签", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "请先选择一个账号", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refresh() {
        accountList.adapter.notifyDataSetChanged()
    }
}
