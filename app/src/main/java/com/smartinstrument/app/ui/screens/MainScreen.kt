package com.smartinstrument.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
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
import kotlinx.coroutines.launch

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
    onAnalyzeAssetTrack: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // State
    var currentKey by remember { mutableStateOf(MusicalKey(Note.C, ScaleType.MINOR)) }
    var numRows by remember { mutableIntStateOf(18) }  // Default 18 rows
    var waveType by remember { mutableIntStateOf(NativeAudioEngine.WAVE_SINE) }  // Default: Organ
    var synthVolume by remember { mutableFloatStateOf(0.5f) }   // Synth al 50%
    var trackVolume by remember { mutableFloatStateOf(0.8f) }   // Track all'80%
    var showSettings by remember { mutableStateOf(true) }  // Settings open by default
    var showNoteLabels by remember { mutableStateOf(true) }
    var showPlayer by remember { mutableStateOf(true) }
    var guitarParams by remember { mutableStateOf(GuitarParams()) }
    var wahEnabled by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }
    
    // Track player state
    val isPlaying by trackPlayer.isPlaying.collectAsState()
    val isTrackLoaded by trackPlayer.isTrackLoaded.collectAsState()
    val trackName by trackPlayer.trackName.collectAsState()
    val currentPosition by trackPlayer.currentPosition.collectAsState()
    val duration by trackPlayer.duration.collectAsState()
    
    // Auto-hide panels when playback starts
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            showSettings = false
            showPlayer = false
        }
    }
    
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
    
    // Update guitar parameters
    LaunchedEffect(guitarParams) {
        audioEngine.setGuitarParams(
            guitarParams.sustain,
            guitarParams.gain,
            guitarParams.distortion,
            guitarParams.reverb
        )
    }
    
    // Update wah pedal
    LaunchedEffect(wahEnabled) {
        audioEngine.setWahEnabled(wahEnabled)
    }
    
    // Instructions Dialog
    if (showInstructions) {
        InstructionsDialog(onDismiss = { showInstructions = false })
    }
    
    // State for new dialogs
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // Credits Dialog
    if (showCreditsDialog) {
        CreditsDialog(onDismiss = { showCreditsDialog = false })
    }
    
    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    
    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentVersion = "3.7.1",
                onInstructions = {
                    scope.launch { drawerState.close() }
                    showInstructions = true
                },
                onInstruments = {
                    scope.launch { drawerState.close() }
                    showSettings = true
                },
                onTracks = {
                    scope.launch { drawerState.close() }
                    showPlayer = true
                },
                onPrivacyPolicy = {
                    scope.launch { drawerState.close() }
                    showPrivacyDialog = true
                },
                onContact = {
                    scope.launch { drawerState.close() }
                    sendEmail(context, "doctorloveapp@gmail.com")
                },
                onRateApp = {
                    scope.launch { drawerState.close() }
                    openPlayStore(context)
                },
                onShare = {
                    scope.launch { drawerState.close() }
                    shareApp(context)
                },
                onLanguage = {
                    scope.launch { drawerState.close() }
                    showLanguageDialog = true
                },
                onCredits = {
                    scope.launch { drawerState.close() }
                    showCreditsDialog = true
                }
            )
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
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
                    // Wah pedal controls (only visible when Guitar is selected)
                    if (waveType == NativeAudioEngine.WAVE_GUITAR) {
                        // Wah Pedal Control (only when wah is enabled)
                        if (wahEnabled) {
                            WahPedalControl(
                                onPositionChange = { position ->
                                    audioEngine.setWahPosition(position)
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        
                        // Main WAH on/off button (to the right of pedal)
                        FilterChip(
                            selected = wahEnabled,
                            onClick = { 
                                wahEnabled = !wahEnabled
                            },
                            label = { 
                                Text(
                                    text = "WAH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentPink,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.2f),
                                labelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    // Toggle Player visibility
                    IconButton(onClick = { showPlayer = !showPlayer }) {
                        Icon(
                            imageVector = if (showPlayer) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showPlayer) "Nascondi Player" else "Mostra Player"
                        )
                    }
                    // Toggle Settings visibility
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
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
                        builtInTracks = trackPlayer.getBuiltInTracks(),
                        onSelectTrack = onSelectTrack,
                        onSelectBuiltInTrack = { fileName ->
                            trackPlayer.loadAssetTrack(fileName)
                            onAnalyzeAssetTrack(fileName)
                        },
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
                        guitarParams = guitarParams,
                        onGuitarParamsChanged = { guitarParams = it },
                        showNoteLabels = showNoteLabels,
                        onShowNoteLabelsChanged = { showNoteLabels = it },
                        onNoteOn = { voiceIndex, frequency ->
                            audioEngine.noteOn(voiceIndex, frequency)
                        },
                        onNoteOff = { voiceIndex ->
                            audioEngine.noteOff(voiceIndex)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            // Instrument Grid or Drum Pad - takes at least 60% of remaining space
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (waveType == NativeAudioEngine.WAVE_DRUMS) {
                    // Show drum pads for drums
                    DrumPad(
                        onDrumHit = { voiceIndex, frequency ->
                            audioEngine.noteOn(voiceIndex, frequency)
                        },
                        onDrumRelease = { voiceIndex ->
                            audioEngine.noteOff(voiceIndex)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Show normal instrument grid
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
        }
        }  // Close background Box
    }
    }  // Close ModalNavigationDrawer
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
    builtInTracks: List<String>,
    onSelectTrack: () -> Unit,
    onSelectBuiltInTrack: (String) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onTrackVolumeChange: (Float) -> Unit,
    onSynthVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTrackMenu by remember { mutableStateOf(false) }
    
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
            // Track Selection with Dropdown Menu
            Box {
                Button(
                    onClick = { showTrackMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPink
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Brano")
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showTrackMenu,
                    onDismissRequest = { showTrackMenu = false }
                ) {
                    // Built-in tracks section - grouped by genre
                    if (builtInTracks.isNotEmpty()) {
                        // Define colors for each genre
                        val bluesColor = Color(0xFF1E88E5)  // Blue
                        val rockColor = Color(0xFFE53935)   // Red
                        val metalColor = Color(0xFF616161)  // Metal gray
                        
                        // Group tracks by genre
                        val bluesTracks = builtInTracks.filter { 
                            it.lowercase().contains("blues") 
                        }.sortedBy { it }
                        val rockTracks = builtInTracks.filter { 
                            it.lowercase().contains("rock") 
                        }.sortedBy { it }
                        val metalTracks = builtInTracks.filter { 
                            it.lowercase().contains("metal") 
                        }.sortedBy { it }
                        
                        // Blues Section
                        if (bluesTracks.isNotEmpty()) {
                            Text(
                                text = "🎷 Blues",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = bluesColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            bluesTracks.forEach { track ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = track.removeSuffix(".mp3"),
                                            fontSize = 14.sp,
                                            color = bluesColor
                                        )
                                    },
                                    onClick = {
                                        onSelectBuiltInTrack(track)
                                        showTrackMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = bluesColor
                                        )
                                    }
                                )
                            }
                        }
                        
                        // Rock Section
                        if (rockTracks.isNotEmpty()) {
                            Text(
                                text = "🎸 Rock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = rockColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            rockTracks.forEach { track ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = track.removeSuffix(".mp3"),
                                            fontSize = 14.sp,
                                            color = rockColor
                                        )
                                    },
                                    onClick = {
                                        onSelectBuiltInTrack(track)
                                        showTrackMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = rockColor
                                        )
                                    }
                                )
                            }
                        }
                        
                        // Metal Section
                        if (metalTracks.isNotEmpty()) {
                            Text(
                                text = "🤘 Metal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = metalColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            metalTracks.forEach { track ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = track.removeSuffix(".mp3"),
                                            fontSize = 14.sp,
                                            color = metalColor
                                        )
                                    },
                                    onClick = {
                                        onSelectBuiltInTrack(track)
                                        showTrackMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = metalColor
                                        )
                                    }
                                )
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                    
                    // Load from file option
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = "📁 Carica da File...",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        },
                        onClick = {
                            onSelectTrack()
                            showTrackMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    )
                }
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
    guitarParams: GuitarParams,
    onGuitarParamsChanged: (GuitarParams) -> Unit,
    showNoteLabels: Boolean,
    onShowNoteLabelsChanged: (Boolean) -> Unit,
    onNoteOn: (Int, Float) -> Unit = { _, _ -> },
    onNoteOff: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .heightIn(max = 280.dp)  // Altezza massima per evitare sovrapposizione
            .verticalScroll(rememberScrollState())
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
        
        // Instrument Type
        Text(
            text = "🎹 Strumento",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        WaveTypeSelector(
            currentWaveType = waveType,
            onWaveTypeSelected = onWaveTypeSelected,
            guitarParams = guitarParams,
            onGuitarParamsChanged = onGuitarParamsChanged,
            onNoteOn = onNoteOn,
            onNoteOff = onNoteOff
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

/**
 * WahPedalControl - A custom wah pedal control
 * Swipe left for heel (low freq), swipe right for toe (high freq)
 * The pedal rocks back and forth following the finger movement
 */
@Composable
fun WahPedalControl(
    onPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var pedalPosition by remember { mutableFloatStateOf(0.5f) } // 0 = heel, 1 = toe
    var controlWidth by remember { mutableIntStateOf(0) }
    
    // Animated rotation for the pedal tilt effect
    val pedalTilt by animateFloatAsState(
        targetValue = (pedalPosition - 0.5f) * 20f, // -10 to +10 degrees
        animationSpec = tween(durationMillis = 50),
        label = "pedalTilt"
    )
    
    Box(
        modifier = modifier
            .width(80.dp)
            .height(40.dp)
            .onSizeChanged { size -> controlWidth = size.width }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        // Set initial position based on tap location
                        if (controlWidth > 0) {
                            pedalPosition = (offset.x / controlWidth).coerceIn(0f, 1f)
                            onPositionChange(pedalPosition)
                        }
                    },
                    onDragEnd = { },
                    onDragCancel = { },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        if (controlWidth > 0) {
                            pedalPosition = (change.position.x / controlWidth).coerceIn(0f, 1f)
                            onPositionChange(pedalPosition)
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(6.dp))
            .shadow(4.dp, RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A4A4A), // Dark gray top
                        Color(0xFF2A2A2A), // Darker bottom
                        Color(0xFF1A1A1A)  // Almost black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pedal body with tilt animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .graphicsLayer {
                    rotationZ = pedalTilt
                }
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF505050),
                            Color(0xFF606060),
                            Color(0xFF505050)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Rubber grip lines on the pedal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(28.dp)
                            .background(Color(0xFF3A3A3A))
                    )
                }
            }
            
            // Position indicator dot
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = ((pedalPosition - 0.5f) * 50).dp)
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AccentPink)
            )
        }
        
        // Chrome trim at top (hinge area)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFAAAAAA),
                            Color(0xFF888888)
                        )
                    )
                )
        )
        
        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 4.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "▼",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "▲",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * App Drawer Content - Main navigation menu
 */
