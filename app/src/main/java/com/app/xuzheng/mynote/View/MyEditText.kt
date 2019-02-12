package com.app.xuzheng.mynote.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.EditText
import com.app.xuzheng.mynote.Utils.toPx

/**
 * Created by xuzheng on 2017/8/16.
 */
class MyEditText : EditText {
    private var paint: Paint? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        paint = Paint()
        paint?.strokeWidth = 1f
        paint?.style = Paint.Style.STROKE
        paint?.isAntiAlias = true
        paint?.textAlign = Paint.Align.CENTER
        paint?.textSize = 16f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //360手机上10sp是26，可见这是px值 140dp是360.可见还有padding margin等影响
        val size = lineHeight.toFloat()

        //直接绘制150行 以防超出
        for (i in 0..150) {
            paint?.color = Color.parseColor("#BCAAA4")
            canvas?.drawLine(0f + 4.toPx(context),
                    size * (i + 1) + 2.toPx(context),
                    (measuredWidth - 2.toPx(context)).toFloat(),
                    size * (i + 1) + 2.toPx(context),
                    paint)
            paint?.color = Color.parseColor("#E0E0E0")
            canvas?.drawText((i + 1).toString(),
                    (measuredWidth - 4.toPx(context)).toFloat(),
                    size * (i + 1),
                    paint)
        }

    }
}