package com.app.xuzheng.mynote.View

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.app.xuzheng.mynote.Bean.AppInfo
import android.view.LayoutInflater
import android.widget.*
import com.app.xuzheng.mynote.R
import com.app.xuzheng.mynote.Utils.SystemUtil
import java.util.*


/**
 * Created by xuzheng on 2017/8/11.
 */
class ListAdapter(val context: Context, var list: List<AppInfo>) : BaseAdapter(), Filterable {

    var mFilter: MyFilter? = null
    //提供搜索功能1
    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = MyFilter(list)
        }
        return mFilter as MyFilter
    }

    override fun getView(position: Int, view: View?, vg: ViewGroup?): View? {
        var vh: ViewHolder?
        var convertView: View?
        if (view == null) {
            //这一栏是第一次加载，该View还没有视图，更没有绑定组件；
            //以下操作就是给view 设置视图，绑定组件
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.app_list_item, null)
            vh = ViewHolder(convertView)
            convertView.tag = vh
        } else {
            //说明是复用的情况，这是一级缓存
            //直接获取tag，避免复用的视图重新获取View id，这是二级缓存
            convertView = view
            vh = convertView.tag as ViewHolder
        }

        val appInfo = list[position]
        vh.appIcon.setImageDrawable(appInfo.icon)

        var firstLetter = appInfo.appName.trim()[0]
        val firstLetterTemp = SystemUtil.getFirstLetter(firstLetter)
        if (firstLetterTemp != null) {
            firstLetter = firstLetterTemp
        }

        vh.appName.text = firstLetter.toUpperCase() + "-" + appInfo.appName
        vh.appPackageName.text = appInfo.packageName
//        vh.appStart.setOnClickListener {
//            SystemUtil.startApkByPackageName(appInfo.packageName, context, appInfo.appName)
//            vh?.appStart?.isClickable = false
//            //启动一个应用后，两秒后才能在此启动该应用
//            Timer().schedule(object : TimerTask() {
//                override fun run() {
//                    vh?.appStart?.isClickable = true
//                }
//            }, 3000)
//        }
        vh.appListItem.setOnClickListener {
            SystemUtil.startApkByPackageName(appInfo.packageName, context, appInfo.appName)
            vh?.appListItem?.isClickable = false
            //启动一个应用后，0.3秒后才能在此启动该应用
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    vh?.appListItem?.post({
                        vh?.appListItem?.isClickable = true
                    })
                }
            }, 300)

            //0.5s后刷新列表，因为启动aty和保存数据都要时间
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    onUpdateCommonUseApp?.update()
                }
            }, 500)
        }
        return convertView
    }

    override fun getItem(position: Int): Any? = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = list.size

    class ViewHolder(val view: View) {
        val appIcon: ImageView = view.findViewById<ImageView>(R.id.ivAppIcon)
        val appName: TextView = view.findViewById<TextView>(R.id.tvAppName)
        val appPackageName: TextView = view.findViewById<TextView>(R.id.tvAppPackageName)
        val appListItem: LinearLayout = view.findViewById<LinearLayout>(R.id.appListItem)
    }

    interface OnUpdateCommonUseAppListener {
        fun update()
    }

    var onUpdateCommonUseApp: OnUpdateCommonUseAppListener? = null

    //提供搜索功能2
    inner class MyFilter(val filter_list: List<AppInfo>) : Filter() {

        //该方法返回搜索过滤后的数据
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            //创建FilterResults对象
            val results = FilterResults()

            //没有搜索内容，赋值原始数据
            if (TextUtils.isEmpty(constraint)) {
                results.values = filter_list
                results.count = filter_list.size
            } else {
                //创建集合保存过滤后的数据
                val resList = ArrayList<AppInfo>()
                //具体的过滤规则
                filter_list.filterTo(resList) { it.appName.trim().toLowerCase().contains(constraint.toString().trim().toLowerCase()) }
//                for (appInfo in list){
//                    if (appInfo.appName.trim().toLowerCase().contains(constraint.toString().trim().toLowerCase())){
//                        resList.add(appInfo)
//                    }
//                }
                results.values = resList
                results.count = resList.size
            }
            return results
        }

        //该方法用来刷新用户界面，根据过滤后的数据重新展示列表
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            list = results?.values as List<AppInfo>
            notifyDataSetChanged()
        }

    }
}