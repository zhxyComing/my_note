package com.app.xuzheng.mynote.Utils

import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import java.nio.charset.Charset
import android.content.ClipData
import android.widget.Toast
import android.view.WindowManager
import android.os.Build
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import cn.refactor.lib.colordialog.PromptDialog
import com.app.xuzheng.mynote.Activity.MyNoteApplication
import com.app.xuzheng.mynote.Activity.arrayFit
import com.app.xuzheng.mynote.Bean.ClockInfo
import com.app.xuzheng.mynote.Bean.NoteInfo
import com.app.xuzheng.mynote.Fragment.NOTE_LIST_REFRESH
import com.app.xuzheng.mynote.Manager.SharedpreferenceManager
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by xuzheng on 2017/7/18.
 * 工具类要背，随时能用
 */
object NoteTextUtil {

    //获取指定目录下所有以txt结尾的文件名
    fun getNotesList(context: Context): ArrayList<String> {
        val list = ArrayList<String>()

        var path = getNotesStorePath(context)
        path = path.substring(0, path.length - 1)
        val file = File(path)
        val files = file.listFiles()
        files.mapTo(list) { it.name }
        return list
    }

    fun getNotesInfoList(context: Context): ArrayList<NoteInfo> {
        val list = ArrayList<NoteInfo>()
        val noteList = getNotesList(context)

        noteList.mapTo(list) { NoteInfo(getNoteByName(context, it), it) }

        //排序写在调用方法里
        Collections.sort(list, SortByNoteTime())
        return list
    }

    fun saveNote(context: Context, content: String) {
        var noteName = SharedpreferenceManager.getString(context, SharedpreferenceManager.NOW_NOTE)
        if (!TextUtils.isEmpty(noteName)) {
            NoteTextUtil.saveText(content, NoteTextUtil.getNotesStorePath(context) + noteName)
        } else {
            val myFmt = SimpleDateFormat("yy-MM-dd-HH-mm-ss")
            noteName = myFmt.format(Date())
            setSharedPreferenceNowNote(context, noteName)
            NoteTextUtil.saveText(content, NoteTextUtil.getNotesStorePath(context) + noteName)
        }
    }

    fun createNewNote(context: Context) {
        val myFmt = SimpleDateFormat("yy-MM-dd-HH-mm-ss")
        val noteName = myFmt.format(Date())

        //生成空文件
        val file = File(getNotesStorePath(context) + noteName)
        if (!file.exists()) {
            file.createNewFile()
        }

        //不把新create直接设定为当前桌面便签 而是询问
        val dialog = PromptDialog(context)
        dialog.setCancelable(true)
        dialog.setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                .setAnimationEnable(true)
                .setTitleText("提醒")
                .setContentText("是否设置新建便签为当前桌面便签？(点击空白区域取消)")
                .setPositiveListener("确定") {
                    NoteTextUtil.setNowNote(context, noteName)
                    context.sendBroadcast(Intent(NOTE_LIST_REFRESH))
                    dialog.dismiss()
                }.show()
//        setNowNote(context, noteName)
    }

    fun removeNote(context: Context, name: String) {
        val file = File(getNotesStorePath(context) + name)
        if (file.exists()) {
            file.delete()
        }
    }

    fun setNowNote(context: Context, noteName: String) {
        SharedpreferenceManager.setString(context, SharedpreferenceManager.NOW_NOTE, noteName)
        MyNoteApplication.etNote?.setText(getNote(context))
    }

    private fun setSharedPreferenceNowNote(context: Context, noteName: String) {
        SharedpreferenceManager.setString(context, SharedpreferenceManager.NOW_NOTE, noteName)
    }

    fun getNote(context: Context): String {
        var noteName = SharedpreferenceManager.getString(context, SharedpreferenceManager.NOW_NOTE)
        if (TextUtils.isEmpty(noteName)) {
            val myFmt = SimpleDateFormat("yy-MM-dd-HH-mm-ss")
            noteName = myFmt.format(Date())
            setSharedPreferenceNowNote(context, noteName)
            //生成空文件
            val file = File(getNotesStorePath(context) + noteName)
            val file1 = File(getNotesStorePath(context))
            if (!file1.exists()) {
                file1.mkdirs()
            }
            if (!file.exists()) {
                file.createNewFile()
            }

            return ""
        }
        return NoteTextUtil.loadText(NoteTextUtil.getNotesStorePath(context) + noteName)
    }

    fun getNoteByName(context: Context, name: String): String {
        return NoteTextUtil.loadText(NoteTextUtil.getNotesStorePath(context) + name)
    }

