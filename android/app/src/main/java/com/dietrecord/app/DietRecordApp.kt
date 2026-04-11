package com.dietrecord.app

import android.app.Application
import com.dietrecord.app.core.data.AppContainer

class DietRecordApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(assets = assets)
    }
}
