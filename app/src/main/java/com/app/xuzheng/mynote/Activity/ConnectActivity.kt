package com.app.xuzheng.mynote.Activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.SystemUtil

//用于跳转到其它app的singleInstance activity
//★ 这个bug解的赶上et在桌面的同步输入了
class ConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra("packageName")
        val appName = intent.getStringExtra("appName")
        if (packageName == null || appName == null) {
            finish()
            return
        }
        try {
            val startIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (startIntent == null) {
                Toast.makeText(this, "Error : Intent is null", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            startIntent.action = Intent.ACTION_MAIN
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            startActivity(startIntent)
            overridePendingTransition(R.anim.start_in, R.anim.start_out)

            SystemUtil.saveAppOpenMessage(this, appName, packageName)
        } catch (e: Exception) {
            Toast.makeText(this, "APP无法跳转", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