    fun saveNoteByName(context: Context, content: String, fileName: String) {
        if (TextUtils.isEmpty(fileName)) {
            return
        } else {
            NoteTextUtil.saveText(content, NoteTextUtil.getNotesStorePath(context) + fileName)
        }
    }

    fun getStorePath(context: Context): String {
        val file = context.getExternalFilesDir(null)
        if (file == null) {
            return Environment.getExternalStorageState() + "/Android/data/com.app.xuzheng.mynote/files/"
        } else {
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.toString() + "/"
        }
    }

    fun getNotesStorePath(context: Context): String {
        var file = context.getExternalFilesDir(null)
        if (file == null) {
            return Environment.getExternalStorageState() + "/Android/data/com.app.xuzheng.mynote/files/notes/"
        } else {
            file = File(file.absolutePath + "/notes")
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.toString() + "/"
        }
    }

    fun getSaveScreenImgPath(context: Context): String {
        var file = context.getExternalFilesDir(null)
        if (file == null) {
            return Environment.getExternalStorageState() + "/Android/data/com.app.xuzheng.mynote/files/screen"
        } else {
            file = File(file.absolutePath + "/screen")
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.toString() + "/"
        }
    }

    fun getClockStorePath(context: Context): String {
        var file = context.getExternalFilesDir(null)
        if (file == null) {
            return Environment.getExternalStorageState() + "/Android/data/com.app.xuzheng.mynote/files/clock/"
        } else {
            file = File(file.absolutePath + "/clock")
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.toString() + "/"
        }
    }

    //参数格式 clockTime yyyy-MM-dd HH:mm:ss
    fun saveClock(context: Context, content: String, clockTime: String, requestCode: Int) {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = format.parse(clockTime)

        val targetFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        val name = targetFormat.format(date)

        NoteTextUtil.saveText("" + requestCode + "&" + content, NoteTextUtil.getClockStorePath(context) + name)
    }

    //参数格式 clockTime yyyy-MM-dd-HH-mm-ss
    fun getClockContentByName(context: Context, clockTime: String): String? {
        val res = NoteTextUtil.loadText(NoteTextUtil.getClockStorePath(context) + clockTime)
        if (!TextUtils.isEmpty(res)) {
            val temp = res.split("&")
            val num = temp[0].length
            return res.substring(num + 1)
        }
        return null
    }

    fun getClockInfoByName(context: Context, clockTime: String): ClockInfo {
        val res = NoteTextUtil.loadText(NoteTextUtil.getClockStorePath(context) + clockTime)
        val temp = res.split("&")
        val num = temp[0].length
        return ClockInfo(res.substring(num + 1), clockTime, temp[0].toInt())
    }

    fun getClockList(context: Context): ArrayList<String> {
        val list = ArrayList<String>()

        var path = getClockStorePath(context)
        path = path.substring(0, path.length - 1)
        val file = File(path)
        val files = file.listFiles()
        files.mapTo(list) { it.name }
        return list
    }

    fun getClockInfoList(context: Context): ArrayList<ClockInfo> {
        val list = ArrayList<ClockInfo>()
        val noteList = getClockList(context)

        noteList.mapTo(list) { getClockInfoByName(context, it) }

        Collections.sort(list, SortByClockTime())
        return list
    }

    //这个可以放在TimeUtils里
    fun getNearClockTime(context: Context): String? {
        val list = ArrayList<ClockInfo>()
        val noteList = getClockList(context)

        noteList.mapTo(list) { getClockInfoByName(context, it) }

        Collections.sort(list, SortByClockTime())

        for (i in list) {
            if (TimeUtils.isBigger(i.fileName, "yyyy-MM-dd-HH-mm-ss")) {
                val time = TimeUtils.stringToDate(i.fileName, "yyyy-MM-dd-HH-mm-ss")
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val res = format.format(time)
                return res
            }
        }
        return null
    }

    //参数格式 clockTime yyyy-MM-dd-HH-mm-ss
    fun removeClock(context: Context, name: String) {
        val file = File(getClockStorePath(context) + name)
        if (file.exists()) {
            file.delete()
        }
    }

