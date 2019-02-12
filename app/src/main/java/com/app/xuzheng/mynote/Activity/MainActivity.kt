package com.app.xuzheng.mynote.Activity

import android.animation.*
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.desktop_layout.view.*
import com.app.xuzheng.mynote.*
import com.app.xuzheng.mynote.Service.NoteService
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import com.app.xuzheng.mynote.Utils.SystemUtil
import com.app.xuzheng.mynote.Utils.toPx
import kotlinx.android.synthetic.main.copy_or_paste.view.*
import java.util.*
import android.animation.ObjectAnimator
import android.content.*
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mydrawview.DrawerLayout
import com.app.xuzheng.mynote.Bean.AppInfoSmall
import com.app.xuzheng.mynote.Fragment.NOTE_LIST_REFRESH
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import com.app.xuzheng.mynote.View.*
import com.app.xuzheng.mynote.View.ListAdapter
import com.app.xuzheng.myslidelayout.MySlideLayout
import com.app.xuzheng.myslidelayout.TIP_HEIGHT
import com.app.xuzheng.myslidelayout.TIP_WIDTH
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.action_tool_bar.*
import kotlinx.android.synthetic.main.popup_view.view.*
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.desktop_applist.view.*


//适配用到的厂商名字列表  因为谷歌等原生手机会崩溃  崩溃原因：TYPE_TOAST 以及 没悬浮窗权限直接崩
//vivo虽然用的权限与以下相同，但是由于它的权限检测无效，所以需要另行适配
//vivo 比较特殊，既用的fit厂商的权限，同时又对所以机型适用的权限检测无效，同时又不会因为没权限而崩溃，同时应用内显示应用外消失

//vivo手机还存在界面变化后不刷新的情况...需要手动打开app才会恢复正常...待修复
val arrayFit = listOf("Android", "Google", "Moto", "xiaomi")

const val CLOSE_NOTE_BROADCAST = "com.app.xuzheng.mynote.Activity.close_note"
const val CLOSE_POPUP_BROADCAST = "com.app.xuzheng.mynote.Activity.close_popup"
const val SLIDE_VIEW_BROADCAST = "com.app.xuzheng.mynote.Activity.slide_view"

class MainActivity : FragmentActivity() {

    //当前手机的厂商名字
    var mBrand = ""

    var mWindowManager: WindowManager? = null
    var mLayout: WindowManager.LayoutParams? = null
    var mDesktopLayout: DesktopLayout? = null
    var mStatusBarHeight: Int = 0
    var mScreenWidth: Int = 0
    var mScreenHeight: Int = 0

    var isSmall = false

    //桌面便签的宽高值 单位px
    var mNoteWidth = 0
    var mNoteHeight = 0

//    private var themeManager: ThemeManager? = null

    //桌面便签是否已经显示
    var isNoteShow = false

    var mPopupWindow: PopupWindow? = null

    var mFrequentlyAppLayout: WindowManager.LayoutParams? = null

    //侧边隐藏的All note
    var quickNoteRecycleView: RecyclerView? = null

    var slideView: MySlideLayout? = null

