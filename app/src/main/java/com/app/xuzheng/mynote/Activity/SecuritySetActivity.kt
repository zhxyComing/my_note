package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.activity_security_set.*
import cn.refactor.lib.colordialog.ColorDialog
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager

const val SECURITY_SET = "security_set"

class SecuritySetActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_set)

        tvResetPass.setOnClickListener {
            val intent = Intent(this, SecurityActivity::class.java)
            intent.putExtra("from", SECURITY_SET)
            startActivityForResult(intent, 0)
        }

        tvClearPass.setOnClickListener {
            val dialog = ColorDialog(this)
            dialog.setTitle("警告")
            dialog.contentText = "强制重置密码会将密码与保存的账号信息一并清除，确定删除所有保存的账号并清空密码吗？"
//            dialog.setContentImage(resources.getDrawable(R.mipmap.sample_img))
            dialog.setPositiveListener("确定") { dialog ->
                dialog.dismiss()
                //对于这些敏感操作，应该以权限的方式控制。目前比较简单，随处都能调用，实际很不安全
                SharedpreferenceManager.setString(this, SharedpreferenceManager.PASS_WORD, "")
                //这里删除所有保存的数据
                SharedpreferenceManager.setString(this, SharedpreferenceManager.ACCOUNT, "")
                Toast.makeText(this, "账户密码已清空", Toast.LENGTH_SHORT).show()
            }
                    .setNegativeListener("取消") { dialog ->
                        dialog.dismiss()
                    }.show()
        }
    }
}
