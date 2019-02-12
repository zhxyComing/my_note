package com.app.xuzheng.mynote

import android.content.Context
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.app.xuzheng.mynote.Bean.ExpandFunction
import com.app.xuzheng.mynote.RecyclerAdapter.MyViewHolder


/**
 * Created by xuzheng on 2017/7/25.
 * 用RecyclerView会很卡，并且目前也不会批量生产主题，暂时弃用
 */
//抽象类，不做为主构造函数时不需要() ?
class RecyclerAdapter(context: Context, list: ArrayList<ExpandFunction>) : Adapter<MyViewHolder>() {

    var mContext: Context? = context
    var mList: ArrayList<ExpandFunction> = ArrayList()

    init {
        mList.addAll(list)
    }

    //赋值操作
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvName.text = mList[position].name
        holder.ivImage.setImageResource(mList[position].drawable)
        holder.ivImage.setOnClickListener {
            mList[position].clickListener.onClick()
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    //给Holder加载视图被返回该Holder
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recycle_item, parent, false))
    }

    //还是相当于以前的ViewHolder，只不过这个ViewHolder能直接贯通于该Adapter
    //如直接在onBindViewHolder里执行赋值操作
    inner class MyViewHolder(view: View) : ViewHolder(view) {
        var tvName: TextView = view.findViewById(R.id.tvName)
        var ivImage: ImageView = view.findViewById(R.id.ivBack)
    }

    interface OnClickListener {
        fun onClick()
    }

}