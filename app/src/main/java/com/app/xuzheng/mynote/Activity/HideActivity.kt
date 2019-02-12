package com.app.xuzheng.mynote.Activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import com.app.xuzheng.mynote.Fragment.NOTE_LIST_REFRESH
import com.app.xuzheng.mynote.Utils.NoteTextUtil
import com.app.xuzheng.mynote.R
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.activity_hide.*
import java.util.*

/**
 * 因为本质上我们软键盘的输入是写入不到悬浮窗口上的
 * （可以参照市面上，没有一款软件是可以直接在桌面输入文字的）
 * ☆所以推论，输入法的输入要依赖于页面即Activity？
 * 所以本app的实现方式，是在后台放一个隐藏的activity和一个隐藏的editText，然后隐藏et和桌面et同步的方式实现
 * 同步：
 * 1.文字的同步
 * 2.光标的同步 （全局标签被点击时hideInput光标变化同步；全局便签光标被拖动时hideInput光标变化同步）
 * 3.界面的同步 （点击退出键退出HideActivity而不只是关闭键盘；关闭键盘时退出HideActivity）
 */
const val FROM_INPUT: Int = 0
const val FROM_SEARCH: Int = 1

class HideActivity : Activity() {

    var fromNum: Int = 0

    //用于控制"光标实时监听线程"的关闭
    var isHideAtyLive = false
    //根布局，用于监听软键盘关闭时的布局变化
    var mSearchLayout: FrameLayout? = null
    //用于判断是否开始启动"软键盘关闭"监听
    var keyboardCanMonitor = false

    //指定用于同步的et
    var syncEtInput: EditText? = null

    companion object {
        //当HideActivity启动时，就会给该变量赋值，便于全局便签同步时调用
        var etHideInput: EditText? = null
        //维护一个供外界关闭的自身aty
        var hideAty: HideActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hide)

        hideAty = this

        fromNum = intent.getIntExtra("from", 0)

        //为了方便全局便签同步，给HideActivity类的变量临时赋值，会在onDestroy销毁
        etHideInput = mHideInput

        //只有在记事状态才需要做文字的初始同步
        if (fromNum == FROM_INPUT) {
            syncEtInput = MyNoteApplication.etNote
        } else if (fromNum == FROM_SEARCH) {
            syncEtInput = MyNoteApplication.etSearch
        }

        /**
         * ★1.1 文字的同步
         * 每次重启HideActivity时mHideInput是全新的，所以应该把全局便签的文字同步过去
         */
        mHideInput.text = syncEtInput?.text

        /**
         * ★1.2 文字的同步
         * 文字改变时实时同步
         */
        mHideInput.addTextChangedListener(mTextWatcher)

        /**
         * ★2.1 光标的同步
         * 不仅要同步文字，还要同步光标起始位置
         */
        syncEtInput?.selectionStart?.let {
            mHideInput.setSelection(it)
        }

        /**
         * ★2.2 光标的同步
         * 处理拖动全局便签光标时 mHideInput因为不是click事件继而无法同步光标的问题
         * 0.2s做一次自动同步
         */
        Thread(Runnable {
            isHideAtyLive = true
            while (isHideAtyLive) {
                var isZero = false
                /**
                 * 奇怪的bug
                 * 按的太快，selectionStart有可能为0
                 * 所有要想办法屏蔽掉偶尔一次且仅一次为0的情况
                 */
                //应该回到主线程去改变UI setSelection是主线程执行的方法
                syncEtInput?.selectionStart?.let {
                    runOnUiThread {
                        try {
                            // Bug解决:错位
                            // 解决方式：屏蔽掉第一个0
                            // 如果selection为0并且上一次不为0,说明是第一个0
                            if (it == 0 && !isZero) {
                                isZero = true
                                //如果上一次为0，说明第一个0已经跳过，重置状态
                            } else if (isZero) {
                                isZero = false
                                mHideInput.setSelection(it)
                            } else {
                                mHideInput.setSelection(it)
                            }
                        } catch (e: Exception) {//在这里可能崩溃，原因尚不清楚

                        }
                    }
                }
                //更新为0.1s同步一次
                Thread.sleep(100)
            }
        }).start()

