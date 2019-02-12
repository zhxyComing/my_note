package com.app.xuzheng.mynote.Fragment

import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mynote.NoteInfoAdapter
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import java.util.*
import android.support.v7.widget.DefaultItemAnimator
import com.app.xuzheng.mynote.Activity.AllClockActivity
import com.app.xuzheng.mynote.Activity.MyNoteApplication
import com.app.xuzheng.mynote.Activity.SLIDE_VIEW_BROADCAST

/**
 * Created by xuzheng on 2017/8/21.
 */
const val NOTE_LIST_REFRESH = "com.app.xuzheng.mynote.Fragment.note_list_refresh"

class NoteMainFragment : Fragment() {

    private var list: RecyclerView? = null
    private var tvOpenClock: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_note, container, false)

        list = view?.findViewById<RecyclerView>(R.id.note_list)
        list?.adapter = NoteInfoAdapter(activity, ArrayList())
        list?.layoutManager = GridLayoutManager(activity, 2)
        list?.itemAnimator = DefaultItemAnimator()
        refresh()

        val createNew = view?.findViewById<TextView>(R.id.createNew)
        createNew?.setOnClickListener {
            val dialog = PromptDialog(activity)
            dialog.setCancelable(true)
            dialog.setDialogType(PromptDialog.DIALOG_TYPE_DEFAULT)
                    .setAnimationEnable(true)
                    .setTitleText("提醒")
                    .setContentText("确定创建新便签？(点击空白区域取消)")
                    .setPositiveListener("确定") {
                        NoteTextUtil.createNewNote(activity)
                        refresh()
                        refreshSlideViewByBroadcast()
                        dialog.dismiss()
                    }.show()
        }

//        val refresh_view = view?.findViewById<SwipeRefreshLayout>(R.id.refresh_view)
//        refresh_view?.setOnRefreshListener {
//            refresh()
//            refresh_view.isRefreshing = false
//        }

        //注册刷新广播
        val intentFilter = IntentFilter(NOTE_LIST_REFRESH)
        activity.registerReceiver(refreshBroadcast, intentFilter)

        tvOpenClock = view?.findViewById<TextView>(R.id.openClockAty)
        setNearClockTime()

        tvOpenClock?.setOnClickListener {
            startActivity(Intent(activity, AllClockActivity::class.java))
        }

        return view
    }

    fun setNearClockTime() {
        Thread(Runnable {
            //数据的获取走子线程
            val nearTime = NoteTextUtil.getNearClockTime(activity)
            activity.runOnUiThread {
                nearTime?.let {
                    tvOpenClock?.visibility = View.VISIBLE
                    tvOpenClock?.text = "最近闹铃提醒时间:$it,点我查看提醒详情"
                } ?: let {
                    tvOpenClock?.visibility = View.GONE
                }
            }
        }).start()
    }

    override fun onStart() {
        super.onStart()
        setNearClockTime()
    }

    fun refresh() {
        (list?.adapter as NoteInfoAdapter).mList.clear()
        val res = NoteTextUtil.getNotesInfoList(activity)
        (list?.adapter as NoteInfoAdapter).mList.addAll(res)
        list?.adapter?.notifyDataSetChanged()

        //针对首次安装应用
        if (res.size == 0) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        refresh()
                    }
                }
            }, 500)
        }
    }

    //添加了广播同步，没必要这种同步了，除非以后新增在note界面直接新增便签
//    override fun onStart() {
//        super.onStart()
//        refresh()
//    }

    val refreshBroadcast: RefreshListBroadcast = RefreshListBroadcast()

    inner class RefreshListBroadcast : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            refresh()
//            (activity as MainActivity).refreshSlideView()
            //使用广播刷新，安全
            refreshSlideViewByBroadcast()
        }
    }

    private fun refreshSlideViewByBroadcast() {
        if (MyNoteApplication.isSlideViewShow) {
            val intent = Intent(SLIDE_VIEW_BROADCAST)
            intent.putExtra("refresh", "refresh")
            activity.sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        activity.unregisterReceiver(refreshBroadcast)
        super.onDestroy()
    }

}