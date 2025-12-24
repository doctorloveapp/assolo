package com.smartinstrument.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartinstrument.app.music.MusicalKey
import com.smartinstrument.app.music.Note
import com.smartinstrument.app.music.ScaleType
import com.smartinstrument.app.ui.theme.AccentPink
import com.smartinstrument.app.ui.theme.DarkSurface

/**
 * Data class per i parametri della chitarra
 */
data class GuitarParams(
    val sustain: Float = 0.9f,
    val gain: Float = 0.85f,
    val distortion: Float = 0.8f,
    val reverb: Float = 0.6f
)

/**
 * Data class per i parametri dell'organo
 */
data class OrganParams(
    val drawbar: Float = 0.8f,      // Volume drawbar principale
    val leslie: Float = 0.5f,       // Velocità Leslie (slow/fast)
    val chorus: Float = 0.4f,       // Chorus/Vibrato
    val overdrive: Float = 0.3f     // Overdrive leggero
)

/**
 * Data class per i parametri del synth
 */
data class SynthParams(
    val attack: Float = 0.1f,       // Attack time
    val filter: Float = 0.7f,       // Filter cutoff
    val resonance: Float = 0.4f,    // Filter resonance
    val chorus: Float = 0.3f        // Chorus amount
)

/**
 * Data class per i parametri della batteria elettronica
 */
data class DrumParams(
    val kick: Float = 0.8f,         // Volume kick
    val snare: Float = 0.7f,        // Volume snare
    val hihat: Float = 0.6f,        // Volume hi-hat
    val tom: Float = 0.7f           // Volume toms
)

/**
 * Data class per i parametri del basso
 */
data class BassParams(
    val tone: Float = 0.6f,         // Tone (dark/bright)
    val attack: Float = 0.1f,       // Attack punch
    val sustain: Float = 0.7f,      // Sustain
    val compression: Float = 0.5f   // Compressione
)

/**
 * KeySelector - Dropdown component for selecting musical key
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeySelector(
    currentKey: MusicalKey,
    detectedKey: MusicalKey?,
    onKeySelected: (MusicalKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showNoteDropdown by remember { mutableStateOf(false) }
    var showScaleDropdown by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Detected key indicator
        if (detectedKey != null) {
            Text(
                text = "Detected: ${detectedKey.displayName}",
                color = AccentPink,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Root note selector
            ExposedDropdownMenuBox(
                expanded = showNoteDropdown,
                onExpandedChange = { showNoteDropdown = it }
            ) {
                OutlinedTextField(
                    value = currentKey.root.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Root") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showNoteDropdown)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .width(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentPink,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showNoteDropdown,
                    onDismissRequest = { showNoteDropdown = false }
                ) {
                    Note.entries.forEach { note ->
                        DropdownMenuItem(
                            text = { Text(note.displayName) },
                            onClick = {
                                onKeySelected(currentKey.copy(root = note))
                                showNoteDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Scale type selector
            ExposedDropdownMenuBox(
                expanded = showScaleDropdown,
                onExpandedChange = { showScaleDropdown = it }
            ) {
                OutlinedTextField(
                    value = currentKey.scaleType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Scale") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showScaleDropdown)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .width(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = AccentPink,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showScaleDropdown,
                    onDismissRequest = { showScaleDropdown = false }
                ) {
                    ScaleType.entries.forEach { scaleType ->
                        DropdownMenuItem(
                            text = { Text(scaleType.displayName) },
                            onClick = {
                                onKeySelected(currentKey.copy(scaleType = scaleType))
                                showScaleDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Use detected key button
            if (detectedKey != null && detectedKey != currentKey) {
                TextButton(
                    onClick = { onKeySelected(detectedKey) }
                ) {
                    Text(
                        text = "Use Detected",
                        color = AccentPink,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * WaveTypeSelector - Chips for selecting synthesizer wave type
 * Double-tap on instrument opens settings dialog
 */
