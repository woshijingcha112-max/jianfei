package com.dietrecord.app.core.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dietrecord.app.data.DietRecordRepository
import com.dietrecord.app.data.GoalRepository
import com.dietrecord.app.data.MockDietRecordRepository
import com.dietrecord.app.data.MockGoalRepository
import com.dietrecord.app.data.MockRecognitionRepository
import com.dietrecord.app.data.MockSessionStore
import com.dietrecord.app.data.RecognitionRepository
import com.dietrecord.app.feature.camera.CameraViewModel
import com.dietrecord.app.feature.goal.GoalViewModel
import com.dietrecord.app.feature.home.HomeViewModel
import com.dietrecord.app.feature.recognize.RecognizeResultViewModel

class AppContainer(
    dispatchers: AppDispatchers = AppDispatchers()
) {
    private val sessionStore = MockSessionStore()

    val goalRepository: GoalRepository = MockGoalRepository(
        store = sessionStore,
        dispatchers = dispatchers
    )

    val dietRecordRepository: DietRecordRepository = MockDietRecordRepository(
        store = sessionStore,
        dispatchers = dispatchers
    )

    val recognitionRepository: RecognitionRepository = MockRecognitionRepository(
        store = sessionStore,
        dispatchers = dispatchers
    )

    fun homeViewModelFactory(): ViewModelProvider.Factory = simpleFactory {
        HomeViewModel(
            goalRepository = goalRepository,
            dietRecordRepository = dietRecordRepository
        )
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
