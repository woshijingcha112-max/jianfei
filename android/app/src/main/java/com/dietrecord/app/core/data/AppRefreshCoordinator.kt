package com.dietrecord.app.core.data

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "AppRefreshCoordinator"

/**
 * 应用级刷新协调器。
 *
 * 每次修改成功后只递增一次全局版本号，由目标页在到达或停留期间自行消费并刷新。
 */
class AppRefreshCoordinator {
    private val _mutationVersion = MutableStateFlow(0L)
    val mutationVersion: StateFlow<Long> = _mutationVersion.asStateFlow()

    fun markMutationSuccess(source: String) {
        _mutationVersion.update { current ->
            val next = current + 1
            Log.i(TAG, "记录一次成功修改，source=$source, version=$next")
            next
        }
    }
}
