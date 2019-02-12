package com.app.xuzheng.mynote

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.app.xuzheng.mynote.Bean.AppInfoSmall
import com.app.xuzheng.mynote.Utils.SystemUtil

/**
 * Created by xuzheng on 2017/8/18.
 */
class CommonUseAppAdapter(context: Context, list: ArrayList<AppInfoSmall>) : RecyclerView.Adapter<CommonUseAppAdapter.MyViewHolder>() {

    var mContext: Context = context
    var mList: ArrayList<AppInfoSmall> = ArrayList()

    init {
        mList.addAll(list)
    }

    //赋值操作
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvName.text = mList[position].name
        holder.tvName.setOnClickListener {
            SystemUtil.startApkByPackageName(mList[position].packageName, mContext, mList[position].name)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    //给Holder加载视图被返回该Holder
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.common_use_adapter_item, parent, false))
    }

    //还是相当于以前的ViewHolder，只不过这个ViewHolder能直接贯通于该Adapter
    //如直接在onBindViewHolder里执行赋值操作
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvName: TextView = view.findViewById(R.id.tvName)
    }

}