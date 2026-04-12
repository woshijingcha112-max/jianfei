package com.dietrecord.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 应用导航目的地定义。
 */
sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    data object Home : AppDestination("home", "首页", Icons.Filled.Home)
    data object Camera : AppDestination("camera", "拍照", Icons.Filled.CameraAlt)
    data object Recognize : AppDestination("recognize", "识别结果")
    data object Search : AppDestination("search", "搜索")
    data object Goal : AppDestination("goal", "目标", Icons.Filled.Flag)

    fun requireIcon(): ImageVector {
        return icon ?: error("导航目的地 $route 未配置图标")
    }
}
