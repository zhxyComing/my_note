package com.app.xuzheng.mynote.View

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.app.xuzheng.mynote.R
import android.graphics.Bitmap


/**
 * Created by xuzheng on 2017/8/2.
 */
class NoteViewPagerAdapter(val context: Context, val list: List<Bitmap>) : PagerAdapter() {

    override fun getCount(): Int = list.size

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object` as View

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val view = View.inflate(context, R.layout.viewpager_item, null)
        val ivPagerItem = view.findViewById<ImageView>(R.id.ivPagerItem)

        ivPagerItem.setImageBitmap(list[position])

        (container as ViewPager).addView(view)

        return view
    }

}