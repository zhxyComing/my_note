package com.app.xuzheng.mynote.View

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import com.app.xuzheng.mynote.R
import kotlinx.android.synthetic.main.use_tip_dialog.*
/**
 * Created by xuzheng on 2017/8/1.
 *
 * dialog 背景bug  小米旧手机弹窗提示bug （sp解决）
 *
 * 原本的启动引导viewPager bug较多 在1.5版本上已经删除
 */
//1.设置风格
class TipDialog(context: Context, val list: List<Bitmap>) : Dialog(context, R.style.UseTipDialog) {

    //注意这里是Style，不是Layout！！！

    //★注意 直接从java导进来，Bundle后是没有?的，运行会报错！！！
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //2.设置布局
        //3.布局里设置布局背景边框等
        setContentView(R.layout.use_tip_dialog)

        window.setWindowAnimations(R.style.TipAnim)
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false)

        init()
    }

    fun init() {
        ivCloseTip.setOnClickListener {
            dismiss()
        }

        vpTip.adapter = NoteViewPagerAdapter(context, list)
        vpTip.currentItem = 0
        indicator.setViewPager(vpTip)
    }
}