@Composable
fun WaveTypeSelector(
    currentWaveType: Int,
    onWaveTypeSelected: (Int) -> Unit,
    guitarParams: GuitarParams,
    onGuitarParamsChanged: (GuitarParams) -> Unit,
    organParams: OrganParams = OrganParams(),
    onOrganParamsChanged: (OrganParams) -> Unit = {},
    synthParams: SynthParams = SynthParams(),
    onSynthParamsChanged: (SynthParams) -> Unit = {},
    drumParams: DrumParams = DrumParams(),
    onDrumParamsChanged: (DrumParams) -> Unit = {},
    bassParams: BassParams = BassParams(),
    onBassParamsChanged: (BassParams) -> Unit = {},
    onNoteOn: (Int, Float) -> Unit = { _, _ -> },
    onNoteOff: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showGuitarDialog by remember { mutableStateOf(false) }
    var showOrganDialog by remember { mutableStateOf(false) }
    var showSynthDialog by remember { mutableStateOf(false) }
    var showDrumsDialog by remember { mutableStateOf(false) }
    var showBassDialog by remember { mutableStateOf(false) }
    
    // Instruments in order: Organ, Guitar, Synth, Bass, Drums
    val waveTypes = listOf(
        0 to "🎹 Organ",
        4 to "⚡ Guitar",
        1 to "🎷 Synth",
        3 to "🎸 Bass",
        2 to "🥁 Drums"
    )
    
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        waveTypes.forEach { (type, name) ->
            FilterChip(
                selected = currentWaveType == type,
                onClick = { 
                    if (currentWaveType == type) {
                        // Double tap - show settings for current instrument
                        when (type) {
                            4 -> showGuitarDialog = true
                            0 -> showOrganDialog = true
                            1 -> showSynthDialog = true
                            2 -> showDrumsDialog = true
                            3 -> showBassDialog = true
                        }
                    } else {
                        onWaveTypeSelected(type)
                    }
                },
                label = { 
                    Text(
                        text = name,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPink,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
    
    // Settings Dialogs for each instrument
    if (showGuitarDialog) {
        GuitarSettingsDialog(
            params = guitarParams,
            onParamsChanged = onGuitarParamsChanged,
            onNoteOn = onNoteOn,
            onNoteOff = onNoteOff,
            onDismiss = { showGuitarDialog = false }
        )
    }
    
    if (showOrganDialog) {
        OrganSettingsDialog(
            params = organParams,
            onParamsChanged = onOrganParamsChanged,
            onNoteOn = onNoteOn,
            onNoteOff = onNoteOff,
            onDismiss = { showOrganDialog = false }
        )
    }
    
    if (showSynthDialog) {
        SynthSettingsDialog(
            params = synthParams,
            onParamsChanged = onSynthParamsChanged,
            onNoteOn = onNoteOn,
            onNoteOff = onNoteOff,
            onDismiss = { showSynthDialog = false }
        )
    }
    
    if (showDrumsDialog) {
        DrumSettingsDialog(
            params = drumParams,
            onParamsChanged = onDrumParamsChanged,
            onDismiss = { showDrumsDialog = false }
        )
    }
    
    if (showBassDialog) {
        BassSettingsDialog(
            params = bassParams,
            onParamsChanged = onBassParamsChanged,
            onNoteOn = onNoteOn,
            onNoteOff = onNoteOff,
            onDismiss = { showBassDialog = false }
        )
    }
}

/**
 * Dialog per configurare i parametri della chitarra elettrica
 */
@Composable
fun GuitarSettingsDialog(
    params: GuitarParams,
    onParamsChanged: (GuitarParams) -> Unit,
    onNoteOn: (Int, Float) -> Unit = { _, _ -> },
    onNoteOff: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    var sustain by remember { mutableFloatStateOf(params.sustain) }
    var gain by remember { mutableFloatStateOf(params.gain) }
    var distortion by remember { mutableFloatStateOf(params.distortion) }
    var reverb by remember { mutableFloatStateOf(params.reverb) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .width(300.dp)
            ) {
                Text(
                    text = "⚡ Guitar Settings",
                    color = AccentPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Test Keyboard for trying effects
                TestKeyboard(
                    onNoteOn = onNoteOn,
                    onNoteOff = onNoteOff,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sustain slider
                GuitarSlider(
                    label = "🎵 Sustain",
                    value = sustain,
                    onValueChange = { 
                        sustain = it
                        onParamsChanged(GuitarParams(sustain, gain, distortion, reverb))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Gain slider
                GuitarSlider(
                    label = "📢 Gain",
                    value = gain,
                    onValueChange = { 
                        gain = it
                        onParamsChanged(GuitarParams(sustain, gain, distortion, reverb))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Distortion slider
                GuitarSlider(
                    label = "🔥 Distortion",
                    value = distortion,
                    onValueChange = { 
                        distortion = it
                        onParamsChanged(GuitarParams(sustain, gain, distortion, reverb))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Reverb slider
                GuitarSlider(
                    label = "🏛️ Reverb",
                    value = reverb,
                    onValueChange = { 
                        reverb = it
                        onParamsChanged(GuitarParams(sustain, gain, distortion, reverb))
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun GuitarSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "${(value * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = AccentPink,
                activeTrackColor = AccentPink,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Dialog per configurare i parametri dell'organo
 */
@Composable
fun OrganSettingsDialog(
    params: OrganParams,
    onParamsChanged: (OrganParams) -> Unit,
    onNoteOn: (Int, Float) -> Unit = { _, _ -> },
    onNoteOff: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    var drawbar by remember { mutableFloatStateOf(params.drawbar) }
    var leslie by remember { mutableFloatStateOf(params.leslie) }
    var chorus by remember { mutableFloatStateOf(params.chorus) }
    var overdrive by remember { mutableFloatStateOf(params.overdrive) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .width(300.dp)
            ) {
                Text(
                    text = "🎹 Organ Settings",
                    color = AccentPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Test Keyboard
                TestKeyboard(
                    onNoteOn = onNoteOn,
                    onNoteOff = onNoteOff,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GuitarSlider(
                    label = "🎚️ Drawbar",
                    value = drawbar,
                    onValueChange = { 
                        drawbar = it
                        onParamsChanged(OrganParams(drawbar, leslie, chorus, overdrive))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🌀 Leslie Speed",
                    value = leslie,
                    onValueChange = { 
                        leslie = it
                        onParamsChanged(OrganParams(drawbar, leslie, chorus, overdrive))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "✨ Chorus/Vibrato",
                    value = chorus,
                    onValueChange = { 
                        chorus = it
                        onParamsChanged(OrganParams(drawbar, leslie, chorus, overdrive))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🔊 Overdrive",
                    value = overdrive,
                    onValueChange = { 
                        overdrive = it
                        onParamsChanged(OrganParams(drawbar, leslie, chorus, overdrive))
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Dialog per configurare i parametri del synth
 */
@Composable
fun SynthSettingsDialog(
    params: SynthParams,
    onParamsChanged: (SynthParams) -> Unit,
    onNoteOn: (Int, Float) -> Unit = { _, _ -> },
    onNoteOff: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    var attack by remember { mutableFloatStateOf(params.attack) }
    var filter by remember { mutableFloatStateOf(params.filter) }
    var resonance by remember { mutableFloatStateOf(params.resonance) }
    var chorus by remember { mutableFloatStateOf(params.chorus) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .width(300.dp)
            ) {
                Text(
                    text = "🎷 Synth Settings",
                    color = AccentPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Test Keyboard
                TestKeyboard(
                    onNoteOn = onNoteOn,
                    onNoteOff = onNoteOff,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GuitarSlider(
                    label = "⚡ Attack",
                    value = attack,
                    onValueChange = { 
                        attack = it
                        onParamsChanged(SynthParams(attack, filter, resonance, chorus))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🎛️ Filter Cutoff",
                    value = filter,
                    onValueChange = { 
                        filter = it
                        onParamsChanged(SynthParams(attack, filter, resonance, chorus))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "〰️ Resonance",
                    value = resonance,
                    onValueChange = { 
                        resonance = it
                        onParamsChanged(SynthParams(attack, filter, resonance, chorus))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "✨ Chorus",
                    value = chorus,
                    onValueChange = { 
                        chorus = it
                        onParamsChanged(SynthParams(attack, filter, resonance, chorus))
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Dialog per configurare i parametri della batteria elettronica
 */
@Composable
fun DrumSettingsDialog(
    params: DrumParams,
    onParamsChanged: (DrumParams) -> Unit,
    onDismiss: () -> Unit
) {
    var kick by remember { mutableFloatStateOf(params.kick) }
    var snare by remember { mutableFloatStateOf(params.snare) }
    var hihat by remember { mutableFloatStateOf(params.hihat) }
    var tom by remember { mutableFloatStateOf(params.tom) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .width(280.dp)
            ) {
                Text(
                    text = "🥁 Drums Settings",
                    color = AccentPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                GuitarSlider(
                    label = "🦶 Kick",
                    value = kick,
                    onValueChange = { 
                        kick = it
                        onParamsChanged(DrumParams(kick, snare, hihat, tom))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🪘 Snare",
                    value = snare,
                    onValueChange = { 
                        snare = it
                        onParamsChanged(DrumParams(kick, snare, hihat, tom))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🎩 Hi-Hat",
                    value = hihat,
                    onValueChange = { 
                        hihat = it
                        onParamsChanged(DrumParams(kick, snare, hihat, tom))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🪘 Toms",
                    value = tom,
                    onValueChange = { 
                        tom = it
                        onParamsChanged(DrumParams(kick, snare, hihat, tom))
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Dialog per configurare i parametri del basso
 */
@Composable
fun BassSettingsDialog(
    params: BassParams,
    onParamsChanged: (BassParams) -> Unit,
    onNoteOn: (Int, Float) -> Unit = { _, _ -> },
    onNoteOff: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    var tone by remember { mutableFloatStateOf(params.tone) }
    var attack by remember { mutableFloatStateOf(params.attack) }
    var sustain by remember { mutableFloatStateOf(params.sustain) }
    var compression by remember { mutableFloatStateOf(params.compression) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .width(300.dp)
            ) {
                Text(
                    text = "🎸 Bass Settings",
                    color = AccentPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Test Keyboard
                TestKeyboard(
                    onNoteOn = onNoteOn,
                    onNoteOff = onNoteOff,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GuitarSlider(
                    label = "🎚️ Tone",
                    value = tone,
                    onValueChange = { 
                        tone = it
                        onParamsChanged(BassParams(tone, attack, sustain, compression))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "👊 Attack/Punch",
                    value = attack,
                    onValueChange = { 
                        attack = it
                        onParamsChanged(BassParams(tone, attack, sustain, compression))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🎵 Sustain",
                    value = sustain,
                    onValueChange = { 
                        sustain = it
                        onParamsChanged(BassParams(tone, attack, sustain, compression))
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                GuitarSlider(
                    label = "🗜️ Compression",
                    value = compression,
                    onValueChange = { 
                        compression = it
                        onParamsChanged(BassParams(tone, attack, sustain, compression))
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * VolumeSlider - Custom volume slider with label
 */
@Composable
fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "${(value * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = AccentPink,
                activeTrackColor = AccentPink,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * RowCountSelector - Selector for number of grid rows
 */
@Composable
fun RowCountSelector(
    currentCount: Int,
    onCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(10, 15, 18)  // Blues scale options
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rows:",
            color = Color.White,
            fontSize = 14.sp
        )
        
        options.forEach { count ->
            FilterChip(
                selected = currentCount == count,
                onClick = { onCountSelected(count) },
                label = { Text(count.toString()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentPink,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * TestKeyboard - 7 test note buttons for trying effects
 * Blues scale in A: A, C, D, Eb, E, G, A (octave)
 */
@Composable
fun TestKeyboard(
    onNoteOn: (Int, Float) -> Unit,
    onNoteOff: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Blues scale in A (220Hz base)
    val testNotes = listOf(
        "A" to 220.00f,
        "C" to 261.63f,
        "D" to 293.66f,
        "Eb" to 311.13f,
        "E" to 329.63f,
        "G" to 392.00f,
        "A'" to 440.00f
    )
    
    val activeNotes = remember { mutableStateMapOf<Int, Boolean>() }
    
    Column(modifier = modifier) {
        Text(
            text = "🎹 Prova suono",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            testNotes.forEachIndexed { index, (note, freq) ->
                val isPressed = activeNotes[index] == true
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isPressed) AccentPink else AccentPink.copy(alpha = 0.3f)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    activeNotes[index] = true
                                    onNoteOn(index, freq)
                                    try {
                                        awaitRelease()
                                    } finally {
                                        activeNotes[index] = false
                                        onNoteOff(index)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = note,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
