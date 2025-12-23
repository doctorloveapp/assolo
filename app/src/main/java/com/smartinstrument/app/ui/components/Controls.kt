package com.smartinstrument.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * Double-tap on Guitar opens settings dialog
 */
@Composable
fun WaveTypeSelector(
    currentWaveType: Int,
    onWaveTypeSelected: (Int) -> Unit,
    guitarParams: GuitarParams,
    onGuitarParamsChanged: (GuitarParams) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGuitarDialog by remember { mutableStateOf(false) }
    
    // Guitar first, then the other instruments
    val waveTypes = listOf(
        4 to "⚡ Guitar",
        0 to "🎹 Organ",
        1 to "🎷 Synth",
        2 to "⬛ Square",
        3 to "🎸 Bass"
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
                    if (type == 4 && currentWaveType == 4) {
                        // Double tap on Guitar - show settings
                        showGuitarDialog = true
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
    
    // Guitar Settings Dialog
    if (showGuitarDialog) {
        GuitarSettingsDialog(
            params = guitarParams,
            onParamsChanged = onGuitarParamsChanged,
            onDismiss = { showGuitarDialog = false }
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
                    .width(280.dp)
            ) {
                Text(
                    text = "⚡ Guitar Settings",
                    color = AccentPink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
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
