package com.smartinstrument.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinstrument.app.music.NoteInfo
import com.smartinstrument.app.ui.theme.GridRowColors

/**
 * InstrumentGrid - The main playable instrument interface
 * 
 * A grid of horizontal rows where each row represents a note in the pentatonic scale.
 * Supports multitouch for playing chords. Optimized for lowest possible latency.
 */
@Composable
fun InstrumentGrid(
    notes: List<NoteInfo>,
    onNoteOn: (voiceIndex: Int, frequency: Float) -> Unit,
    onNoteOff: (voiceIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    showNoteLabels: Boolean = true
) {
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Track active touches: pointerId -> rowIndex
    val activeTouches = remember { mutableStateMapOf<Long, Int>() }
    
    // Map pointer IDs to voice indices (0-7)
    val pointerToVoice = remember { mutableStateMapOf<Long, Int>() }
    var nextVoiceIndex by remember { mutableIntStateOf(0) }
    
    // Calculate row height
    val rowHeight = if (notes.isNotEmpty() && gridSize.height > 0) {
        gridSize.height.toFloat() / notes.size
    } else 0f
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { gridSize = it }
            .pointerInput(notes) {
                awaitEachGesture {
                    // Wait for first touch
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    
                    // Handle first touch immediately for zero latency
                    handleTouchDown(
                        pointerId = firstDown.id.value,
                        position = firstDown.position,
                        rowHeight = rowHeight,
                        notes = notes,
                        activeTouches = activeTouches,
                        pointerToVoice = pointerToVoice,
                        nextVoiceIndex = nextVoiceIndex,
                        onNoteOn = onNoteOn,
                        onVoiceAssigned = { nextVoiceIndex = (nextVoiceIndex + 1) % 8 }
                    )
                    
                    // Continue tracking all pointers
                    do {
                        val event = awaitPointerEvent()
                        
                        event.changes.forEach { change ->
                            when {
                                change.pressed && !change.previousPressed -> {
                                    // New touch down
                                    handleTouchDown(
                                        pointerId = change.id.value,
                                        position = change.position,
                                        rowHeight = rowHeight,
                                        notes = notes,
                                        activeTouches = activeTouches,
                                        pointerToVoice = pointerToVoice,
                                        nextVoiceIndex = nextVoiceIndex,
                                        onNoteOn = onNoteOn,
                                        onVoiceAssigned = { nextVoiceIndex = (nextVoiceIndex + 1) % 8 }
                                    )
                                }
                                !change.pressed && change.previousPressed -> {
                                    // Touch up
                                    handleTouchUp(
                                        pointerId = change.id.value,
                                        activeTouches = activeTouches,
                                        pointerToVoice = pointerToVoice,
                                        onNoteOff = onNoteOff
                                    )
                                }
                                change.pressed -> {
                                    // Touch move - check if crossed to different row
                                    handleTouchMove(
                                        pointerId = change.id.value,
                                        position = change.position,
                                        rowHeight = rowHeight,
                                        notes = notes,
                                        activeTouches = activeTouches,
                                        pointerToVoice = pointerToVoice,
                                        onNoteOn = onNoteOn,
                                        onNoteOff = onNoteOff
                                    )
                                }
                            }
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                    
                    // All touches released
                    activeTouches.keys.toList().forEach { pointerId ->
                        handleTouchUp(
                            pointerId = pointerId,
                            activeTouches = activeTouches,
                            pointerToVoice = pointerToVoice,
                            onNoteOff = onNoteOff
                        )
                    }
                }
            }
    ) {
        // Draw the grid rows (reversed so bass is at bottom)
        Column(modifier = Modifier.fillMaxSize()) {
            notes.reversed().forEachIndexed { visualIndex, noteInfo ->
                val actualIndex = notes.size - 1 - visualIndex
                val isActive = activeTouches.values.contains(actualIndex)
                
                NoteRow(
                    noteInfo = noteInfo,
                    rowIndex = actualIndex,
                    totalRows = notes.size,
                    isActive = isActive,
                    showLabel = showNoteLabels,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Individual note row in the grid
 */
@Composable
private fun NoteRow(
    noteInfo: NoteInfo,
    rowIndex: Int,
    totalRows: Int,
    isActive: Boolean,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    // Get color based on row position
    val colorIndex = (rowIndex * (GridRowColors.size - 1)) / (totalRows - 1).coerceAtLeast(1)
    val baseColor = GridRowColors.getOrElse(colorIndex) { GridRowColors.first() }
    
    // Active state makes the row brighter
    val rowColor = if (isActive) {
        baseColor.copy(alpha = 1f)
    } else {
        baseColor.copy(alpha = 0.6f)
    }
    
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            rowColor.copy(alpha = if (isActive) 0.9f else 0.4f),
            rowColor,
            rowColor.copy(alpha = if (isActive) 0.9f else 0.4f)
        )
    )
    
    Box(
        modifier = modifier
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(gradientBrush),
        contentAlignment = Alignment.CenterStart
    ) {
        if (showLabel) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Note name
                Text(
                    text = noteInfo.displayName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
                
                // Frequency (smaller, right side)
                Text(
                    text = noteInfo.frequencyDisplay,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
        
        // Visual feedback for active state
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    }
}

// Helper functions for touch handling

private fun handleTouchDown(
    pointerId: Long,
    position: Offset,
    rowHeight: Float,
    notes: List<NoteInfo>,
    activeTouches: MutableMap<Long, Int>,
    pointerToVoice: MutableMap<Long, Int>,
    nextVoiceIndex: Int,
    onNoteOn: (voiceIndex: Int, frequency: Float) -> Unit,
    onVoiceAssigned: () -> Unit
) {
    if (rowHeight <= 0 || notes.isEmpty()) return
    
    // Calculate which row was touched (inverted because bass is at bottom)
    val touchedRow = ((notes.size - 1) - (position.y / rowHeight).toInt())
        .coerceIn(0, notes.lastIndex)
    
    // Assign a voice to this pointer
    val voiceIndex = nextVoiceIndex
    pointerToVoice[pointerId] = voiceIndex
    activeTouches[pointerId] = touchedRow
    
    // Trigger the note
    val noteInfo = notes[touchedRow]
    onNoteOn(voiceIndex, noteInfo.frequency)
    onVoiceAssigned()
}

private fun handleTouchUp(
    pointerId: Long,
    activeTouches: MutableMap<Long, Int>,
    pointerToVoice: MutableMap<Long, Int>,
    onNoteOff: (voiceIndex: Int) -> Unit
) {
    val voiceIndex = pointerToVoice[pointerId] ?: return
    
    // Release the note
    onNoteOff(voiceIndex)
    
    // Clean up
    activeTouches.remove(pointerId)
    pointerToVoice.remove(pointerId)
}

private fun handleTouchMove(
    pointerId: Long,
    position: Offset,
    rowHeight: Float,
    notes: List<NoteInfo>,
    activeTouches: MutableMap<Long, Int>,
    pointerToVoice: MutableMap<Long, Int>,
    onNoteOn: (voiceIndex: Int, frequency: Float) -> Unit,
    onNoteOff: (voiceIndex: Int) -> Unit
) {
    if (rowHeight <= 0 || notes.isEmpty()) return
    
    val previousRow = activeTouches[pointerId] ?: return
    val voiceIndex = pointerToVoice[pointerId] ?: return
    
    // Calculate current row
    val currentRow = ((notes.size - 1) - (position.y / rowHeight).toInt())
        .coerceIn(0, notes.lastIndex)
    
    // If moved to a different row, update the note
    if (currentRow != previousRow) {
        activeTouches[pointerId] = currentRow
        
        // Note off for old note, note on for new note (same voice for glide effect)
        val newNoteInfo = notes[currentRow]
        onNoteOn(voiceIndex, newNoteInfo.frequency)
    }
}