    /**
     * 因为按退出键不执行onDestroy方法，推测是桌面便签引用了MainContext，内存泄漏，导致回收不了（360手机不执行onDestroy，小米执行了！）
     * 但是事实上，小便签确实需要MainContext的变量，MainContext不能被关闭
     * 所以干脆设计成，在启动时，生成且只生成一个MainContext，也就是说除进程杀死外的所有操作，都是小化程序
     * MainContext可以处于后台隐藏，但他是桌面便签的必要存在，所以不会被关闭，且只维护一个，节省内存消耗
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //app主题要好好规划一下
//        setTheme(R.style.ThemeOrange)
        setContentView(R.layout.app_bar_main)

//        setSupportActionBar(toolbar)
        initToolBar()

        //★华为也能用，不会影响到虚拟键的沉浸式状态栏
//        NoteTextUtil.setImmBarNonOffset(this)

        mBrand = SystemUtil.deviceBrand
        Log.e("DeviceBrand ", mBrand)

        initParams()
        initDesktopParams()

//        themeManager = ThemeManager()
//        themeManager?.openThemeLayout()

        //初始化应用快捷搜索
        initChange()

        //！！！sb fragment 必须设置个默认的fragment，而且该fragment还特么作为前背景一直存在，删都删不了
        //完了单独做项目测fragment切换，本项目不加了！

        initCloseNoteBroadcast()
        initClosePopupBroadcast()
        initSlideViewBroadcast()

        initDesktopSwitch()
    }

    fun initToolBar() {
        val popupView = layoutInflater.inflate(R.layout.popup_view, null)

        mPopupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        mPopupWindow?.isTouchable = true
        mPopupWindow?.isOutsideTouchable = true

        tvDrop.setOnClickListener { view -> mPopupWindow?.showAsDropDown(view) }

        popupView.tvPassSave.setOnClickListener {
            startActivity(Intent(this, SecurityActivity::class.java))
            mPopupWindow?.dismiss()
        }

        popupView.tvMoreSetting.setOnClickListener {
            startActivity(Intent(this, MoreSetActivity::class.java))
            mPopupWindow?.dismiss()
        }

        popupView.tvOpenLimit.setOnClickListener {
            openSetting()
            mPopupWindow?.dismiss()
        }
    }

    fun initParams() {
        mNoteWidth = 140.toPx(this)
        mNoteHeight = 140.toPx(this)
    }

    fun initDesktopParams() {
        createWindowManager()
        createDesktopLayout()
    }

    override fun onStart() {
        super.onStart()
        //每次打开，检测小便签是否被关闭，关闭则快速重启
        if (!isNoteShow && SharedpreferenceManager.getBoolean(this, SharedpreferenceManager.IS_HIDE_DESKTOP, true)) {
            showDesktopLayout()
            startService(Intent(this@MainActivity, NoteService::class.java))
        }
        //每次打开，检测是否提醒权限申请
//        openTip()
    }

    fun openTip() {
        //弹窗提醒要权限
        //首先要知道6.0版本权限模型跟原来版本是不同的，不再是统一在manifest中默认系统授权，而是有需要的时候，向系统请求授权，提高用户体验。
        //而深度rom，如小米，干脆不提示，直接不给权限
        //改权限申请要延后处理，否则影响一些设置
        val task = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if ("vivo".equals(SystemUtil.deviceBrand) && !SharedpreferenceManager.getBoolean(this@MainActivity, SharedpreferenceManager.IS_SHOW_LIMIT_TIP)) {
                        showTipDialogForVivo()
                    }
                    //小米和魅族的适配
                    //先判断小米或者魅族手机权限是否已开启或者跳过开启
                    if (("xiaomi".equals(SystemUtil.deviceBrand.toLowerCase()) || "meizu".equals(SystemUtil.deviceBrand.toLowerCase())) &&
                            !SystemUtil.isLimitEnabled(this@MainActivity, SystemUtil.OP_SYSTEM_ALERT_WINDOW) &&
                            !SharedpreferenceManager.getBoolean(this@MainActivity, SharedpreferenceManager.IS_SHOW_LIMIT_TIP)) {
                        showTipDialog()
                    }
                }
            }
        }
        Timer().schedule(task, 500)
    }

    //打开 权限设置页面 （这里直接打开悬浮窗权限申请页面！）
    fun openSetting() {
        //针对魅族点了跳转悬浮窗权限没反应做的适配
        if ("Meizu".equals(SystemUtil.deviceBrand)) {
            val intent1 = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent1.data = uri
            startActivityForResult(intent1, 11)
            return
        }
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName))
            startActivityForResult(intent, 0)
        } catch (localActivityNotFoundException: ActivityNotFoundException) {
            val intent1 = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent1.data = uri
            startActivityForResult(intent1, 11)
        }
    }

    fun showTipDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        val normalDialog =
                AlertDialog.Builder(this)
        normalDialog.setIcon(R.drawable.warn)
        normalDialog.setTitle("权限申请")
        normalDialog.setCancelable(false)
        normalDialog.setMessage("小便签检测到您的悬浮窗权限未开启，这将影响小便签的正常使用，是否快速开启权限？")
        normalDialog.setPositiveButton("快速开启"
        ) { _, _ ->
            openSetting()
        }
        normalDialog.setNegativeButton("下次再说",
                { _, _ ->
                }
        )
        normalDialog.setNeutralButton("显示正常，无需开启") { _, _ ->
            SharedpreferenceManager.setBoolean(this@MainActivity, SharedpreferenceManager.IS_SHOW_LIMIT_TIP, true)
        }
        // 显示
        normalDialog.show()
    }

    //vivo权限打不开，只能通过i管家去打开
    fun openSettingForVivo() {
        SystemUtil.startApkByPackageName("com.iqoo.secure", this, "i 管家")
    }

    //控制vivo提示窗只能显示一个
    private var isVivoTipShow = false

    fun showTipDialogForVivo() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        val normalDialog =
                AlertDialog.Builder(this)
        normalDialog.setIcon(R.drawable.warn)
        normalDialog.setTitle("权限申请")
        normalDialog.setCancelable(false)
        normalDialog.setMessage("vivo手机在悬浮窗权限未开启的情况下，会存在应用外桌签无法显示的异常情况，需要您手动去i管家开启权限")
        normalDialog.setPositiveButton("立即前往"
        ) { _, _ ->
            openSettingForVivo()
            isVivoTipShow = false
        }
        normalDialog.setNegativeButton("下次提醒",
                { _, _ ->
                    isVivoTipShow = false
                }
        )
        normalDialog.setNeutralButton("我已开启") { _, _ ->
            SharedpreferenceManager.setBoolean(this@MainActivity, SharedpreferenceManager.IS_SHOW_LIMIT_TIP, true)
            isVivoTipShow = false
        }
        // 显示
        if (!isVivoTipShow) {
            normalDialog.show()
            isVivoTipShow = true
        }
    }

    /**
     * 点击退出键不能finish了，不会执行onDestroy，不清楚原因
     * 即时在onKeyDown里监听然后finish也不会执行onDestroy，只能写在这里
     *
     * 写在这里表明，不能切换到新的页面，一旦切换本页面就会被finish（）
     */
    override fun onPause() {
        super.onPause()
        //应该写在base类里，这里临时写在这里
        MobclickAgent.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onDestroy() {
        unregisterReceiver(closeNoteBroadcast)
        unregisterReceiver(closePopupBroadcast)
        unregisterReceiver(slideViewBroadcast)
        super.onDestroy()
    }

    fun createWindowManager() {
        //1 取得系统窗体
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        //2 设置窗体的布局样式
        mLayout = WindowManager.LayoutParams()

        //设置窗体的显示类型
        //★使用TYPE_TOAST，360,华为等手机不需要权限就能弹出悬浮窗；小米旧版不需要，新版需要
        /**
         * ★ 疑难报错  并非aty没启动
         * Unable to add window -- token null is not valid; is your activity running?
         * app在谷歌手机上崩溃的原因
         * 1.不能用TYPE_TOAST
         * 2.没有开权限直接崩（原生就是6）
         */
//        if (NoteTextUtil.isInFit(mBrand)) {
//            mLayout?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//        } else if ("vivo".equals(SystemUtil.deviceBrand)) { //vivo需要这个权限
//            mLayout?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//        } else {
//            mLayout?.type = WindowManager.LayoutParams.TYPE_TOAST
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayout?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mLayout?.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        //设置窗体焦点及触摸
        mLayout?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        //设置显示的模式
        mLayout?.format = PixelFormat.RGBA_8888

        //设置对齐的边
        //★注意：这里是对齐不是位置，设置left和top，会让视图位移时以屏幕左上角为00点
        mLayout?.gravity = Gravity.LEFT + Gravity.TOP

        //设置窗体的宽度和高度 单位px
        mLayout?.width = mNoteWidth
        mLayout?.height = mNoteHeight

        mLayout?.x = 105.toPx(this)
        mLayout?.y = 200.toPx(this)
    }

    fun createSlideLayout() {
//-------------------------------------------------------------------新功能测试代码
        if (SharedpreferenceManager.getBoolean(this, SharedpreferenceManager.IS_SHOW_SLIDE_VIEW)) {

            slideView = MySlideLayout(this)

            //可以测试下updateLayout的移动方式来实现下面效果！！！
            //经过测试，updateLayout的方法无法移动到视图外，最多只能贴边(即使是负数)，原因尚不清除，周末需要用layout代码类比updateLayout去测试！！！

            //隐藏时，缩小宽度，以防影响屏幕透明区域的正常点击
            slideView?.setOnHideListener(object : MySlideLayout.OnHideListener {
                override fun onHide() {
                    mFrequentlyAppLayout?.width = TIP_WIDTH
//                mFrequentlyAppLayout?.height = TIP_HEIGHT + TIP_POSITION + 5
                    mWindowManager?.updateViewLayout(slideView, mFrequentlyAppLayout)
                }
            })

            //Tip点击时，重新定义宽度
            //这里的定制，导致只能添加一个Tip，仅供桌面使用
            slideView?.setOnTipTouchListener(object : MySlideLayout.OnTipTouchListener {
                private var lastX = 0f
                private var lastY = 0f
                override fun onTipTouch(ev: MotionEvent) {
                    if (ev.action == MotionEvent.ACTION_DOWN) {
                        mFrequentlyAppLayout?.width = 100.toPx(this@MainActivity)
//                    mFrequentlyAppLayout?.height = 200.toPx(this@MainActivity)
                        mWindowManager?.updateViewLayout(slideView, mFrequentlyAppLayout)
                    }
                    //防止只是一次点击，没有滑动
                    if (ev.action == MotionEvent.ACTION_UP) {
                        mFrequentlyAppLayout?.width = TIP_WIDTH
//                    mFrequentlyAppLayout?.height = TIP_HEIGHT + TIP_POSITION + 5
                        mWindowManager?.updateViewLayout(slideView, mFrequentlyAppLayout)
                    }

                    //目前暂时不让其移动
//                when (ev.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        lastX = ev.rawX
//                        lastY = ev.rawY
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val disX = ev.rawX - lastX
//                        val disY = ev.rawY - lastY
//
//                        if (Math.abs(disY) > Math.abs(disX)
//                                && Math.abs(disY) > 10) {
//                            mFrequentlyAppLayout?.y = mFrequentlyAppLayout?.y?.plus(disY.toInt())
//                            mWindowManager?.updateViewLayout(slideView, mFrequentlyAppLayout)
//
//
//                        }
//
//                        lastX = ev.rawX
//                        lastY = ev.rawY
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        lastX = 0f
//                        lastY = 0f
//                    }
//                }
                }
            })

//        val tv_1 = TextView(this)
//        tv_1.gravity = Gravity.CENTER
//        tv_1.textSize = 12f
//        tv_1.text = "自定义视图"

//        val card_1 = CardView(this)
//        card_1.cardElevation = 4f
//        card_1.radius = 2f

            val quickNoteView = LayoutInflater.from(this@MainActivity).inflate(R.layout.slide_desktop_view, null)
            quickNoteRecycleView = quickNoteView.findViewById<RecyclerView>(R.id.rvSlide)
            quickNoteRecycleView?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
            quickNoteRecycleView?.adapter = SlideNoteInfoAdapter(this, NoteTextUtil.getNotesInfoList(this), slideView)

//        card_1.addView(quickNoteView)

            slideView?.addSlideView(quickNoteView)

            mFrequentlyAppLayout = WindowManager.LayoutParams()

            //设置窗体的显示类型
            //★使用TYPE_TOAST，360,华为等手机不需要权限就能弹出悬浮窗；小米旧版不需要，新版需要
            /**
             * ★ 疑难报错  并非aty没启动
             * Unable to add window -- token null is not valid; is your activity running?
             * app在谷歌手机上崩溃的原因
             * 1.不能用TYPE_TOAST
             * 2.没有开权限直接崩（原生就是6）
             */
//            if (NoteTextUtil.isInFit(mBrand)) {
//                mFrequentlyAppLayout?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//            } else if ("vivo".equals(SystemUtil.deviceBrand)) { //vivo需要这个权限
//                mFrequentlyAppLayout?.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//            } else {
//                mFrequentlyAppLayout?.type = WindowManager.LayoutParams.TYPE_TOAST
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mFrequentlyAppLayout?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mFrequentlyAppLayout?.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            //设置窗体焦点及触摸
            mFrequentlyAppLayout?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

            //设置显示的模式
            mFrequentlyAppLayout?.format = PixelFormat.RGBA_8888

            //设置对齐的边
            //★注意：这里是对齐不是位置，设置left和top，会让视图位移时以屏幕左上角为00点
            mFrequentlyAppLayout?.gravity = Gravity.LEFT + Gravity.TOP

            //设置窗体的宽度和高度 单位px
//        mFrequentlyAppLayout?.width = 100.toPx(this)
            mFrequentlyAppLayout?.width = TIP_WIDTH
            mFrequentlyAppLayout?.height = TIP_HEIGHT

            mFrequentlyAppLayout?.x = 0.toPx(this)
            mFrequentlyAppLayout?.y = 100.toPx(this)

            mWindowManager?.addView(slideView, mFrequentlyAppLayout)

            MyNoteApplication.isSlideViewShow = true
        }

        //----------------------------------------------------------------
    }

    fun closeSlideView() {
        mWindowManager?.removeView(slideView)
        mFrequentlyAppLayout = null
        quickNoteRecycleView = null
        slideView = null
        MyNoteApplication.isSlideViewShow = false
    }

    fun refreshSlideView() {
        if (SharedpreferenceManager.getBoolean(this, SharedpreferenceManager.IS_SHOW_SLIDE_VIEW) && MyNoteApplication.isSlideViewShow) {
            (quickNoteRecycleView?.adapter as SlideNoteInfoAdapter).list = NoteTextUtil.getNotesInfoList(this)
            quickNoteRecycleView?.adapter?.notifyDataSetChanged()
        }
    }

    var slideViewBroadcast: SlideViewBroadcast = SlideViewBroadcast()

    fun initSlideViewBroadcast() {
        slideViewBroadcast = SlideViewBroadcast()
        val intentFilter = IntentFilter(SLIDE_VIEW_BROADCAST)
        registerReceiver(slideViewBroadcast, intentFilter)
    }

    inner class SlideViewBroadcast : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent) {
            val refresh = intent.getStringExtra("refresh")
            refresh?.let {
                if (refresh == "refresh") {
                    refreshSlideView()
                }
            } ?: let {
                val bool = intent.getBooleanExtra("isChecked", false)
                if (bool) {
                    createSlideLayout()
                } else {
                    closeSlideView()
                }
            }
        }
    }

    /**
     * ★
     * 贴边时/移动到某一位置时 的监听
     */
    fun createDesktopLayout() {

        createSlideLayout()

        //改造点：这里用哪个context？区别是什么？
        //只有Activity的context才能弹出输入法
        mDesktopLayout = DesktopLayout(this)

        mDesktopLayout?.etInput?.isCursorVisible = false

        mDesktopLayout?.onSideListener = object : DesktopLayout.OnSideListener {

            override fun onSide() {
                /**
                 * bug : 这里的判断出错了
                 * 不能只判断左贴边！！！上下左右都应该考虑进去
                 * 否则在右贴边执行完，onSide又给置回去了
                 */
                if (mDesktopLayout?.onScreenLeft == false &&
                        mDesktopLayout?.onScreenRight == false &&
                        mDesktopLayout?.onScreenTop == false &&
                        mDesktopLayout?.onScreenBottom == false) {
                    if (isSmall) {
                        isSmall = false
                        showAnim()
                    }
                }
            }

            //某边贴边时执行
            override fun onLeftSide() {
                super.onLeftSide()
                if (!isSmall) {
                    isSmall = true
                    smallAnimLeft()
                }
            }

            override fun onRightSide() {
                super.onRightSide()
                if (!isSmall) {
                    isSmall = true
                    smallAnimRight()
                }
            }

            //顶栏暂时不设置隐藏
            override fun onTopSide() {
                super.onTopSide()
            }

            override fun onBottomSide() {
                super.onBottomSide()
                if (!isSmall) {
                    isSmall = true
                    smallAnimBottom()
                }
            }
        }

        /**
         * ★
         * 让便签可以响应滑动事件
         * 注意滑动事件的实现方式，别的实现方式能实现滑动，但是在贴边监听上可能会出问题
         */
        mDesktopLayout?.setOnMoveTouchEvent(object : DrawerLayout.OnMoveTouchEventListener {
            var downX: Float = 0f
            var downY: Float = 0f
            //★注意 这两个值 因为需要measuredWidth 而这个值在aty开始时是获取不到的，onWindowsFocusChanged才能布局完毕才能获取到
            //所以干脆改到了点击时获取
            var desktopWidth = 0
            var desktopHeight = 0
            override fun onMoveTouch(event: MotionEvent?) {

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        desktopWidth = mDesktopLayout?.measuredWidth ?: 0
                        desktopHeight = mDesktopLayout?.measuredHeight ?: 0
                        downX = event.x
                        downY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (downX == 0f && downY == 0f) {
                            desktopWidth = mDesktopLayout?.measuredWidth ?: 0
                            desktopHeight = mDesktopLayout?.measuredHeight ?: 0
                            downX = event.x
                            downY = event.y
                            return
                        }

                        //如果不减去downX，意为视图左上角位移到手指按压位置
                        mLayout?.x = NoteTextUtil.clamp((event.rawX - downX).toInt(), 0, mScreenWidth - desktopWidth)
                        //LayoutParams.y是不包括通知栏的，RawY包括
                        mLayout?.y = NoteTextUtil.clamp((event.rawY - downY).toInt() - mStatusBarHeight, 0, mScreenHeight - desktopHeight)
                        mWindowManager?.updateViewLayout(mDesktopLayout, mLayout)
                    }
                    MotionEvent.ACTION_UP -> {

                        //左右上下贴边回调依次执行，然后执行onSide()
                        mDesktopLayout?.onScreenLeft = mLayout?.x == 0
                        mDesktopLayout?.onScreenRight = mLayout?.x == mScreenWidth - desktopWidth
                        mDesktopLayout?.onScreenTop = mLayout?.y == 0
                        mDesktopLayout?.onScreenBottom = mLayout?.y == mScreenHeight - desktopHeight

                        mDesktopLayout?.onSideListener?.onSide()

                        downX = 0f
                        downY = 0f

                        //不是小化模式，抬起时才能重置状态
                        if (!MyNoteApplication.isDeaktopSmall) {
                            mDesktopLayout?.isOnMoveTouch = DrawerLayout.SWITCH_MODE
                        }
                    }
                }
            }
        })

        //指定点击该位置时，mDesktop更换为滑动组件而不是组件切换
        //这个滑动状态在自己实现的onMoveTouchListener的UP方法里被重置了，也就是说只支持一次按压周期
        mDesktopLayout?.touchMove_1?.setOnTouchListener { _, ev ->
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDesktopLayout?.isOnMoveTouch = DrawerLayout.MOVE_MODE
                }
            }
            false
        }

        //！！！这部分需要做专门优化
        mDesktopLayout?.tvClose?.setOnClickListener {
            onCloseClick()
        }

        mDesktopLayout?.etInput?.setOnLongClickListener {
            if (!MyNoteApplication.isCopyOrPasteLayoutShow) {
                createCopyOrPasteLayout()
                MyNoteApplication.isCopyOrPasteLayoutShow = true
            }
            true
        }
    }

    fun onCloseClick() {
        //先保存数据it  不为空，才能去保存
        if (isNoteShow) {
            MyNoteApplication.etNote?.let {
                NoteTextUtil.saveNote(this, it.text.toString())
                sendBroadcast(Intent(NOTE_LIST_REFRESH))
            }
            isNoteShow = false
            //只能remove，不能把desktop置为null，因为还会start重启
            mWindowManager?.removeView(mDesktopLayout)
            MyNoteApplication.etNote = null
        }
    }

    var closeNoteBroadcast: CloseNoteBroadcast? = null

    inner class CloseNoteBroadcast : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            onCloseClick()
        }
    }

    //解决切换功能页时输入法不隐藏 bug
    var closePopupBroadcast: ClosePopupBroadcast? = null

    inner class ClosePopupBroadcast : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            mPopupWindow?.dismiss()
        }
    }

    fun initCloseNoteBroadcast() {
        closeNoteBroadcast = CloseNoteBroadcast()
        val intentFilter = IntentFilter(CLOSE_NOTE_BROADCAST)
        registerReceiver(closeNoteBroadcast, intentFilter)
    }

    fun initClosePopupBroadcast() {
        closePopupBroadcast = ClosePopupBroadcast()
        val intentFilter = IntentFilter(CLOSE_POPUP_BROADCAST)
        registerReceiver(closePopupBroadcast, intentFilter)
    }

    //获得屏幕宽高
    fun initDisplayParams() {
        val r = Rect()
        /**
         * ★
         * 这个方法，当启动页非全屏时，从启动页进入首页，获得到的状态栏高度是正确的，是72
         * 但是当启动页非全屏时，从启动页进入首页，在onWindowFocusChanged方法里，第一时间获取到的状态栏高度是0！
         * 原因分析：
         * 不论全屏与非全屏的切换，activity切换后的布局，都是在用一个aty中进行的
         * 所以推测：xml Theme 全屏向非全屏转换的速度 < activity setContentView 和 onWindowFocus的速度
         * 或者说：☆★非全屏的转换是在setContentView之后，是在onWindowFocusChanged之后执行的！
         *
         * 另外，之所以之前退出后能正确滑动，是因为onWindowFocusChanged在程序退出时还会执行一次！所以状态栏高度得以正确修改！
         * 所以不能使用timerTask延迟的方式，因为延迟，页面都退出了，statusHeight获取不到，被重置为了0！
         */
        window.decorView.getWindowVisibleDisplayFrame(r)
        //只有初始化时才赋值，避免了因输入法弹出造成的高度改变（输入法小化时并不执行onContentChanged）
        //但是可以联想到，可以用这种方式监听输入法弹出
        if (mScreenHeight == 0 || mScreenWidth == 0) {
            mScreenHeight = r.bottom
            mScreenWidth = r.right
        }
    }

    override fun onContentChanged() {
        super.onContentChanged()
        mStatusBarHeight = NoteTextUtil.getStatusHeight(this@MainActivity)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //输入法的弹出会影响布局高度！！！
        initDisplayParams()
    }

    /**
     * ★
     * show时，才能给全局便签赋值，初始化是不能赋值的；
     * 并且，DesktopLayout里也不应该出现MyNoteApplication.etNote的代码
     * 初始化的流程不应该影响到全局便签变量，没有赋值前它和全局便签没有任何关系
     * ★
     * isNoteShow要用application的变量去记录，以Aty记录在程序退出时会重置
     * application只有在进程重启时才会重置
     */
    fun showDesktopLayout() {
//        if (NoteTextUtil.isInFit(mBrand)) {//如果符合适配要求 谷歌等原生手机没权限打开dialog直接崩溃
        //如果没权限 就弹窗提示，否则退出
        //不能替换原先的权限申请弹窗提示，因为原先的存在没权限也能显示正常的情况如小米4，而360华为等不在此判断范围
        if (!SystemUtil.isLimitEnabled(this@MainActivity, SystemUtil.OP_SYSTEM_ALERT_WINDOW)) {
            val dialog = PromptDialog(this@MainActivity)
            dialog.setCancelable(false)
            dialog.setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                    .setAnimationEnable(true)
                    .setTitleText("提醒")
                    .setContentText("小便签监测到您没有开启悬浮窗权限，这将影响到程序的正常运行，点击确定前往开启")
                    .setPositiveListener("确定") {
                        openSetting()
                        dialog.dismiss()
                    }.show()
        } else {
            showDesktopNote()
        }
//        } else {
//            showDesktopNote()
//        }
    }

    private fun showDesktopNote() {
        mDesktopLayout?.etInput?.setText(NoteTextUtil.getNote(this@MainActivity))
        MyNoteApplication.etNote = mDesktopLayout?.etInput
        mWindowManager?.addView(mDesktopLayout, mLayout)
        isNoteShow = true
//        themeManager?.setDesktopLayoutTheme(SharedpreferenceManager.getInt(this, SharedpreferenceManager.NOW_THEME))
    }

    /**
     * 左隐藏便签时的动画
     */
    fun smallAnimLeft() {
        smallAnim("translationX", -500f, LEFT)
    }

    /**
     * 右隐藏便签时的动画
     * 右隐藏和下隐藏，都要相应的移动2/3的位置
     */
    fun smallAnimRight() {
        smallAnim("translationX", 500f, RIGHT)
    }

    /**
     * 上隐藏便签时的动画
     */
    fun smallAnimTop() {
        smallAnim("translationY", -1000f, TOP)
    }

    /**
     * 下隐藏便签时的动画
     */
    fun smallAnimBottom() {
        smallAnim("translationY", 1000f, BOTTOM)
    }

    fun smallAnim(orientation: String, dis: Float, dir: String) {
        mDesktopLayout?.hideDropView()

        val animOut = ObjectAnimator.ofFloat(mDesktopLayout, orientation, dis)
        animOut.interpolator = DecelerateInterpolator()
        animOut.duration = 400
        animOut.start()
        animOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                setHideAttribute(dir)

                val animIn = ObjectAnimator.ofFloat(mDesktopLayout, orientation, 0f)
                animIn.interpolator = DecelerateInterpolator()
                animIn.duration = 300
                animIn.start()
            }
        })
    }

    //便签从显示到隐藏
    private val LEFT = "left"
    private val TOP = "top"
    private val RIGHT = "right"
    private val BOTTOM = "bottom"
    fun setHideAttribute(dir: String) {
        /**
         * 优化如下：
         * 隐藏界面下：无论note还是appList均要隐藏 ；而小化后显示的界面才要根据具体情况而定
         */
        //通用 隐藏或小化
        mLayout?.width = mLayout?.width!! / 3
        mLayout?.height = mLayout?.height!! / 3
        when (dir) {
        /**
         * 根据方向做适配
         * ★？？但是奇怪的是：这样做会导致timeView两次重绘，不清楚原因
         * 可能这样做相当于两次调用updateView
         */
            RIGHT -> {
                mLayout?.x = mLayout?.x!! + mLayout?.width!! * 2
            }
            BOTTOM -> {
                mLayout?.y = mLayout?.y!! + mLayout?.height!! * 2
            }
        }
        mWindowManager?.updateViewLayout(mDesktopLayout, mLayout)

        //设置alpha
        mDesktopLayout?.alpha = 0.6f

        //隐藏掉appList相关界面
        mDesktopLayout?.appListContent?.visibility = View.GONE


        //隐藏掉note相关界面
        mDesktopLayout?.cvCard?.visibility = View.GONE

        setHideDisplay()

        //新增 隐藏模式下 设置为滑动模式
        MyNoteApplication.isDeaktopSmall = true
        mDesktopLayout?.isOnMoveTouch = DrawerLayout.MOVE_MODE
    }

    //显示隐藏表盘还是显示快捷切换
    fun setHideDisplay() {
        if (isAppListShow) {
            //新增appList状态下的visible
            mDesktopLayout?.appListSmallContent?.visibility = View.VISIBLE

            //刷新列表
            val array = SystemUtil.getAppOpenMessage(this@MainActivity)
            array?.let {
                appListSmall.clear()
                appListSmall.addAll(array.asReversed())
                appListSmallAdapter?.notifyDataSetChanged()
            } ?: let {
                appListSmall.clear()
                appListSmall.add(AppInfoSmall("应用闪切", "", 0))
                appListSmall.add(AppInfoSmall("尚未使用", "", 0))
                appListSmallAdapter?.notifyDataSetChanged()
            }
        } else {
            //1.2版本 新增隐藏栏时间表盘显示
            //隐藏与文本编辑页面所有相关的东西，显示时间
            mDesktopLayout?.timeView?.visibility = View.VISIBLE
            mDesktopLayout?.timeView?.startTime()
        }
        /**
         * 1.重新布局，不会scroll回去...shit
         * 2.即使cv与timeView都设置隐藏，但是父组件还在，所以依然占位！所以要偏移 measure * position
         * 3.尺寸measureWidth有延迟，即使先缩小了尺寸。所以需要同比例除/乘
         */
        val position: Int = if (mDesktopLayout == null) 0 else mDesktopLayout!!.getNowPosition()
        mDesktopLayout?.scrollTo(mDesktopLayout?.measuredWidth!! / 3 * position, 0)
    }

    /**
     * 便签从隐藏到显示
     */
    fun showAnim() {
        /**
         * 优化如下：
         * 显示界面下：无论表盘还是appSmallList均要隐藏 ；而正常显示的界面才要根据具体情况而定
         */
        //        mDesktopLayout?.ivDrop?.visibility = View.VISIBLE

        //设置通用属性
        mLayout?.width = mLayout?.width!! * 3
        mLayout?.height = mLayout?.height!! * 3
//        mDesktopLayout?.etInput?.visibility = View.VISIBLE
        mWindowManager?.updateViewLayout(mDesktopLayout, mLayout)
        //设置alpha
        mDesktopLayout?.alpha = 1f

        //隐藏app快切
        mDesktopLayout?.appListSmallContent?.visibility = View.GONE

        //隐藏表盘
        mDesktopLayout?.timeView?.stopTime()
        mDesktopLayout?.timeView?.visibility = View.GONE

//        if (isAppListShow) {
        //新增appList状态下的visible
        mDesktopLayout?.appListContent?.visibility = View.VISIBLE
//        } else {
        //1.2版本 新增隐藏栏时间表盘显示
        //隐藏与文本编辑页面所有相关的东西，显示时间
        mDesktopLayout?.cvCard?.visibility = View.VISIBLE
//        }

        //新增显示状态下，回归组件切换模式
        MyNoteApplication.isDeaktopSmall = false
        mDesktopLayout?.isOnMoveTouch = DrawerLayout.SWITCH_MODE

        val position: Int = if (mDesktopLayout == null) 0 else mDesktopLayout!!.getNowPosition()
        mDesktopLayout?.scrollTo(mDesktopLayout?.measuredWidth!! * 3 * position, 0)

        if (!isAppListShow) {
            mDesktopLayout?.etInput?.isFocusable = true
            mDesktopLayout?.etInput?.requestFocus()
        }
    }

    //★点击Home键，亲测KeyCode是没用的，需要调用onUserLeaveHint方法
