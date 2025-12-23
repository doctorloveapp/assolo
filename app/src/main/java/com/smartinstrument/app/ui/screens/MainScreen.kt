package com.smartinstrument.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinstrument.app.R
import com.smartinstrument.app.audio.NativeAudioEngine
import com.smartinstrument.app.audio.TrackPlayer
import com.smartinstrument.app.music.MusicalKey
import com.smartinstrument.app.music.Note
import com.smartinstrument.app.music.PentatonicScale
import com.smartinstrument.app.music.ScaleType
import com.smartinstrument.app.ui.components.*
import com.smartinstrument.app.ui.theme.AccentPink
import com.smartinstrument.app.ui.theme.DarkBackground
import com.smartinstrument.app.ui.theme.DarkSurface

/**
 * MainScreen - The main instrument playing screen with track player
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    audioEngine: NativeAudioEngine,
    trackPlayer: TrackPlayer,
    detectedKey: MusicalKey?,
    isAnalyzing: Boolean,
    onSelectTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State
    var currentKey by remember { mutableStateOf(MusicalKey(Note.C, ScaleType.MINOR)) }
    var numRows by remember { mutableIntStateOf(18) }  // Default 18 rows
    var waveType by remember { mutableIntStateOf(NativeAudioEngine.WAVE_SAWTOOTH) }
    var synthVolume by remember { mutableFloatStateOf(0.5f) }   // Synth al 50%
    var trackVolume by remember { mutableFloatStateOf(0.8f) }   // Track all'80%
    var showSettings by remember { mutableStateOf(false) }
    var showNoteLabels by remember { mutableStateOf(true) }
    var showPlayer by remember { mutableStateOf(true) }
    
    // Track player state
    val isPlaying by trackPlayer.isPlaying.collectAsState()
    val isTrackLoaded by trackPlayer.isTrackLoaded.collectAsState()
    val trackName by trackPlayer.trackName.collectAsState()
    val currentPosition by trackPlayer.currentPosition.collectAsState()
    val duration by trackPlayer.duration.collectAsState()
    
    // Auto-apply detected key - always use MINOR for blues feel
    // If song is in E Major, we play E Minor pentatonic for that classic blues sound
    LaunchedEffect(detectedKey) {
        detectedKey?.let { detected ->
            // Always convert to minor for blues atmosphere
            currentKey = MusicalKey(detected.root, ScaleType.MINOR)
        }
    }
    
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
    
    LaunchedEffect(trackVolume) {
        trackPlayer.setVolume(trackVolume)
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
                    // Toggle Player visibility
                    IconButton(onClick = { showPlayer = !showPlayer }) {
                        Icon(
                            imageVector = if (showPlayer) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showPlayer) "Nascondi Player" else "Mostra Player"
                        )
                    }
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = if (showSettings) Icons.Default.Close else Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        // Background image
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.sfondo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Semi-transparent overlay for better readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
            
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
            // Track Player Controls (collapsible)
            if (showPlayer) {
                TrackPlayerPanel(
                    trackName = trackName,
                    isPlaying = isPlaying,
                    isTrackLoaded = isTrackLoaded,
                    isAnalyzing = isAnalyzing,
                    currentPosition = currentPosition,
                    duration = duration,
                    trackVolume = trackVolume,
                    synthVolume = synthVolume,
                    detectedKey = detectedKey,
                    onSelectTrack = onSelectTrack,
                    onPlayPause = { trackPlayer.togglePlayPause() },
                    onStop = { trackPlayer.stop() },
                    onSeek = { trackPlayer.seekTo(it) },
                    onTrackVolumeChange = { trackVolume = it },
                    onSynthVolumeChange = { synthVolume = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Settings panel (collapsible)
            if (showSettings) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                SettingsPanel(
                    currentKey = currentKey,
                    detectedKey = detectedKey,
                    onKeySelected = { currentKey = it },
                    numRows = numRows,
                    onRowCountSelected = { numRows = it },
                    waveType = waveType,
                    onWaveTypeSelected = { waveType = it },
                    showNoteLabels = showNoteLabels,
                    onShowNoteLabelsChanged = { showNoteLabels = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
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
                    onPitchBend = { voiceIndex, semitones ->
                        audioEngine.setPitchBend(voiceIndex, semitones)
                    },
                    showNoteLabels = showNoteLabels,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        }  // Close background Box
    }
}

/**
 * Track Player Panel with controls
 */
@Composable
private fun TrackPlayerPanel(
    trackName: String?,
    isPlaying: Boolean,
    isTrackLoaded: Boolean,
    isAnalyzing: Boolean,
    currentPosition: Long,
    duration: Long,
    trackVolume: Float,
    synthVolume: Float,
    detectedKey: MusicalKey?,
    onSelectTrack: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onTrackVolumeChange: (Float) -> Unit,
    onSynthVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Track Selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Load Track Button
            Button(
                onClick = onSelectTrack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPink
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Carica Brano")
            }
            
            // Track name or status
            Column(modifier = Modifier.weight(1f)) {
                if (isAnalyzing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AccentPink,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Analisi tonalità...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                } else if (trackName != null) {
                    Text(
                        text = trackName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (detectedKey != null) {
                        Text(
                            text = "Tonalità: ${detectedKey.displayName}",
                            color = AccentPink,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = "Nessun brano caricato",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Playback Controls
        if (isTrackLoaded) {
            // Progress Bar
            Column {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    onValueChange = { onSeek((it * duration).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPink,
                        activeTrackColor = AccentPink,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatTime(duration),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Play/Pause/Stop buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Button
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Play/Pause Button
                FilledIconButton(
                    onClick = onPlayPause,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AccentPink
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        
        // Volume Controls
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Track Volume
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎵 Brano",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Slider(
                    value = trackVolume,
                    onValueChange = onTrackVolumeChange,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Cyan,
                        activeTrackColor = Color.Cyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
            
            // Synth Volume
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎸 Synth",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Slider(
                    value = synthVolume,
                    onValueChange = onSynthVolumeChange,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentPink,
                        activeTrackColor = AccentPink,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

/**
 * Format milliseconds to MM:SS
 */
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/**
 * Settings panel with configuration options
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
            text = "Tonalità",
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
            text = "Tipo di Onda",
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
        
        // Show Labels Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mostra Note",
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
