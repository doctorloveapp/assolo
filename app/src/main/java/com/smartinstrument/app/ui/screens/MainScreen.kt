package com.smartinstrument.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinstrument.app.audio.NativeAudioEngine
import com.smartinstrument.app.music.MusicalKey
import com.smartinstrument.app.music.Note
import com.smartinstrument.app.music.PentatonicScale
import com.smartinstrument.app.music.ScaleType
import com.smartinstrument.app.ui.components.*
import com.smartinstrument.app.ui.theme.AccentPink
import com.smartinstrument.app.ui.theme.DarkBackground
import com.smartinstrument.app.ui.theme.DarkSurface

/**
 * MainScreen - The main instrument playing screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    audioEngine: NativeAudioEngine,
    modifier: Modifier = Modifier
) {
    // State
    var currentKey by remember { mutableStateOf(MusicalKey(Note.C, ScaleType.MINOR)) }
    var detectedKey by remember { mutableStateOf<MusicalKey?>(null) }
    var numRows by remember { mutableIntStateOf(7) }
    var waveType by remember { mutableIntStateOf(NativeAudioEngine.WAVE_SAWTOOTH) }
    var synthVolume by remember { mutableFloatStateOf(0.8f) }
    var showSettings by remember { mutableStateOf(false) }
    var showNoteLabels by remember { mutableStateOf(true) }
    
    // Generate scale notes based on current key
    val scaleNotes = remember(currentKey, numRows) {
        PentatonicScale.generateGridFrequencies(currentKey, numRows, baseOctave = 3)
    }
    
    // Update audio engine when settings change
    LaunchedEffect(waveType) {
        audioEngine.setWaveType(waveType)
    }
    
    LaunchedEffect(synthVolume) {
        audioEngine.setMasterVolume(synthVolume)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Assolo",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentKey.displayName} Pentatonic",
                            fontSize = 12.sp,
                            color = AccentPink
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = if (showSettings) Icons.Default.Close else Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Settings panel (collapsible)
            if (showSettings) {
                SettingsPanel(
                    currentKey = currentKey,
                    detectedKey = detectedKey,
                    onKeySelected = { currentKey = it },
                    numRows = numRows,
                    onRowCountSelected = { numRows = it },
                    waveType = waveType,
                    onWaveTypeSelected = { waveType = it },
                    synthVolume = synthVolume,
                    onSynthVolumeChanged = { synthVolume = it },
                    showNoteLabels = showNoteLabels,
                    onShowNoteLabelsChanged = { showNoteLabels = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Instrument Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                InstrumentGrid(
                    notes = scaleNotes,
                    onNoteOn = { voiceIndex, frequency ->
                        audioEngine.noteOn(voiceIndex, frequency)
                    },
                    onNoteOff = { voiceIndex ->
                        audioEngine.noteOff(voiceIndex)
                    },
                    showNoteLabels = showNoteLabels,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Settings panel with all configuration options
 */
@Composable
private fun SettingsPanel(
    currentKey: MusicalKey,
    detectedKey: MusicalKey?,
    onKeySelected: (MusicalKey) -> Unit,
    numRows: Int,
    onRowCountSelected: (Int) -> Unit,
    waveType: Int,
    onWaveTypeSelected: (Int) -> Unit,
    synthVolume: Float,
    onSynthVolumeChanged: (Float) -> Unit,
    showNoteLabels: Boolean,
    onShowNoteLabelsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Key Selection
        Text(
            text = "Musical Key",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        KeySelector(
            currentKey = currentKey,
            detectedKey = detectedKey,
            onKeySelected = onKeySelected
        )
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        // Wave Type
        Text(
            text = "Synth Wave",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        WaveTypeSelector(
            currentWaveType = waveType,
            onWaveTypeSelected = onWaveTypeSelected
        )
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        // Grid Rows
        RowCountSelector(
            currentCount = numRows,
            onCountSelected = onRowCountSelected
        )
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        // Volume
        VolumeSlider(
            label = "Synth Volume",
            value = synthVolume,
            onValueChange = onSynthVolumeChanged
        )
        
        // Show Labels Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Note Labels",
                color = Color.White,
                fontSize = 14.sp
            )
            Switch(
                checked = showNoteLabels,
                onCheckedChange = onShowNoteLabelsChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentPink,
                    checkedTrackColor = AccentPink.copy(alpha = 0.5f)
                )
            )
        }
    }
}