//为了解决按Home键回到桌面或切换到其它应用时，点击Note弹回到本应用主界面的bug
    override fun onUserLeaveHint() {
//        finish()
//        Toast.makeText(this, "小便签后台运行中", Toast.LENGTH_SHORT).show()
        super.onUserLeaveHint()
    }

    private fun createCopyOrPasteLayout() {
        val copyOrPasteLayout = CopyOrPasteLayout(this)

        //创建定时器，3s钟后自动消失
        val task = object : TimerTask() {
            override fun run() {
                removeCopyOrPasteLayout(copyOrPasteLayout)
            }
        }
        val timer = Timer()
        timer.schedule(task, 1500)

        copyOrPasteLayout.copy.setOnClickListener {
            //记得即时取消掉Timer计时器
            timer.cancel()
            //移除复制粘贴视图
            removeCopyOrPasteLayout(copyOrPasteLayout)
            //Toast提示
            Toast.makeText(this, "文字已复制", Toast.LENGTH_SHORT).show()
            //复制所有文字到粘贴版
            val str: String = mDesktopLayout?.etInput?.text.toString()
            NoteTextUtil.copyFromEditText(str, this)
        }

        copyOrPasteLayout.paste.setOnClickListener {
            timer.cancel()

            removeCopyOrPasteLayout(copyOrPasteLayout)

            Toast.makeText(this, "文字已粘贴", Toast.LENGTH_SHORT).show()

            //从selection点开始粘贴
            var res = mDesktopLayout?.etInput?.text?.substring(0, mDesktopLayout?.etInput?.selectionStart!!)
            res += NoteTextUtil.pasteToResult(this)
            res += mDesktopLayout?.etInput?.text?.substring(mDesktopLayout?.etInput?.selectionStart!!)

            mDesktopLayout?.etInput?.setText(res)

            //粘贴完记得同步
            HideActivity.etHideInput?.text = mDesktopLayout?.etInput?.text

            //粘贴在非编辑状态下也是可以的，所以要记得保存
            if (!MyNoteApplication.isHideActivityShow) {
                NoteTextUtil.saveNote(this, mDesktopLayout?.etInput?.text.toString())
                sendBroadcast(Intent(NOTE_LIST_REFRESH))
            }
        }

        // 设置窗体的布局样式
        val layout = WindowManager.LayoutParams()

        //设置窗体的显示类型
        //★使用TYPE_TOAST，不需要权限就能弹出悬浮窗
//        if (NoteTextUtil.isInFit(mBrand)) {
//            layout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
//        } else {
//            layout.type = WindowManager.LayoutParams.TYPE_TOAST
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layout.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layout.type = WindowManager.LayoutParams.TYPE_PHONE
        }

        //设置窗体焦点及触摸
        layout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        //设置显示的模式
        layout.format = PixelFormat.RGBA_8888

        //设置对齐的边
        //★注意：这里是对齐不是位置，设置left和top，会让视图位移时以屏幕左上角为00点
        layout.gravity = Gravity.LEFT + Gravity.TOP

        //设置窗体的宽度和高度 单位px
        layout.width = WindowManager.LayoutParams.WRAP_CONTENT
        layout.height = WindowManager.LayoutParams.WRAP_CONTENT

        layout.x = mLayout?.x?.plus(5.toPx(this)) ?: 0
        layout.y = mLayout?.y?.minus(25.toPx(this)) ?: 0
        mWindowManager?.addView(copyOrPasteLayout, layout)
    }

    private fun removeCopyOrPasteLayout(view: CopyOrPasteLayout) {
        if (MyNoteApplication.isCopyOrPasteLayoutShow) {
            mWindowManager?.removeView(view)
            MyNoteApplication.isCopyOrPasteLayoutShow = false
        }
    }

    //主题的数据管理和动画切换类
