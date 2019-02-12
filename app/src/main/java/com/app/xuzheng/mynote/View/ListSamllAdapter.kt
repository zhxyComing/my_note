package com.app.xuzheng.mynote.View

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.app.xuzheng.mynote.Bean.AppInfoSmall
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.SystemUtil
import java.util.*

/**
 * Created by xuzheng on 2017/8/15.
 */

class ListSmallAdapter(val context: Context, var list: List<AppInfoSmall>) : BaseAdapter() {

    override fun getView(position: Int, view: View?, vg: ViewGroup?): View? {
        var vh: ViewHolder?
        var convertView: View?
        if (view == null) {
            //这一栏是第一次加载，该View还没有视图，更没有绑定组件；
            //以下操作就是给view 设置视图，绑定组件
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.app_list_small_item, null)
            vh = ViewHolder(convertView)
            convertView.tag = vh
        } else {
            //说明是复用的情况，这是一级缓存
            //直接获取tag，避免复用的视图重新获取View id，这是二级缓存
            convertView = view
            vh = convertView.tag as ViewHolder
        }

        val appInfo = list[position]
        vh.appName.text = appInfo.name
        if (!TextUtils.isEmpty(appInfo.packageName)) {
            vh.appName.setOnClickListener {
                SystemUtil.startApkByPackageName(appInfo.packageName, context, appInfo.name)
                vh?.appName?.isClickable = false
                //启动一个应用后，两秒后才能在此启动该应用
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        vh?.appName?.post({
                            it.isClickable = true
                        })
                    }
                }, 300)
            }
        }
        return convertView
    }

    override fun getItem(position: Int): Any? = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = list.size

    class ViewHolder(val view: View) {
        val appName: TextView = view.findViewById(R.id.tvAppNameSmall)
    }
}