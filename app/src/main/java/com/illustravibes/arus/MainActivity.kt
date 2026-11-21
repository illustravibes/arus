package com.illustravibes.arus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.illustravibes.arus.core.designsystem.ArusTheme
import com.illustravibes.arus.core.navigation.ArusAppShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArusTheme {
                ArusAppShell()
            }
        }
    }
}
