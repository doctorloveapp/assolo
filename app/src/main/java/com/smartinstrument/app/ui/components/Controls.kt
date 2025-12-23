package com.smartinstrument.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.smartinstrument.app.music.MusicalKey
import com.smartinstrument.app.music.Note
import com.smartinstrument.app.music.ScaleType
import com.smartinstrument.app.ui.theme.AccentPink
import com.smartinstrument.app.ui.theme.DarkSurface

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
 */
@Composable
fun WaveTypeSelector(
    currentWaveType: Int,
    onWaveTypeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val waveTypes = listOf(
        0 to "Sine",
        1 to "Saw",
        2 to "Square",
        3 to "Triangle"
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        waveTypes.forEach { (type, name) ->
            FilterChip(
                selected = currentWaveType == type,
                onClick = { onWaveTypeSelected(type) },
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
