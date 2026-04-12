package com.dietrecord.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dietrecord.app.core.ui.theme.DietRecordTheme
import com.dietrecord.app.navigation.AppNavHost

/**
 * 单 Activity 入口。
 *
 * Android 端所有页面都挂在 Compose 导航树下，Java 开发可以把这里理解为前端壳层入口。
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DietRecordTheme {
                AppNavHost()
            }
        }
    }
}
