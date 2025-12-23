package com.smartinstrument.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.smartinstrument.app.audio.KeyDetector
import com.smartinstrument.app.audio.NativeAudioEngine
import com.smartinstrument.app.audio.TrackPlayer
import com.smartinstrument.app.music.MusicalKey
import com.smartinstrument.app.ui.screens.MainScreen
import com.smartinstrument.app.ui.theme.DarkBackground
import com.smartinstrument.app.ui.theme.SmartInstrumentTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val audioEngine = NativeAudioEngine()
    private lateinit var trackPlayer: TrackPlayer
    private lateinit var keyDetector: KeyDetector
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize audio engine
        audioEngine.create()
        audioEngine.start()
        
        // Initialize track player and key detector
        trackPlayer = TrackPlayer(this)
        keyDetector = KeyDetector(this)
        
        setContent {
            SmartInstrumentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainScreenWithPlayer(
                        audioEngine = audioEngine,
                        trackPlayer = trackPlayer,
                        keyDetector = keyDetector
                    )
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
        trackPlayer.release()
        audioEngine.destroy()
    }
}

@Composable
fun MainScreenWithPlayer(
    audioEngine: NativeAudioEngine,
    trackPlayer: TrackPlayer,
    keyDetector: KeyDetector
) {
    val scope = rememberCoroutineScope()
    
    // Track state
    var trackUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var detectedKey by remember { mutableStateOf<MusicalKey?>(null) }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            trackUri = selectedUri
            trackPlayer.loadTrack(selectedUri)
            
            // Start key detection
            scope.launch {
                isAnalyzing = true
                val result = keyDetector.detectKey(selectedUri)
                detectedKey = result?.key
                isAnalyzing = false
            }
        }
    }
    
    // Update playback position periodically
    LaunchedEffect(Unit) {
        while (true) {
            trackPlayer.updatePosition()
            delay(100)
        }
    }
    
    MainScreen(
        audioEngine = audioEngine,
        trackPlayer = trackPlayer,
        detectedKey = detectedKey,
        isAnalyzing = isAnalyzing,
        onSelectTrack = {
            filePickerLauncher.launch(arrayOf("audio/*"))
        },
        onAnalyzeAssetTrack = { assetFileName ->
            // Analyze built-in track
            scope.launch {
                isAnalyzing = true
                // Create asset URI for analysis
                val assetUri = Uri.parse("asset:///tracks/$assetFileName")
                val result = keyDetector.detectKey(assetUri)
                detectedKey = result?.key
                isAnalyzing = false
            }
        }
    )
}
