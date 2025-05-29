package com.tecsup.blinkcare

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.tecsup.blinkcare.ui.navigation.MainScaffold
import com.tecsup.blinkcare.ui.theme.BlinkCareTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlinkCareTheme {
                MainScaffold()
            }
        }
    }
}