//这里的数据在后期多的时候可以考虑 文件加载 的形式
//★后期版本考虑recyclerView


//    inner class ThemeManager {
//        //目前的主题数量
//        val themeNum = 6
//        //目前展示到了第几个主题
////        var nowDisplay = 0
//        //主题卡背景集合
//        val themeCardBackArray = listOf(null, R.drawable.bg_0, R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3, R.drawable.bg_4)
//        //关闭按钮颜色集合
//        val themeCloseColorArray = listOf(Color.BLACK,
//                Color.parseColor("#3E2723"),
//                Color.parseColor("#ffffff"),
//                Color.parseColor("#3E2723"),
//                Color.parseColor("#263238"),
//                Color.parseColor("#000000"))
//        //输入et颜色集合
//        val themeInputTextColorArray = listOf(Color.parseColor("#3E2723")
//                ,
//                Color.parseColor("#3E2723"),
//                Color.parseColor("#ffffff"),
//                Color.parseColor("#3E2723"),
//                Color.parseColor("#263238"),
//                Color.parseColor("#000000"))
//        //输入et背景集合
//        val themeInputBackArray = listOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT)
//        //et Margin集合
//        val themeMarginArray = listOf(ThemeMargin(0, 0, 10, 0),
//                ThemeMargin(0, 0, 10, 0),
//                ThemeMargin(0, 0, 10, 0),
//                ThemeMargin(5, 0, 15, 10),
//                ThemeMargin(28, 15, 28, 18),
//                ThemeMargin(25, 0, 25, 12))

    //目前是一页显示三种主题，所以是三个三个的切换
