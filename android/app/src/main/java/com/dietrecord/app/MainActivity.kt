package com.dietrecord.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dietrecord.app.core.ui.theme.DietRecordTheme
import com.dietrecord.app.navigation.AppNavHost

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
