package com.app.xuzheng.mynote

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mynote.Bean.ClockInfo
import com.app.xuzheng.mynote.Broadcast.ClockBroadcastReceiver
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import com.app.xuzheng.mynote.Utils.TimeUtils
import java.util.*

/**
 * Created by xuzheng on 2017/8/18.
 */
class ClockInfoAdapter(context: Context, list: ArrayList<ClockInfo>) : RecyclerView.Adapter<ClockInfoAdapter.MyViewHolder>() {

    var mContext: Context = context
    var mList: ArrayList<ClockInfo> = ArrayList()

    init {
        mList = list
    }

    //赋值操作
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvContent.text = mList[position].content

        holder.ivRemove.setOnClickListener {
            val dialog = PromptDialog(mContext)
            dialog.setCancelable(true)
            dialog.setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("警告")
                    .setContentText("确定移除该闹铃提醒？")
                    .setPositiveListener("确定") {
                        //移除记录
                        NoteTextUtil.removeClock(mContext, mList[position].fileName)
                        //移除闹铃
                        val intent = Intent(mContext, ClockBroadcastReceiver::class.java)
                        TimeUtils.cancelClock(mContext, intent, mList[position].requestCode)
                        //从列表中移除
                        mList.removeAt(position)
                        notifyDataSetChanged()
                        //关闭对话框
                        dialog.dismiss()
                    }.show()
        }

        val time = mList[position].fileName
        val builder = StringBuilder()
        val times = time.split("-")

        builder.append(times[0]).append("/").append(times[1]).append("/").append(times[2]).append(" ").append(times[3]).append(":").append(times[4]).append(":").append(times[5])
        holder.tvTime.text = "提醒时间:" + builder.toString()

        val selectDay = builder.toString().replace("/", "-")
        if (selectDay != null && !TimeUtils.isBigger(selectDay)) {
            holder.tvIsOut.visibility = View.VISIBLE
        } else {
            holder.tvIsOut.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    //给Holder加载视图被返回该Holder
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.clock_info_item, parent, false))
    }

    //还是相当于以前的ViewHolder，只不过这个ViewHolder能直接贯通于该Adapter
    //如直接在onBindViewHolder里执行赋值操作
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvContent: TextView = view.findViewById(R.id.tvContent)
        var tvTime: TextView = view.findViewById(R.id.tvTime)
        var ivRemove: ImageView = view.findViewById(R.id.ivRemove)
        var tvIsOut: TextView = view.findViewById(R.id.tvIsOut)
    }

}