//        val imgList = listOf(ivTheme_1, ivTheme_2, ivTheme_3)
//        val imgCardList = listOf(cvTheme_1, cvTheme_2, cvTheme_3)
    //主题卡展示的示例图
//        val imgBack = listOf(R.drawable.bg_default, R.drawable.bg_0_fe, R.drawable.bg_1_fe, R.drawable.bg_2_fe, R.drawable.bg_3_fe, R.drawable.bg_4_fe)

    private var isFirstIn = true

    //主题卡切换开启动画 目前已完善为只执行一个动画
//        fun openThemeLayout() {
//            if (isFirstIn) {
//                setThemeCard()
//                return
//            }
//            more.isClickable = false
//            val outAnimator = ObjectAnimator.ofFloat(themeList, "translationY", 0f, -2000f)
//            outAnimator.duration = 300
//            outAnimator.interpolator = AccelerateInterpolator()
//            outAnimator.start()
//            outAnimator.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator?) {
//                    super.onAnimationEnd(animation)
//                    setThemeCard()
//                    val inAnimator = ObjectAnimator.ofFloat(themeList, "translationY", 1000f, 0f)
//                    inAnimator.interpolator = OvershootInterpolator()
//                    inAnimator.duration = 500
//                    inAnimator.start()
//                    //经过测试，只要滑出动画执行时isClickable为false即可
//                    more.isClickable = true
//                }
//            })
//        }

    //设置主题卡布局 并重新定义nowDisplay
