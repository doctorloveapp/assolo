package com.smartinstrument.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.smartinstrument.app.audio.NativeAudioEngine
import com.smartinstrument.app.ui.screens.MainScreen
import com.smartinstrument.app.ui.theme.DarkBackground
import com.smartinstrument.app.ui.theme.SmartInstrumentTheme

class MainActivity : ComponentActivity() {
    
    private val audioEngine = NativeAudioEngine()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize audio engine
        audioEngine.create()
        audioEngine.start()
        
        setContent {
            SmartInstrumentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainScreen(audioEngine = audioEngine)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        audioEngine.start()
    }
    
    override fun onPause() {
        super.onPause()
        audioEngine.allNotesOff()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioEngine.destroy()
    }
}
