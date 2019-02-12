package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.activity_security.*

class SecurityActivity : Activity() {

    var isFirstSetPassword = true
    var firstInputPassword = ""
    var pass = ""
    var isSetPass = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        val from = intent.getStringExtra("from")
        if (!TextUtils.isEmpty(from) && from.equals(SECURITY_SET)) {
            setInit()
        } else {
            commonInit()
        }
    }

    //从 密码修改 进入
    private fun setInit() {
        if (hasPass()) {
            etPass.hint = "请输入旧密码"
            btnYes.setOnClickListener {
                val str = etPass.text.toString()
                if (!TextUtils.isEmpty(str)) {
                    //验证密码，而非修改密码
                    if (!isSetPass) {
                        if (pass.equals(str)) {
                            //输入密码与旧密码匹配  可以修改密码
                            etPass.hint = "请输入新密码"
                            etPass.setText("")
                            isSetPass = true
                        } else {
                            //输入密码错误 不能修改
                            Toast.makeText(this, "密码输入错误！", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        savePassword(str)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            etPass.hint = getString(R.string.set_pass_set_tip)
            btnYes.setOnClickListener {
                val str = etPass.text.toString()
                if (!TextUtils.isEmpty(str)) {
                    if (isFirstSetPassword) {
                        isFirstSetPassword = false
                        firstInputPassword = str
                        etPass.setText("")
                        etPass.hint = "请再次输入密码..."
                    } else {
                        if (str.equals(firstInputPassword)) {
                            savePassword(firstInputPassword)
                            finish()
                        } else {
                            isFirstSetPassword = true
                            firstInputPassword = ""
                            etPass.setText("")
                            etPass.hint = getString(R.string.set_pass_set_tip)
                            Toast.makeText(this, "两次密码不同，请重新输入！", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnNo.setOnClickListener {
            finish()
        }
    }

    private fun hasPass(): Boolean {
        pass = SharedpreferenceManager.getString(this, SharedpreferenceManager.PASS_WORD)
        if (TextUtils.isEmpty(pass)) {
            return false
        }
        return true
    }

    //从 密码保险 进入
    private fun commonInit() {
        if (hasPass()) {
            btnYes.setOnClickListener {
                val str = etPass.text.toString()
                if (!TextUtils.isEmpty(str)) {
                    if (pass.equals(str)) {
                        openSavePassAty()
                    } else {
                        Toast.makeText(this, "密码输入错误！", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            etPass.hint = getString(R.string.set_pass_tip)
            btnYes.setOnClickListener {
                val str = etPass.text.toString()
                if (!TextUtils.isEmpty(str)) {
                    if (isFirstSetPassword) {
                        isFirstSetPassword = false
                        firstInputPassword = str
                        etPass.setText("")
                        etPass.hint = "请再次输入密码..."
                    } else {
                        if (str.equals(firstInputPassword)) {
                            savePassword(firstInputPassword)
                            openSavePassAty()
                        } else {
                            isFirstSetPassword = true
                            firstInputPassword = ""
                            etPass.setText("")
                            etPass.hint = getString(R.string.set_pass_tip)
                            Toast.makeText(this, "两次密码不同，请重新输入！", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnNo.setOnClickListener {
            finish()
        }
    }

    private fun savePassword(password: String) {
        if (!TextUtils.isEmpty(password)) {
            SharedpreferenceManager.setString(this, SharedpreferenceManager.PASS_WORD, password)
            Toast.makeText(this, "密码保存成功！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSavePassAty() {
        startActivity(Intent(this, PasswordSaveActivity::class.java))
        finish()
    }
}