//        fun setThemeCard() {
//            for (i in imgList.indices) {
//                if (isFirstIn && nowDisplay == 0) {
//                    setTheme(i, nowDisplay)
//                    nowDisplay++
//                    continue
//                }
//                if (nowDisplay < themeNum) {
//                    setTheme(i, nowDisplay)
//                } else {
//                    imgCardList[i].visibility = View.INVISIBLE
//                    imgList[i].visibility = View.INVISIBLE
//                }
//                nowDisplay++
//            }
//            if (isFirstIn) {
//                isFirstIn = false
//            }
//            if (nowDisplay >= themeNum) {
//                nowDisplay = 0
//            }
//        }

//        fun setTheme(i: Int, dis: Int) {
//            imgCardList[i].visibility = View.VISIBLE
//            imgList[i].visibility = View.VISIBLE
//            imgList[i].setImageResource(imgBack[dis])
//            /**
//             * MyNoteApplication.desktopLayout?.tvClose?.setTextColor(themeCloseColorArray[nowDisplay])
//             * 这里不能有这种写法，这种取的是随nowDisplay变化的值，不是固定值
//             */
//
//            imgList[i].setOnClickListener {
//                openSuccessDialog()
//                setDesktopLayoutTheme(dis)
//                SharedpreferenceManager.setInt(this@MainActivity, SharedpreferenceManager.NOW_THEME, dis)
//            }
//        }