        /**
         * ★3.1 界面的同步
         * 点击退出键时，不只是键盘小化，而是HideActivity退出；即退出编辑界面
         */
        mHideInput.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                finish()
                return@OnKeyListener true
            }
            false
        })

        /**
         * ★3.2 界面的同步
         * 处理键盘关闭时，HideActivity退出；监听键盘的关闭
         */
        addKeyboardCloseListener()

        //点击Hide背景时关闭输入法编辑
        rootView.setOnClickListener {
            finish()
        }
    }

    override fun onStop() {
        /**
         * 不可见
         * 自身的切换应用因为点开时会关闭输入法，是没有问题的，但是360侧边栏因为不会隐藏输入法，所以会导致一些问题
         * 经过测试 finish写在这里，从桌面弹出输入法，然后使用360侧边栏切换应用，仍然会有问题
         */
        super.onStop()
    }

    override fun onPause() {
        /**
         * 可见不可操作
         * 因为本来就是HideAty，所以当不可见或不可操作时，输入法自然就无法弹出，也就是小化
         * 当处于输入法弹出状态时，切换应用，会导致HideAty处于后台状态，失去了它本来的作用，这是不允许的
         * 所以一旦处于后台，就将它关闭
         */
        finish()
        super.onPause()
        MobclickAgent.onPause(this)
    }

    override fun onDestroy() {
        //防止内存泄漏?
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            mSearchLayout?.viewTreeObserver?.removeGlobalOnLayoutListener(mOnGlobalLayoutListener)
        } else {
            mSearchLayout?.viewTreeObserver?.removeOnGlobalLayoutListener(mOnGlobalLayoutListener)
        }
        mHideInput.removeTextChangedListener(mTextWatcher)
        MyNoteApplication.isHideActivityShow = false
        etHideInput = null
        isHideAtyLive = false

        //★可能存在输入法还在，也就是本页面还在，但是Note已经被关掉的情况，这个时候不能再保存数据
        //解决在HideAty存在时关闭Note导致文字丢失的bug
        if (fromNum == FROM_INPUT) {//只有来自编辑记事才会保存
            MyNoteApplication.etNote?.let {
                NoteTextUtil.saveNote(this, it.text.toString())
                sendBroadcast(Intent(NOTE_LIST_REFRESH))
            }
        }

        //当销毁时，记得即时将此全局aty置为null
        hideAty = null
        super.onDestroy()
    }

    private val mTextWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(allText: CharSequence?, startPosition: Int, p2: Int, inputSize: Int) {
            syncEtInput?.setText(allText)
            syncEtInput?.setSelection(startPosition + inputSize)
        }
    }

    /**
     * ★
     * 键盘点击关闭(不是退出键关闭)的监听
     * 之所以有计时器，是为了屏蔽掉启动时键盘弹出所触发的监听
     * 因为本质是为了监听键盘关闭，而不是弹出，所以添加计时器去屏蔽
     * 同时也说明了，只要HideActivity存在，此时键盘就一定处于弹出状态；反之亦然
     */
    fun addKeyboardCloseListener() {
        mSearchLayout = rootView
        mSearchLayout?.viewTreeObserver?.addOnGlobalLayoutListener(mOnGlobalLayoutListener)

        // 只能采用时间屏蔽的方式
        // 0.5s，相信一般情况下不会出现在0.5s内关掉刚刚弹出的键盘的情况
        val task = object : TimerTask() {
            override fun run() {
                //每次需要执行的代码放到这里面。
                keyboardCanMonitor = true
            }
        }
        Timer().schedule(task, 500)
    }

    private val mOnGlobalLayoutListener = OnGlobalLayoutListener {
        if (keyboardCanMonitor) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }
}
