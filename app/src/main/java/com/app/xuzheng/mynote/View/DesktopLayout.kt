package com.app.xuzheng.mynote.View

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.app.xuzheng.mynote.Activity.HideActivity
import com.app.xuzheng.mynote.Activity.MyNoteApplication
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.desktop_layout.view.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.view.animation.*
import android.widget.Toast
import com.app.xuzheng.mydrawview.DrawerLayout
import com.app.xuzheng.mynote.Activity.CLOSE_POPUP_BROADCAST
import com.app.xuzheng.mynote.Bean.ExpandFunction
import com.app.xuzheng.mynote.RecyclerAdapter
import com.app.xuzheng.mynote.Utils.SystemUtil
import kotlinx.android.synthetic.main.desktop_applist.view.*

/**
 * Created by xuzheng on 2017/7/13.
 * 窗体内的视图
 */
class DesktopLayout : DrawerLayout {

    var onScreenLeft = true
        set(value) {
            field = value
            if (value) onSideListener?.onLeftSide()
        }
    var onScreenTop = true
        set(value) {
            field = value
            if (value) onSideListener?.onTopSide()
        }
    var onScreenRight = false
        set(value) {
            field = value
            if (value) onSideListener?.onRightSide()
        }
    var onScreenBottom = false
        set(value) {
            field = value
            if (value) onSideListener?.onBottomSide()
        }

