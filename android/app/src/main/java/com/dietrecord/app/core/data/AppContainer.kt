package com.dietrecord.app.core.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dietrecord.app.core.network.ApiConfig
import com.dietrecord.app.data.DietRecordRepository
import com.dietrecord.app.data.GoalRepository
import com.dietrecord.app.data.RealDietRecordRepository
import com.dietrecord.app.data.RealGoalRepository
import com.dietrecord.app.data.RealRecognitionRepository
import com.dietrecord.app.data.RecognitionRepository
import com.dietrecord.app.feature.camera.CameraViewModel
import com.dietrecord.app.feature.goal.GoalViewModel
import com.dietrecord.app.feature.home.HomeViewModel
import com.dietrecord.app.feature.recognize.RecognizeResultViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

private const val TAG = "AppContainer"

/**
 * 全局依赖容器。
 *
 * 这里集中创建网络服务、Repository 和各页面 ViewModel Factory，
 * 让 Java 开发可以按“容器 -> 仓储 -> ViewModel -> 页面”的结构理解安卓代码。
 */
class AppContainer(
    context: Context,
    private val baseUrl: String = ApiConfig.DEFAULT_BASE_URL,
    private val dispatchers: AppDispatchers = AppDispatchers()
) {
    private val appScope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val dietApiService = ApiConfig.createDietApiService(baseUrl)
    val refreshCoordinator = AppRefreshCoordinator()

    init {
        Log.i(TAG, "创建应用容器，baseUrl=$baseUrl")
        Log.d(TAG, "应用上下文包名=${context.packageName}")
    }

    val goalRepository: GoalRepository = RealGoalRepository(
        api = dietApiService,
        scope = appScope,
        dispatchers = dispatchers
    )

    val recognitionRepository: RecognitionRepository = RealRecognitionRepository(
        api = dietApiService,
        scope = appScope,
        dispatchers = dispatchers
    )

    val dietRecordRepository: DietRecordRepository = RealDietRecordRepository(
        api = dietApiService,
        recognitionRepository = recognitionRepository,
        scope = appScope,
        dispatchers = dispatchers
    )

    fun homeViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        HomeViewModel(dietRecordRepository = dietRecordRepository)
    }

    fun goalViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        GoalViewModel(
            goalRepository = goalRepository,
            refreshCoordinator = refreshCoordinator
        )
    }

    fun cameraViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        CameraViewModel(recognitionRepository = recognitionRepository)
    }

    fun recognizeResultViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        RecognizeResultViewModel(
            recognitionRepository = recognitionRepository,
            dietRecordRepository = dietRecordRepository,
            refreshCoordinator = refreshCoordinator
        )
    }
}

/**
 * 最小化 ViewModel 工厂。
 *
 * 当前项目没有引入 DI 框架，这里用统一工厂把依赖注入 ViewModel。
 */
private fun <T : ViewModel> simpleFactory(
    create: () -> T
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
}
