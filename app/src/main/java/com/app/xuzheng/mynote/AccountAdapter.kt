package com.app.xuzheng.mynote

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.refactor.lib.colordialog.ColorDialog
import com.app.xuzheng.mynote.Bean.Account
import com.app.xuzheng.mynote.Bean.AppInfo
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.Utils.JsonUtils
import com.app.xuzheng.mynote.Utils.SystemUtil

/**
 * Created by xuzheng on 2017/8/24.
 */
class AccountAdapter(context: Context, list: ArrayList<Account>, val accountTv: TextView, val passwordTv: TextView) : RecyclerView.Adapter<AccountAdapter.MyViewHolder>() {

    var mContext: Context = context
    var mList: ArrayList<Account> = ArrayList()
    var mAppInfos: ArrayList<AppInfo> = ArrayList()

    init {
        mList.addAll(list)

        mAppInfos.addAll(SystemUtil.getAppInfos(mContext))
    }

    //赋值操作
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.source.text = mList[position].source
        holder.delete.setOnClickListener {
            val dialog = ColorDialog(mContext)
            dialog.setTitle("警告")
            dialog.contentText = "确定删除该条记录？"
//            dialog.setContentImage(resources.getDrawable(R.mipmap.sample_img))
            dialog.setPositiveListener("确定") { dialog ->
                deleteAccount(position)
                dialog.dismiss()
            }
                    .setNegativeListener("取消") { dialog ->
                        dialog.dismiss()
                    }.show()

        }
        val app_icon: Drawable? = mAppInfos
                .firstOrNull { it.appName.toLowerCase().equals(mList[position].source.toLowerCase()) }
                ?.icon
        if (app_icon == null) {
            holder.icon.setImageDrawable(null)
            holder.icon.visibility = View.GONE
        } else {
            holder.icon.setImageDrawable(app_icon)
            holder.icon.visibility = View.VISIBLE
        }

        holder.source.setOnClickListener {
            accountTv.text = mList[position].account
            passwordTv.text = mList[position].password
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    //给Holder加载视图被返回该Holder
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.account_list_item, parent, false))
    }

    //还是相当于以前的ViewHolder，只不过这个ViewHolder能直接贯通于该Adapter
    //如直接在onBindViewHolder里执行赋值操作
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var source: TextView = view.findViewById(R.id.source)
        var delete: ImageView = view.findViewById(R.id.btnDeleteAccount)
        var icon: ImageView = view.findViewById(R.id.icon)
    }

    private fun deleteAccount(position: Int) {
        mList.removeAt(position)
        notifyDataSetChanged()
        val result = JsonUtils.parseAccountToJson(mList)
        SharedpreferenceManager.setString(mContext, SharedpreferenceManager.ACCOUNT, result)
    }
}