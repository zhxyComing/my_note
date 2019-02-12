package com.app.xuzheng.mynote

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.app.xuzheng.mynote.Bean.NoteInfo
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import com.app.xuzheng.myslidelayout.MySlideLayout
import java.util.*

/**
 * Created by xuzheng on 2017/8/18.
 *
 * 侧边栏显示所有便签
 */
class SlideNoteInfoAdapter(val context: Context, var list: ArrayList<NoteInfo>, val slideView: MySlideLayout?) : RecyclerView.Adapter<SlideNoteInfoAdapter.MyViewHolder>() {

    //赋值操作
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.tvName.text = mList[position].name
//        holder.tvName.setOnClickListener {
//            SystemUtil.startApkByPackageName(mList[position].packageName, mContext, mList[position].name)
//        }
        var isDesktopNote = false
        if (list[position].fileName.equals(
                SharedpreferenceManager.getString(context, SharedpreferenceManager.NOW_NOTE))) {
            isDesktopNote = true
        }
        val text = NoteTextUtil.getNoteByName(context, list[position].fileName)
        holder.tvContent.text = text
        holder.tvContent.setOnClickListener {
            slideView?.hideSlideView()
            startNoteEdit(position)
        }


        val time = list[position].fileName
        val builder = StringBuilder()
        val times = time.split("-")

        builder.append(times[0]).append("/").append(times[1]).append("/").append(times[2]).append(" ").append(times[3]).append(":").append(times[4]).append(":").append(times[5])
        holder.tvCreateTime.text = "" + builder.toString()

        if (isDesktopNote) {
            holder.tvCreateTime.setBackgroundColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvCreateTime.setBackgroundColor(Color.parseColor("#607D8B"))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //给Holder加载视图被返回该Holder
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.slide_note_info_item, parent, false))
    }

    //还是相当于以前的ViewHolder，只不过这个ViewHolder能直接贯通于该Adapter
    //如直接在onBindViewHolder里执行赋值操作
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvContent: TextView = view.findViewById(R.id.tvContent)
        var tvCreateTime: TextView = view.findViewById(R.id.createTime)
    }


    fun startNoteEdit(position: Int) {
        val intent = Intent()
        intent.setClassName(context.packageName,"com.app.xuzheng.mynote.Activity.NoteDetailActivity")
        intent.putExtra("data", list[position].content)
        intent.putExtra("file_name", list[position].fileName)
//        intent.setClassName(context.packageName, "com.app.xuzheng.mynote.View.TimeSetLayout.ClockActivity")//通过应用的包名加Activity的类名定义的意图开启应用.
//        //主要 application的context是不能启动aty的
        context.startActivity(intent)
    }
}