    /**
     * ？？？
     * 这个context是MainActivity，那么当MainActivity退出时，会造成内存泄露吗？毕竟依赖它context的悬浮窗还在
     * 假如内存泄漏，再次启动时，因为是singleTask，启动的是旧的还是新的？？？我猜测是新的？因为它是finish掉的，不存在在栈里？
     */
    constructor(context: Context) : super(context) {
//        设置自身Orientation
//        super.setOrientation(LinearLayout.VERTICAL)
//        orientation = LinearLayout.VERTICAL

//        设置自身LayoutParams
//        super.setLayoutParams(LayoutParams(LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

//        setBackgroundColor(Color.TRANSPARENT)

        isMotionEventSplittingEnabled = false
        //加载Note界面
        val view = LayoutInflater.from(context).inflate(R.layout.desktop_layout, null)
        addView(view)

        //加载Applist界面
        val view_applist = LayoutInflater.from(context).inflate(R.layout.desktop_applist, null)
        addView(view_applist)

        /**
         * ？？？
         * 成员变量赋值给static
         * 成员变量持有类的引用吗？
         * 如果持有，就会造成全局的内存泄漏
         */


        /**
         * ★2.3 光标的同步
         * 处理点击全局便签时的同步
         * 为什么写在全局便签里？
         * 因为全局便签只需要注册一次监听即可，不需要mHideInput每次启动就去给全局便签做注册，并且关闭时还得remove掉这个注册
         * 并且他是安全 不占用额外资源的
         * 其实2.2已经做了完全同步，但是2.3的同步更加即时迅速安全
         * 2.2只是为了防止滑动光标这类不确定因素导致的不同步，因此即时性相较略差
         */
        etInput.setOnClickListener {
            if (isScroll) {
                isScroll = false
            } else {
                if (!MyNoteApplication.isHideActivityShow) {
                    //启动HideActivity，因为HideActivity的EditText的关系，键盘会自动弹出
                    startApk(context.packageName, context)

                    //设置HideActivity为已打开状态
                    MyNoteApplication.isHideActivityShow = true

                    //启动时HideActivity的et会自己去做同步，不需要我们手动去做同步
                } else {
                    //此时，一定处于输入法弹出的状态，只需要做同步即可
                    HideActivity.etHideInput?.setSelection(etInput.selectionStart)
                }
            }
        }

        initDropView()

        appListTopBar.setOnTouchListener { _, ev ->
            if (ev?.action == MotionEvent.ACTION_DOWN) {
                isOnMoveTouch = true
            }
            false
        }

        etSearch.setOnTouchListener { _, ev ->
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> isOnMoveTouch = true
            /**
             * bug :
             * 当在et上滑动时，事件抢夺，正常，drawerLayout的up会执行，重置为功能切换状态
             * 当在et点击时，事件不抢夺，非正常，drawerLayout无法执行up，所以状态无法重置，出现本意为切功能，结果变为滑动的情况
             */
                MotionEvent.ACTION_UP -> isOnMoveTouch = DrawerLayout.SWITCH_MODE
            }
            false
        }
    }

    var onSideListener: OnSideListener? = null

    interface OnSideListener {
        //前四个回调，只有Flag为true即贴到对应边时，才会执行
        fun onTopSide() {
        }

        fun onLeftSide() {
        }

        fun onRightSide() {
        }

        fun onBottomSide() {
        }

        //执行位置 : action_up，一旦移动完毕，就会执行一次onSide方法
        fun onSide()
    }

    /**
     * ★
     * 启动Aty
     * ！这个方法应该放在Utils里
     * startApk的context不能用application，启动activity只能由aty去启动
     */
    private fun startApk(packname: String, context: Context) {
        /**
         * 通过包名打开某一个app的方法：
         * val packinfo = context.packageManager.getPackageInfo(packname, PackageManager.GET_ACTIVITIES)//获得应用包对象
         * val activityinfos = packinfo.activities//获得所有的Activity对象
         * if (activityinfos != null && activityinfos.size > 0) {
         *    val activityinfo = activityinfos[0]//获得第一个Activity对象
         *    val className = activityinfo.name//获得Activity类名
         * }
         */
        val intent = Intent()
        intent.setClassName(packname, "com.app.xuzheng.mynote.Activity.HideActivity")//通过应用的包名加Activity的类名定义的意图开启应用.
        //主要 application的context是不能启动aty的
        context.startActivity(intent)

        context.sendBroadcast(Intent(CLOSE_POPUP_BROADCAST))
    }

    //1.5启动闹铃界面
    private fun startClock() {
        val intent = Intent()
        intent.setClassName(context.packageName, "com.app.xuzheng.mynote.View.TimeSetLayout.ClockActivity")//通过应用的包名加Activity的类名定义的意图开启应用.
        //主要 application的context是不能启动aty的
        context.startActivity(intent)
    }

    //1.1新增下拉扩展功能
    private val DROP_VIEW_OPEN = 0
    private val DROP_VIEW_CLOSE = 1
    private val DROP_VIEW_ON_THE_WAY = 2

    var dropViewStatus = DROP_VIEW_CLOSE

    //★★★BUG或优化点
    //屏蔽掉滑动时 etInput的click事件 毕竟setOnTouchListener是先响应的
    var isScroll = false

    fun initDropView() {

        dropView.adapter = RecyclerAdapter(context, initExpandFunction())
        dropView.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)

        ivDrop.setOnClickListener {
            openOrHideDropView()
        }
    }

    //供点击事件使用
    fun openOrHideDropView() {
        when (dropViewStatus) {
            DROP_VIEW_OPEN -> hideDropView()
            DROP_VIEW_CLOSE -> openDropView()
        }
    }

    fun openDropView() {
        if (dropViewStatus == DROP_VIEW_ON_THE_WAY || dropViewStatus == DROP_VIEW_OPEN) {
            return
        }
        dropViewStatus = DROP_VIEW_ON_THE_WAY

        dropView.visibility = View.VISIBLE

        val animOut = ObjectAnimator.ofFloat(dropView, "translationX", 400f, 0f)
        animOut.interpolator = DecelerateInterpolator()
        animOut.duration = 300
        animOut.start()
        animOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                dropViewStatus = DROP_VIEW_OPEN
            }
        })

        val rotaAnim = AnimationUtils.loadAnimation(context, R.anim.drop_open_rotation)
        rotaAnim.interpolator = OvershootInterpolator()  //设置匀速旋转，在xml文件中设置会出现卡顿
        rotaAnim.fillAfter = true
        ivDrop.startAnimation(rotaAnim)  //开始动画
    }

    fun hideDropView() {
        if (dropViewStatus == DROP_VIEW_ON_THE_WAY || dropViewStatus == DROP_VIEW_CLOSE) {
            return
        }
        dropViewStatus = DROP_VIEW_ON_THE_WAY

        val animOut = ObjectAnimator.ofFloat(dropView, "translationX", 0f, 400f)
        animOut.interpolator = DecelerateInterpolator()
        animOut.duration = 300
        animOut.start()
        animOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                dropView.visibility = View.GONE
                dropViewStatus = DROP_VIEW_CLOSE
            }
        })

        val rotaAnim = AnimationUtils.loadAnimation(context, R.anim.drop_close_rotation)
        rotaAnim.interpolator = OvershootInterpolator()  //设置匀速旋转，在xml文件中设置会出现卡顿
        rotaAnim.fillAfter = true
        ivDrop.startAnimation(rotaAnim)  //开始动画
    }

    fun hideDropViewImmediately() {
        if (dropViewStatus == DROP_VIEW_ON_THE_WAY || dropViewStatus == DROP_VIEW_CLOSE) {
            return
        }
        dropView.visibility = View.GONE
        dropViewStatus = DROP_VIEW_CLOSE

        val rotaAnim = AnimationUtils.loadAnimation(context, R.anim.drop_close_rotation)
        rotaAnim.interpolator = OvershootInterpolator()  //设置匀速旋转，在xml文件中设置会出现卡顿
        rotaAnim.fillAfter = true
        ivDrop.startAnimation(rotaAnim)  //开始动画
    }

    //扩展功能的具体实现
    fun initExpandFunction(): ArrayList<ExpandFunction> {
        val array = ArrayList<ExpandFunction>()

        //因为新组件的引入，这里的滑动有些问题

        array.add(ExpandFunction("应用", R.drawable.search, object : RecyclerAdapter.OnClickListener {
            override fun onClick() {
                switchToPosition(1)
            }
        }))

        array.add(ExpandFunction("截屏", R.drawable.screen_catch, object : RecyclerAdapter.OnClickListener {
            override fun onClick() {
                getAppScreen()
            }
        }))
        array.add(ExpandFunction("闹钟", R.drawable.alarm_clock, object : RecyclerAdapter.OnClickListener {
            override fun onClick() {
                startClock()
            }
        }))
        return array
    }

    fun getAppScreen() {
        Toast.makeText(context, "当前版本仅支持截取小便签当页笔记内容", Toast.LENGTH_LONG).show()
        hideDropViewImmediately()
        SystemUtil.getScreenImg(this@DesktopLayout, context)
    }
}