@Composable
fun AppDrawerContent(
    currentVersion: String,
    onInstructions: () -> Unit,
    onInstruments: () -> Unit,
    onTracks: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onContact: () -> Unit,
    onRateApp: () -> Unit,
    onShare: () -> Unit,
    onLanguage: () -> Unit,
    onCredits: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = DarkSurface,
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentPink, AccentPink.copy(alpha = 0.7f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "🎸 Assolo",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Smart Blues Instrument",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Menu Items
            DrawerMenuItem(
                icon = Icons.Outlined.Info,
                title = "📖 Istruzioni",
                onClick = onInstructions
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Settings,
                title = "🎹 Scelta Strumenti",
                onClick = onInstruments
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.PlayArrow,
                title = "🎵 Brani",
                onClick = onTracks
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.1f)
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Lock,
                title = "🔒 Privacy Policy",
                onClick = onPrivacyPolicy
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Email,
                title = "✉️ Contattaci",
                onClick = onContact
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Star,
                title = "⭐ Valuta App",
                onClick = onRateApp
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Share,
                title = "📤 Condividi",
                onClick = onShare
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.1f)
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Settings,
                title = "🌐 Lingua / Language",
                onClick = onLanguage
            )
            
            DrawerMenuItem(
                icon = Icons.Outlined.Info,
                title = "📜 Crediti & Licenze",
                onClick = onCredits
            )
            
            // Version at bottom
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Versione $currentVersion",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(16.dp)
            )
            
            Text(
                text = "© 2025 Doctor Love App",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Single menu item in the drawer
 */
@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Instructions Dialog
 */
@Composable
fun InstructionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "📖 Come Usare Assolo",
                color = AccentPink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InstructionItem(
                    emoji = "1️⃣",
                    title = "Carica un Brano",
                    description = "Scegli un brano incluso o carica un MP3 dal tuo dispositivo"
                )
                InstructionItem(
                    emoji = "2️⃣",
                    title = "Analisi Automatica",
                    description = "Assolo rileva automaticamente la tonalità del brano"
                )
                InstructionItem(
                    emoji = "3️⃣",
                    title = "Premi Play",
                    description = "Avvia la riproduzione - l'interfaccia si semplifica per suonare"
                )
                InstructionItem(
                    emoji = "4️⃣",
                    title = "Suona la Griglia",
                    description = "Tocca le note - sono tutte nella scala giusta, impossibile sbagliare!"
                )
                InstructionItem(
                    emoji = "🎸",
                    title = "Bend e Vibrato",
                    description = "Scorri orizzontalmente per il bend. Tieni premuto per il vibrato automatico"
                )
                InstructionItem(
                    emoji = "🎹",
                    title = "Cambia Strumento",
                    description = "Scegli tra Guitar, Organ, Synth, Square e Bass dal pannello impostazioni"
                )
                InstructionItem(
                    emoji = "⚙️",
                    title = "Impostazioni Strumento",
                    description = "Tocca DUE VOLTE l'icona dello strumento selezionato per aprire le impostazioni avanzate (effetti, tono, ecc.)"
                )
                InstructionItem(
                    emoji = "🎸",
                    title = "Wah Pedal",
                    description = "Con la chitarra, attiva il WAH e scorri il pedale per l'effetto wah"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Ho Capito!", color = AccentPink)
            }
        }
    )
}