    fun saveText(text: String, path: String) {
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        }
        val fout = FileOutputStream(path)//也可以传一个file作为参数
        val bytes = text.toByteArray()
        fout.write(bytes)
        fout.close()
    }

    fun loadText(path: String): String {
        val file = File(path)
        if (!file.exists()) {
            return ""
        }
        val fin = FileInputStream(path)
        val length = fin.available()
        val buffer = ByteArray(length)
        fin.read(buffer)
        fin.close()
        return String(buffer, Charset.forName("UTF-8"))
    }

    //复制到剪切板
    fun copy(content: String, context: Context) {
        // 得到剪贴板管理器
        val cmb = context
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cmb.text = content.trim { it <= ' ' }
    }

    //复制到剪切板
    fun copyFromEditText(content: String, context: Context) {

        // Gets a handle to the clipboard service.
        val mClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Creates a new text clip to put on the clipboard
        val clip = ClipData.newPlainText("simple text", content)

        // Set the clipboard's primary clip.
        mClipboard.primaryClip = clip
    }

    //从剪切板获取文字
    fun pasteToResult(context: Context): String {
        // Gets a handle to the clipboard service.
        val mClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        var resultString = ""
        // 检查剪贴板是否有内容
        if (!mClipboard.hasPrimaryClip()) {
            Toast.makeText(context,
                    "剪切板为空，复制文字后再粘贴哦", Toast.LENGTH_SHORT).show()
        } else {
            val clipData = mClipboard.primaryClip
            val count = clipData.itemCount

//                for (i in 0..count - 1) {
//                    val item = clipData.getItemAt(i)
//                    val str = item
//                            .coerceToText(context)
//                    resultString += str
//                }

            //★下一条map中的it就是上一条map的结果！
            (0..count - 1)
                    .map { clipData.getItemAt(it) }
                    .map {
                        it.coerceToText(context)
                    }
                    .forEach { resultString += it }
        }
        return resultString
    }

    /**
     * 设置沉浸式状态栏
     * 该方法只负责状态栏的透明化和不占空间，故内容会占据状态栏（但是状态栏电池图标等还在），需要手动做偏移
     * 目前使用页：WebActivity （H5自己做了偏移，所以不需要我们手动去写）  启动页（不需要偏移）
     * @param activity
     */
    fun setImmBarNonOffset(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * 获取状态栏高度
     * @param context
     * *
     * @return
     */
    fun getStatusHeight(context: Context): Int {
        var statusBarHeight = -1
        //获取status_bar_height资源的ID
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    fun compressScale(image: Bitmap): Bitmap {

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().size / 1024 > 1024) {
            baos.reset()// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos)// 这里压缩50%，把压缩后的数据存放到baos中
        }
        var isBm = ByteArrayInputStream(baos.toByteArray())
        val newOpts = BitmapFactory.Options()
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeStream(isBm, null, newOpts)
        newOpts.inJustDecodeBounds = false
        val w = newOpts.outWidth
        val h = newOpts.outHeight
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        // float hh = 800f;// 这里设置高度为800f
        // float ww = 480f;// 这里设置宽度为480f
        val hh = 980f
        val ww = 550f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        var be = 1// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (newOpts.outWidth / ww).toInt()
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (newOpts.outHeight / hh).toInt()
        }
        if (be <= 0)
            be = 1
        newOpts.inSampleSize = be // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565

        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = ByteArrayInputStream(baos.toByteArray())
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts)

        return compressImage(bitmap)// 压缩好比例大小后再进行质量压缩

        //return bitmap;
    }

    fun compressImage(image: Bitmap): Bitmap {

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 90

        while (baos.toByteArray().size / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset() // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10// 每次都减少10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray())// 把压缩后的数据baos存放到ByteArrayInputStream中
        val bitmap = BitmapFactory.decodeStream(isBm, null, null)// 把ByteArrayInputStream数据生成图片
        return bitmap
    }

    fun clamp(target: Int?, min: Int, max: Int): Int? {
        if (min >= max) return target
        target?.let {
            return when {
                target < min -> min
                target > max -> max
                else -> target
            }
        } ?: return target
    }


    /**
     * 通过图片资源id获取图片对应的压缩后的Bitmap对象
     * 不会产生用于压缩前的Bitmap导致OOM
     * 但是缺点：这清晰度，去你妈的
     */
    fun getCompressedBitmap(res: Resources, resId: Int, size: Int): Bitmap {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        //设置了这个属性，不会返回实际的bitmap，也不会分配内存空间，用于获取图片宽高继而计算比例

        BitmapFactory.decodeResource(res, resId, options)// 第一次解析
        options.inSampleSize = size //压缩size倍

        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)// 第一次解析
    }

    //判断是否在适配名单里
    fun isInFit(brand: String): Boolean = arrayFit.any { it.equals(brand, true) }
}