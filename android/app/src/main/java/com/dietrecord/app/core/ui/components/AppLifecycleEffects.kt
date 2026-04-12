package com.dietrecord.app.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * 页面恢复到前台时执行回调。
 *
 * 用于处理“当前页面需要在返回后重新拉取最新数据”的通用场景，
 * 例如从编辑页返回首页后，需要刷新首页总览与列表。
 */
@Composable
fun AppOnResumeEffect(
    onResume: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnResume = rememberUpdatedState(onResume)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                latestOnResume.value()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
