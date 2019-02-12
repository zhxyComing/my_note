package com.app.xuzheng.mynote.View

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by xuzheng on 2017/8/4.
 *
 * 闪动文字 自定义View
 */
class ShaderTextView : TextView {
    private var mPaint: Paint? = null
    private var mViewWidth: Int = 0
    private var mLinearGradient: LinearGradient? = null
    private var mGradientMatrix: Matrix? = null
    private var mTranslate: Int = 0

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onSizeChanged(w: Int, h: Int, old_w: Int, old_h: Int) {
        super.onSizeChanged(w, h, old_w, old_h)
        if (mViewWidth == 0) {
            mViewWidth = measuredWidth
            if (mViewWidth > 0) {
                mPaint = paint
                mLinearGradient = LinearGradient(0f, 0f, mViewWidth.toFloat(), 0f, intArrayOf(currentTextColor, 0xAD1457, currentTextColor), null, Shader.TileMode.CLAMP)
                mPaint!!.shader = mLinearGradient
                mGradientMatrix = Matrix()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mGradientMatrix != null) {
            mTranslate += mViewWidth / 20
            if (mTranslate > mViewWidth * 10) {
                mTranslate = -mViewWidth
            }
            mGradientMatrix!!.setTranslate(mTranslate.toFloat(), 0f)
            mLinearGradient!!.setLocalMatrix(mGradientMatrix)
            postInvalidateDelayed(30)
        }
    }
}
