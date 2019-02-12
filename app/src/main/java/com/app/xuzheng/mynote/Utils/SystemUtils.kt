package com.app.xuzheng.mynote.Utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import com.app.xuzheng.mynote.Bean.AppInfo
import java.util.*
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.app.xuzheng.mynote.Bean.AppInfoSmall
import kotlin.collections.ArrayList
import com.app.xuzheng.mynote.Activity.ConnectActivity
import net.sourceforge.pinyin4j.PinyinHelper
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.regex.Pattern


/**
 * Created by xuzheng on 2017/7/31.
 */
/**
 * 系统工具类
 */
object SystemUtil {

    /**
     * 获取当前手机系统语言。

     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    val systemLanguage: String
        get() = Locale.getDefault().language

    /**
     * 获取当前系统上的语言列表(Locale列表)

     * @return  语言列表
     */
    val systemLanguageList: Array<Locale>
        get() = Locale.getAvailableLocales()

    /**
     * 获取当前手机系统版本号

     * @return  系统版本号
     */
    val systemVersion: String
        get() = android.os.Build.VERSION.RELEASE

    /**
     * 获取手机型号

     * @return  手机型号
     */
    val systemModel: String
        get() = android.os.Build.MODEL

    /**
     * 获取手机厂商

     * @return  手机厂商
     */
    val deviceBrand: String
        get() = android.os.Build.BRAND

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)

     * @return  手机IMEI  暂时没权限
     */
    //    fun getIMEI(ctx: Context): String? {
//        val tm = ctx.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager
//        if (tm != null) {
//            return tm.deviceId
//        }
//        return null
//    }

    val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
    val OP_SYSTEM_ALERT_WINDOW = "OP_SYSTEM_ALERT_WINDOW"
    //获取某一项权限是否开启的通用方法
    //通过android.app.AppOpsManager的checkOp方法,将id，uid，包名传入，即可查询有无权限
    //但是由于是系统方法，我们获取不到，所以只能通过反射
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun isLimitEnabled(context: Context, limitName: String): Boolean {

        val CHECK_OP_NO_THROW = "checkOpNoThrow"

        val mAppOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val appInfo = context.applicationInfo
        val pkg = context.applicationContext.packageName
        val uid = appInfo.uid

        var appOpsClass: Class<*>? = null
        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager::class.java.name)
            val checkOpNoThrowMethod = appOpsClass!!.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String::class.java)
            val opPostNotificationValue = appOpsClass.getDeclaredField(limitName)

            val value = opPostNotificationValue.get(Int::class.java) as Int
            return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int === AppOpsManager.MODE_ALLOWED

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * 反射执行步骤
     * 1.获取AppOpsManager 权限管理类
     * 2.获取权限名对应的属性变量
     * 3.获取通过属性变量查询是否有该权限的方法
     * 4.调用方法，将属性变量作为参数传入，查询是否有对应权限
     */

    fun getAppMemory(): Int {
        val rt = Runtime.getRuntime()
        val maxMemory = rt.maxMemory()
        return (maxMemory / (1024 * 1024)).toInt()
    }

    //通过包名启动app
    fun startApkByPackageName(packagename: String, context: Context, appName: String) {

        /**
         * 这种方式启动app，造成的后果就是所有新启动的app都运行在一个task里
         * 当之前启动的app没有点击退出键时，新启动的app会覆盖栈顶，而原先app还存在task栈原始的位置
         * 这就导致再次调用该方法启动时，app由于已经启动，所以不能再启动。但是由于栈位置没变，所以无响应无显示
         *
         * 微信等app aty不能存在与别的栈
         * 而不设置flags，又会导致上述的调不到前台的问题
         *
         * ★为什么目前采用这种 先跳转新aty再跳转到其它app 的解决方法：
         *
         * 1.假如采用new_task
         * 本质上，新启动app的aty与本app当前aty处于同一个task
         * 按理说该flag相当于singleTask，即旧aty已存在于栈，会直接调到栈顶，它之上的aty会被弹出；旧aty不存在于栈，则创建新的压入栈
         * 但是实测：可能各自app的启动aty定义不同所致，导致旧app没关闭新app直接覆盖时，旧app再次使用该方法，不能被调到task栈顶，即不能被调起
         *
         * 2.采用clear_top,clear_task等方式
         * 本质上，新aty与本app仍然处于同一个task
         * 实测：一般旧app终于能被调到栈顶了。但是发现，微信，游民等app会无法启动，直接跳转到本task的上一个aty，如果没有则回到本App的MainAty
         *
         * 3.所以从本质上解决，需要每次启动新app，就需要一个全新的栈去承载新app的新aty
         * 所以需要singleTask
         * 但是flag不能设置singleTask
         * 所以只能由本app自己创建一个launchMode为singleTask的aty，即自己创建一个额外的task供新app的新aty使用
         */
        val intent = Intent(context, ConnectActivity::class.java)
        intent.putExtra("packageName", packagename)
        intent.putExtra("appName", appName)
        context.startActivity(intent)
    }

    //保存app跳转过的信息
    fun saveAppOpenMessage(context: Context, appName: String, packagename: String) {
        val array = ArrayList<AppInfoSmall>()
        val appList = getAppOpenMessage(context)

        if (appList == null) {//此时列表为空，说明是第一次加载
            array.add(AppInfoSmall(appName, packagename, 1)) //加入列表
        } else {//列表不为空，则是往已有列表添加数据
            //添加规则 常用应用列表为10个
            //每次添加时，应用属于10个列表中的一个，则该应用启动次数+1
            //如果应用不属于10个列表中的一个，则删除掉启动次数最少的那一个，将该app加入其中

            var hasApp = false
            //判断是否已存在该app
            array.addAll(appList)
            for (app in array) {
                if (app.name == appName && app.packageName == packagename) {
                    //如果app存在，只要该app启动次数+1即可
                    app.num += 1
                    hasApp = true
                }
            }

            //说明此时app不存在
            if (!hasApp) {
                //判断长度是否已经达到10个,达到删除，没达到直接添加
                if (array.size >= 10) {
                    //注意，此时array取自JsonUtil，已经是排过序的
                    //直接移除启动次数最少的
                    array.removeAt(0)
                }
                array.add(0, AppInfoSmall(appName, packagename, 1))
            }
        }
        saveAppOpenList(array, context)
    }

    fun getAppOpenMessage(context: Context): List<AppInfoSmall>? {
        val res = NoteTextUtil.loadText(NoteTextUtil.getStorePath(context) + "app.txt")
        var list: List<AppInfoSmall>? = null
        if (res != null && !TextUtils.isEmpty(res)) {
            list = JsonUtils.parseToList(res)
        }
        return list
    }

    private fun saveAppOpenList(array: ArrayList<AppInfoSmall>, context: Context) {
        val res = JsonUtils.parseToJson(array) //将列表转为json
        Log.e("saveJson", "保存的Json:" + res)
        NoteTextUtil.saveText(res, NoteTextUtil.getStorePath(context) + "app.txt") //保存到本地
    }

    //获取所有已安装的app信息
    fun getAppInfos(context: Context): ArrayList<AppInfo> {
//        val pm = context.packageManager
//        //得到所有的application信息
//        val packgeInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES)
//        /* 获取应用程序的名称，不是包名，而是清单文件中的labelname
//            String str_name = packageInfo.applicationInfo.loadLabel(pm).toString();
//            appInfo.setAppName(str_name);
//         */
        val pm = context.packageManager
        val packgeInfos = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)//得到所有的Application信息
        val list: ArrayList<AppInfo> = ArrayList()
        packgeInfos
                .map { it as ApplicationInfo }
