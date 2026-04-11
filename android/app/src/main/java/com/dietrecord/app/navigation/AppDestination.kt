package com.dietrecord.app.navigation

sealed class AppDestination(val route: String, val label: String) {
    data object Home : AppDestination("home", "首页")
    data object Camera : AppDestination("camera", "拍照")
    data object Recognize : AppDestination("recognize", "识别结果")
    data object Search : AppDestination("search", "搜索")
    data object Goal : AppDestination("goal", "目标")
}
