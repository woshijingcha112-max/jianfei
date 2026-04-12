package com.dietrecord.app.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 页面到达与修改后刷新副作用。
 *
 * 每次导航到达目标页时刷新一次；若页面停留期间又收到新的成功修改版本，则再刷新一次。
 */
@Composable
fun AppDestinationRefreshEffect(
    navigationEntry: Any,
    refreshVersion: Long,
    onEnterRefresh: () -> Unit,
    onMutationRefresh: () -> Unit = onEnterRefresh
) {
    var lastHandledVersion by remember(navigationEntry) {
        mutableLongStateOf(refreshVersion)
    }

    LaunchedEffect(navigationEntry) {
        onEnterRefresh()
        lastHandledVersion = refreshVersion
    }

    LaunchedEffect(refreshVersion) {
        if (refreshVersion > lastHandledVersion) {
            onMutationRefresh()
            lastHandledVersion = refreshVersion
        }
    }
}