@Composable
fun InstructionItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = emoji,
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

// Helper functions for drawer actions
private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error silently
    }
}

private fun sendEmail(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Assolo - Feedback")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error silently
    }
}

private fun openPlayStore(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.smartinstrument.app"))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to browser
        openUrl(context, "https://play.google.com/store/apps/details?id=com.smartinstrument.app")
    }
}

private fun shareApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Assolo - Smart Blues Instrument")
            putExtra(Intent.EXTRA_TEXT, "🎸 Assolo - Suona il blues come un professionista!\n\nScaricalo gratis: https://play.google.com/store/apps/details?id=com.smartinstrument.app")
        }
        context.startActivity(Intent.createChooser(intent, "Condividi Assolo"))
    } catch (e: Exception) {
        // Handle error silently
    }
}

/**
 * Credits & Licenses Dialog
 */
@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "📜 Crediti & Licenze",
                color = AccentPink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App info
                Text(
                    text = "🎸 Assolo v3.0.1",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Smart Blues Instrument\n© 2025 Doctor Love App",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                
                // Developer
                CreditItem(
                    title = "👨‍💻 Sviluppatore",
                    description = "Alessandro Giannetti"
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                
                // Libraries
                Text(
                    text = "📚 Librerie Open Source",
                    color = AccentPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                CreditItem(
                    title = "Google Oboe",
                    description = "Audio engine ad alta performance\nApache License 2.0"
                )
                
                CreditItem(
                    title = "Media3 ExoPlayer",
                    description = "Riproduzione audio professionale\nApache License 2.0"
                )
                
                CreditItem(
                    title = "Jetpack Compose",
                    description = "Modern UI toolkit\nApache License 2.0"
                )
                
                CreditItem(
                    title = "Kotlin",
                    description = "Linguaggio di programmazione\nApache License 2.0"
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                
                // Inspiration
                Text(
                    text = "🎵 Ispirazione",
                    color = AccentPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                CreditItem(
                    title = "Organ",
                    description = "Ispirazione per il suono Organ"
                )
                
                CreditItem(
                    title = "Wah Pedal",
                    description = "Effetto classico per chitarra"
                )
                
                CreditItem(
                    title = "Electric Bass",
                    description = "Sintesi basso elettrico"
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                
                // License
                Text(
                    text = "📄 Licenza",
                    color = AccentPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                
                Text(
                    text = "Assolo è rilasciato sotto licenza MIT.\nIl codice sorgente è disponibile su GitHub.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                
                // GitHub link
                TextButton(
                    onClick = { openUrl(context, "https://github.com/doctorloveapp/assolo") }
                ) {
                    Text(
                        text = "🔗 Visita GitHub Repository",
                        color = AccentPink,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi", color = AccentPink)
            }
        }
    )
}

@Composable
fun CreditItem(
    title: String,
    description: String
) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

/**
 * Privacy Policy Dialog - Shows policy directly without opening browser
 */
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "🔒 Privacy Policy",
                color = AccentPink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ultimo aggiornamento: Dicembre 2025",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                
                PolicySection(
                    title = "📱 Raccolta Dati",
                    content = "Assolo NON raccoglie alcun dato personale.\n\n" +
                            "• ❌ Nessun account richiesto\n" +
                            "• ❌ Nessun tracciamento\n" +
                            "• ❌ Nessuna pubblicità\n" +
                            "• ❌ Nessuna condivisione dati"
                )
                
                PolicySection(
                    title = "💾 Dati Locali",
                    content = "Tutti i dati restano sul tuo dispositivo:\n\n" +
                            "• Preferenze audio\n" +
                            "• Impostazioni strumenti\n" +
                            "• File audio caricati\n\n" +
                            "Nulla viene inviato a server esterni."
                )
                
                PolicySection(
                    title = "🎵 File Audio",
                    content = "I brani che carichi vengono elaborati localmente per l'analisi della tonalità. " +
                            "Nessun audio viene mai trasmesso online."
                )
                
                PolicySection(
                    title = "📋 Permessi",
                    content = "• Storage/Media: Solo per caricare i tuoi brani\n" +
                            "• Audio: Per riprodurre i suoni degli strumenti"
                )
                
                PolicySection(
                    title = "👶 Minori",
                    content = "L'app è sicura per utenti di tutte le età poiché non raccoglie alcun dato."
                )
                
                PolicySection(
                    title = "📧 Contatti",
                    content = "Per domande sulla privacy:\ndoctorloveapp@gmail.com"
                )
                
                Text(
                    text = "\n© 2025 Doctor Love App. Tutti i diritti riservati.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi", color = AccentPink)
            }
        }
    )
}

@Composable
fun PolicySection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            color = AccentPink,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
    }
}

/**
 * Language Selection Dialog
 */
@Composable
fun LanguageDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "🌐 Lingua / Language",
                color = AccentPink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Italian (current)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPink.copy(alpha = 0.2f))
                        .clickable { /* Already selected */ }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🇮🇹", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Italiano",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selezionato",
                            color = AccentPink,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // English (coming soon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🇬🇧", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "English",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Coming soon...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi", color = AccentPink)
            }
        }
    )
}