//        fun openSuccessDialog() {
//            PromptDialog(this@MainActivity)
//                    .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
//                    .setAnimationEnable(true)
//                    .setTitleText("设置")
//                    .setContentText("主题保存成功")
//                    .setPositiveListener("OK") { dialog -> dialog.dismiss() }.show()
//        }

//        fun setDesktopLayoutTheme(dis: Int) {
//            val cardBack = themeCardBackArray[dis]
//            val closeColor = themeCloseColorArray[dis]
//            val inputTextColor = themeInputTextColorArray[dis]
//            val inputBack = themeInputBackArray[dis]
//            val themeMargin = themeMarginArray[dis]
//
//            /**
//             * 这里针对默认主题和非默认主题做了区分
//             */
//            if (cardBack != null) {
//                mDesktopLayout?.cvCard?.setBackgroundResource(cardBack)
//                mDesktopLayout?.cardBack?.setBackgroundColor(Color.TRANSPARENT)
//
//                mDesktopLayout?.cutoff?.visibility = View.GONE
//            } else {
//                //== null 说明是默认便签
//                //这个默认图还必须有，用于撑起View
//                mDesktopLayout?.cvCard?.setBackgroundResource(R.drawable.bg_0)
//                //新增一层覆盖图
//                mDesktopLayout?.cardBack?.setBackgroundColor(Color.parseColor("#ffffff"))
//                //滑动与et分解线显示，其余主题均不显示
//                mDesktopLayout?.cutoff?.visibility = View.VISIBLE
//            }
//            mDesktopLayout?.tvClose?.setTextColor(closeColor)
//            mDesktopLayout?.etInput?.setTextColor(inputTextColor)
////            mDesktopLayout?.etInput?.setBackgroundColor(inputBack)
//
//            val layoutParams = mDesktopLayout?.etInput?.layoutParams as ViewGroup.MarginLayoutParams
//            layoutParams.leftMargin = themeMargin.left.toPx(this@MainActivity)
//            layoutParams.topMargin = themeMargin.top.toPx(this@MainActivity)
//            layoutParams.rightMargin = themeMargin.right.toPx(this@MainActivity)
//            layoutParams.bottomMargin = themeMargin.bottom.toPx(this@MainActivity)
//            mDesktopLayout?.etInput?.layoutParams = layoutParams
//        }
//    }


    //重写退出键为回到桌面
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0) {
//            if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
//                drawer_layout.closeDrawer(GravityCompat.START)
//                return true
//            }
            val backHome = Intent(Intent.ACTION_MAIN)
            backHome.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            backHome.addCategory(Intent.CATEGORY_HOME)
            startActivity(backHome)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    //1.1代码  新增app搜索切换功能---------------------------

    var isAppListShow = false
    var appListSmall: ArrayList<AppInfoSmall> = ArrayList()
    var appListSmallAdapter: ListSmallAdapter? = null

    var commonUseAppList: ArrayList<AppInfoSmall> = ArrayList()
    var commonUseAppAdapter: CommonUseAppAdapter? = null

    //由于用到MainAty的东西，所以把App搜索写在了这里而不是DesktopLayout
    fun initChange() {
        //给全局搜索变量赋值  注意这个组件是一直存在的，只不过被隐藏了
        MyNoteApplication.etSearch = mDesktopLayout?.etSearch

        //获取adapter
        val adapter = ListAdapter(this, SystemUtil.getAppInfos(this))
        adapter.onUpdateCommonUseApp = object : ListAdapter.OnUpdateCommonUseAppListener {
            override fun update() {
                updateQuickAppList()
            }
        }
        //给listView赋值adapter
        mDesktopLayout?.lvAppList?.adapter = adapter

        //刷新常用app列表
        updateQuickAppList()
        mDesktopLayout?.lvCommonUseApp?.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)

        //EditText adapter搜索匹配3
        mDesktopLayout?.etSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(searchText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.let {
                    it.filter.filter(searchText)
                }
            }
        })

        mDesktopLayout?.etSearch?.setOnClickListener {
            if (!MyNoteApplication.isHideActivityShow) {
                //启动HideActivity，因为HideActivity的EditText的关系，键盘会自动弹出
                startApk(this.packageName, this, FROM_SEARCH)

                //设置HideActivity为已打开状态
                MyNoteApplication.isHideActivityShow = true

                //启动时HideActivity的et会自己去做同步，不需要我们手动去做同步
            }
            //其余情况，只需要利用其中的同步线程做同步即可，无需额外同步
        }

        //1.1新增app搜索界面小化后的快捷切换界面

        appListSmallAdapter = ListSmallAdapter(this, appListSmall)
        mDesktopLayout?.lvAppListSmall?.adapter = appListSmallAdapter
    }

    //显示app搜索页
    fun showAppList() {
//        mDesktopLayout?.appListContent?.visibility = View.VISIBLE

        //要求焦点
        mDesktopLayout?.etSearch?.isFocusable = true
        mDesktopLayout?.etSearch?.requestFocus()
        mDesktopLayout?.etSearch?.isCursorVisible = false

        //隐藏输入法
        //显示新视图前，假如上一个视图处于编辑状态，记得关闭
        if (MyNoteApplication.isHideActivityShow) {
            HideActivity.hideAty?.finish()
            MyNoteApplication.isHideActivityShow = false
        }
        isAppListShow = true

        //更新快切列表
        updateQuickAppList()
    }

    //显示Note页面
    fun showNote() {
        //隐藏输入法
        if (MyNoteApplication.isHideActivityShow) {
            HideActivity.hideAty?.finish()
            MyNoteApplication.isHideActivityShow = false
        }

        /**
         * bug：
         * 切换到其它功能页，切回时，需要点击两次才能弹出输入法。原因是需要点一次获得焦点！
         */
        mDesktopLayout?.etInput?.isFocusable = true
        mDesktopLayout?.etInput?.requestFocus()

        isAppListShow = false
    }

    /**
     * 更新快切列表
     * ★！！实际测试 recycler的notifyDataSetChanged不生效，只能暂时采用这种方式，重新生成列表
     * 交给子线程去获取数据，排序等
     */
    fun updateQuickAppList() {
        Thread({
            val array = SystemUtil.getAppOpenMessage(this@MainActivity)
            array?.let {
                commonUseAppList.clear()
                commonUseAppList.addAll(array.reversed())
                commonUseAppAdapter = CommonUseAppAdapter(this@MainActivity, commonUseAppList)
                runOnUiThread {
                    if (mDesktopLayout?.tvCommonUseAppTip?.visibility == View.VISIBLE) {
                        mDesktopLayout?.tvCommonUseAppTip?.visibility = View.GONE
                    }
                    mDesktopLayout?.lvCommonUseApp?.adapter = commonUseAppAdapter
                }
            }
        }).start()
    }

    private fun startApk(packname: String, context: Context, mark: Int) {
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
        intent.putExtra("from", mark)

        //主要 application的context是不能启动aty的
        context.startActivity(intent)

        mPopupWindow?.dismiss()
    }

    //功能页切换时的回调
    fun initDesktopSwitch() {
        mDesktopLayout?.setOnSwitchCheckedListener(object : DrawerLayout.OnSwitchCheckedListener {
            override fun onSwitchChecked(lastPosition: Int, nowPosition: Int) {

                if (lastPosition != nowPosition) {
                    when (nowPosition) {
                        0 -> {
                            showNote()
                        }
                        1 -> {
                            showAppList()
                        }
                    }
                }
            }
        })
    }
}