//                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM !== 0 } //所有系统应用
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM <= 0 || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP !== 0 } //所有第三方应用
                .mapTo(list) {
                    AppInfo(it.packageName,
                            it.loadLabel(pm).toString(),
                            it.loadIcon(pm))
                }

        //按首字母排序
        Collections.sort(list, SortByAppName())

//        for (applicationInfo in packgeInfos) {
//            val appInfo = applicationInfo as ApplicationInfo
//            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM !== 0) {
//                list.add(AppInfo(appInfo.packageName,
//                        appInfo.loadLabel(pm).toString(),
//                        appInfo.loadIcon(pm)))
//            }
//        }
        return list
    }


    //1.1新功能 截屏

    //这种截屏方式只能截取本app内的cache
    //完全版截屏后续添加，感觉需求不大
    fun getScreenImg(v: View, context: Context) {
        val view = v.rootView
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        var bitmap = view.drawingCache
        if (bitmap != null) {
//            bitmap.compress(Bitmap.CompressFormat.PNG,100, out)  100是不压缩
            saveScreenImg(bitmap, context)
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                bitmap = null
            }
        }
        view.isDrawingCacheEnabled = false
    }

    fun saveScreenImg(bitmap: Bitmap, context: Context) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA)
            val fileName = sdf.format(Date()) + ".png"
            val fos = FileOutputStream(NoteTextUtil.getSaveScreenImgPath(context) + fileName)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            Toast.makeText(context, "截图已保存到" + NoteTextUtil.getSaveScreenImgPath(context) + fileName, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "截图异常", Toast.LENGTH_SHORT).show()
        }
    }

    fun isHanzi(txt: String): Boolean {
//        var p = Pattern.compile("[0-9]*")
//        var m = p.matcher(txt)
//
//        p = Pattern.compile("[a-zA-Z]")
//        m = p.matcher(txt)

        var p = Pattern.compile("[\u4e00-\u9fa5]")
        var m = p.matcher(txt)
        if (m.matches()) {
            return true
        }
        return false
    }

    //根据单个汉字得到拼音首字母 拼音一般小写
    fun getFirstLetter(char: Char): Char? {
        if (SystemUtil.isHanzi(char.toString())) {
            val firstString: String? = PinyinHelper.toHanyuPinyinStringArray(char)[0]
            firstString.let {
                return it!![0]
            }
        }
        return null
    }
}