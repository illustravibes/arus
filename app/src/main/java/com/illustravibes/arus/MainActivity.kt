package com.illustravibes.arus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.illustravibes.arus.core.designsystem.ArusTheme
import com.illustravibes.arus.core.navigation.ArusAppShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArusTheme {
                ArusAppShell()
            }
        }
    }
}
