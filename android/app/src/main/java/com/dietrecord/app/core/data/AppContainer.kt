package com.dietrecord.app.core.data

import android.content.res.AssetManager
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

class AppContainer(
    private val assets: AssetManager,
    private val baseUrl: String = ApiConfig.DEFAULT_BASE_URL,
    private val dispatchers: AppDispatchers = AppDispatchers()
) {
    private val appScope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val dietApiService = ApiConfig.createDietApiService(baseUrl)

    val goalRepository: GoalRepository = RealGoalRepository(
        api = dietApiService,
        scope = appScope,
        dispatchers = dispatchers
    )

    val recognitionRepository: RecognitionRepository = RealRecognitionRepository(
        api = dietApiService,
        assetManager = assets,
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
        GoalViewModel(goalRepository = goalRepository)
    }

    fun cameraViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        CameraViewModel(recognitionRepository = recognitionRepository)
    }

    fun recognizeResultViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        RecognizeResultViewModel(
            recognitionRepository = recognitionRepository,
            dietRecordRepository = dietRecordRepository
        )
    }
}

private fun <T : ViewModel> simpleFactory(
    create: () -> T
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
}
