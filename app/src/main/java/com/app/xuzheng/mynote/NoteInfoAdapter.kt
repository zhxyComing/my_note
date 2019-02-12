package com.app.xuzheng.mynote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mynote.Activity.CLOSE_NOTE_BROADCAST
import com.app.xuzheng.mynote.Activity.MyNoteApplication
import com.app.xuzheng.mynote.Activity.NoteDetailActivity
import com.app.xuzheng.mynote.Activity.SLIDE_VIEW_BROADCAST
import com.app.xuzheng.mynote.Bean.NoteInfo
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import com.app.xuzheng.mynote.Utils.toPx
import java.util.*

/**
 * Created by xuzheng on 2017/8/18.
 */
class NoteInfoAdapter(context: Context, list: ArrayList<NoteInfo>) : RecyclerView.Adapter<NoteInfoAdapter.MyViewHolder>() {

    var mContext: Context = context
    var mList: ArrayList<NoteInfo> = ArrayList()

    init {
        mList.addAll(list)
    }

    //赋值操作
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.tvName.text = mList[position].name
//        holder.tvName.setOnClickListener {
//            SystemUtil.startApkByPackageName(mList[position].packageName, mContext, mList[position].name)
//        }
        var isDesktopNote = false
        if (mList[position].fileName.equals(
                SharedpreferenceManager.getString(mContext, SharedpreferenceManager.NOW_NOTE))) {
            isDesktopNote = true
        }
        val text = NoteTextUtil.getNoteByName(mContext, mList[position].fileName)
        holder.tvContent.text = text
        holder.ivSetNow.setOnClickListener {
            //            val dialog = PromptDialog(mContext)
//            dialog.setCancelable(true)
//            dialog.setDialogType(PromptDialog.DIALOG_TYPE_INFO)
//                    .setAnimationEnable(true)
//                    .setTitleText("提醒")
//                    .setContentText("确定设置为当前桌面便签？")
//                    .setPositiveListener("确定") {
            NoteTextUtil.setNowNote(mContext, mList[position].fileName)
            refresh()

            if (MyNoteApplication.isSlideViewShow) {
                val intent = Intent(SLIDE_VIEW_BROADCAST)
                intent.putExtra("refresh", "refresh")
                mContext.sendBroadcast(intent)
            }
//                        dialog.dismiss()
//                    }.show()
        }
        holder.ivRemoveNow.setOnClickListener {
            val dialog = PromptDialog(mContext)
            dialog.setCancelable(true)
            dialog.setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                    .setAnimationEnable(true)
                    .setTitleText("警告")
                    .setContentText("确定删除该便签？")
                    .setPositiveListener("确定") {
                        //如果要删除的正好是当前便签
//                        if (isDesktopNote) {
//                            val dialog = PromptDialog(mContext)
//                            dialog.setCancelable(true)
//                            dialog.setDialogType(PromptDialog.DIALOG_TYPE_INFO)
//                                    .setAnimationEnable(true)
//                                    .setTitleText("提醒")
//                                    .setContentText("当前便签正在作为桌面便签使用，请将桌面便签切换至其它便签后重新尝试删除！")
//                                    .setPositiveListener("确定") {
//                                        //如果要删除的正好是当前便签
//                                        dialog.dismiss()
//                                    }.show()
//                        } else {
                        NoteTextUtil.removeNote(mContext, mList[position].fileName)
//                            refresh()
                        removeData(position)
//                        }
                        //刷新侧边栏
                        if (MyNoteApplication.isSlideViewShow) {
                            val intent = Intent(SLIDE_VIEW_BROADCAST)
                            intent.putExtra("refresh", "refresh")
                            mContext.sendBroadcast(intent)
                        }

                        dialog.dismiss()
                    }.show()
        }

        val time = mList[position].fileName
        val builder = StringBuilder()
        val times = time.split("-")

        builder.append(times[0]).append("/").append(times[1]).append("/").append(times[2]).append(" ").append(times[3]).append(":").append(times[4]).append(":").append(times[5])
        holder.tvCreateTime.text = "创建于:" + builder.toString()

        holder.tvContent.setOnClickListener {
            //关闭桌面Note
            mContext.sendBroadcast(Intent(CLOSE_NOTE_BROADCAST))

            val intent = Intent(mContext, NoteDetailActivity::class.java)
            intent.putExtra("data", mList[position].content)
            intent.putExtra("file_name", mList[position].fileName)
            mContext.startActivity(intent)
        }

        if (isDesktopNote) {
            holder.ivSetNow.visibility = View.GONE
            holder.ivRemoveNow.visibility = View.GONE
            holder.noteFlag.visibility = View.VISIBLE
            holder.cvCard.cardElevation = 2.toPx(mContext).toFloat()
            holder.topLayout.setBackgroundColor(Color.parseColor("#8BC34A"))
        } else {
            holder.ivSetNow.visibility = View.VISIBLE
            holder.ivRemoveNow.visibility = View.VISIBLE
            holder.noteFlag.visibility = View.GONE
            holder.cvCard.cardElevation = 1.toPx(mContext).toFloat()
            holder.topLayout.setBackgroundColor(Color.parseColor("#B0BEC5"))
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    //给Holder加载视图被返回该Holder
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.note_info_item, parent, false))
    }

    //还是相当于以前的ViewHolder，只不过这个ViewHolder能直接贯通于该Adapter
    //如直接在onBindViewHolder里执行赋值操作
    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvContent: TextView = view.findViewById(R.id.tvContent)
        var ivSetNow: ImageView = view.findViewById(R.id.setNow)
        var ivRemoveNow: ImageView = view.findViewById(R.id.removeNow)
        var tvCreateTime: TextView = view.findViewById(R.id.createTime)
        var topLayout: LinearLayout = view.findViewById(R.id.topLayout)
        var noteFlag: TextView = view.findViewById(R.id.note_flag)
        var cvCard: CardView = view.findViewById(R.id.cvCard)
    }

    fun refresh() {
        mList.clear()
        mList.addAll(NoteTextUtil.getNotesInfoList(mContext))
        notifyDataSetChanged()
    }

    fun removeData(position: Int) {
        mList.removeAt(position)
        notifyItemRemoved(position)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                (mContext as Activity).runOnUiThread {
                    //为了保留动画，不得以二次刷新
                    notifyDataSetChanged()
                    //经过测试，上述动画会导致数据越界
                    //因为notifyItemRemoved方法并没有刷新所有的列表，导致别的列表在删除时还是用的旧的position
                }
            }
        }, 500)
    }

    fun addData(position: Int, noteInfo: NoteInfo) {
        mList.add(position, noteInfo)
        notifyItemInserted(position)
    }

}