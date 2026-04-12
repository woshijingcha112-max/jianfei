package com.dietrecord.app

import android.app.Application
import android.util.Log
import com.dietrecord.app.core.data.AppContainer

private const val TAG = "DietRecordApp"

/**
 * Android 应用入口。
 *
 * 这里负责在进程启动时初始化全局依赖容器，后续页面与 ViewModel 都从这里取依赖。
 */
class DietRecordApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "应用启动，开始初始化全局依赖容器")
        container = AppContainer(context = applicationContext)
        Log.i(TAG, "全局依赖容器初始化完成")
    }
}
