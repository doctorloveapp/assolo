package com.smartinstrument.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.smartinstrument.app.ui.theme.BlueNoteColor
import com.smartinstrument.app.ui.theme.GridRowColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * InstrumentGrid - The main playable instrument interface with pitch bend and vibrato
 * 
 * A grid of horizontal rows where each row represents a note in the blues scale.
 * Supports multitouch for playing chords, horizontal drag for pitch bending,
 * and automatic vibrato after holding a note for 1 second.
 */
@Composable
fun InstrumentGrid(
    notes: List<NoteInfo>,
    onNoteOn: (voiceIndex: Int, frequency: Float) -> Unit,
    onNoteOff: (voiceIndex: Int) -> Unit,
    onPitchBend: (voiceIndex: Int, semitones: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    showNoteLabels: Boolean = true
) {
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()
    
    // Track active touches: pointerId -> TouchState
    data class TouchState(
        val rowIndex: Int,
        val voiceIndex: Int,
        val startX: Float,
        val startTime: Long,
        val currentBend: Float = 0f,
        val isVibrating: Boolean = false,
        val manualBend: Float = 0f  // Bend from finger movement
    )
    
    // Use key to force recomposition when notes change - fixes tap mismatch bug
    val notesKey = remember(notes) { notes.hashCode() }
    
    val activeTouches = remember(notesKey) { mutableStateMapOf<Long, TouchState>() }
    var nextVoiceIndex by remember { mutableIntStateOf(0) }
    
    // Vibrato state for each voice
    val vibratoPhases = remember { mutableStateMapOf<Int, Float>() }
    
    // Calculate row height - recalculate when notes or grid size changes
    val rowHeight by remember(notes.size, gridSize.height) {
        derivedStateOf {
            if (notes.isNotEmpty() && gridSize.height > 0) {
                gridSize.height.toFloat() / notes.size
            } else 0f
        }
    }
    
    // Bend sensitivity
    val bendSensitivity by remember(gridSize.width) {
        derivedStateOf {
            if (gridSize.width > 0) 8f / gridSize.width else 0f
        }
    }
    
    // Vibrato effect - runs continuously for active vibrating notes
    LaunchedEffect(activeTouches.keys.toSet()) {
        while (activeTouches.isNotEmpty()) {
            val currentTime = System.currentTimeMillis()
            
            activeTouches.forEach { (pointerId, state) ->
                val holdDuration = currentTime - state.startTime
                
                // Start vibrato after 700ms of holding
                if (holdDuration > 700 && !state.isVibrating) {
                    activeTouches[pointerId] = state.copy(isVibrating = true)
                }
                
                // Apply vibrato if active
                if (state.isVibrating) {
                    val phase = (vibratoPhases[state.voiceIndex] ?: 0f) + 0.405f  // 35% faster
                    vibratoPhases[state.voiceIndex] = phase
                    
                    // Vibrato: oscillates ±0.3 semitones at ~8Hz
                    val vibratoBend = sin(phase.toDouble()).toFloat() * 0.3f
                    val totalBend = (state.manualBend + vibratoBend).coerceIn(0f, 2.5f)
                    
                    if (kotlin.math.abs(totalBend - state.currentBend) > 0.02f) {
                        activeTouches[pointerId] = state.copy(currentBend = totalBend)
                        onPitchBend(state.voiceIndex, totalBend)
                    }
                }
            }
            
            delay(16) // ~60fps for smooth vibrato
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { gridSize = it }
            .pointerInput(notesKey, gridSize) {  // Key on notes AND gridSize to reset on changes
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    val currentRowHeight = rowHeight
                    
                    // Handle first touch
                    if (currentRowHeight > 0 && notes.isNotEmpty()) {
                        val touchedRow = ((notes.size - 1) - (firstDown.position.y / currentRowHeight).toInt())
                            .coerceIn(0, notes.lastIndex)
                        
                        val voiceIndex = nextVoiceIndex
                        nextVoiceIndex = (nextVoiceIndex + 1) % 8
                        
                        activeTouches[firstDown.id.value] = TouchState(
                            rowIndex = touchedRow,
                            voiceIndex = voiceIndex,
                            startX = firstDown.position.x,
                            startTime = System.currentTimeMillis()
                        )
                        
                        vibratoPhases[voiceIndex] = 0f
                        onNoteOn(voiceIndex, notes[touchedRow].frequency)
                    }
                    
                    // Continue tracking all pointers
                    do {
                        val event = awaitPointerEvent()
                        val currentRowHeightInLoop = rowHeight
                        
                        event.changes.forEach { change ->
                            val pointerId = change.id.value
                            
                            when {
                                // New touch down
                                change.pressed && !change.previousPressed -> {
                                    if (currentRowHeightInLoop > 0 && notes.isNotEmpty()) {
                                        val touchedRow = ((notes.size - 1) - (change.position.y / currentRowHeightInLoop).toInt())
                                            .coerceIn(0, notes.lastIndex)
                                        
                                        val voiceIndex = nextVoiceIndex
                                        nextVoiceIndex = (nextVoiceIndex + 1) % 8
                                        
                                        activeTouches[pointerId] = TouchState(
                                            rowIndex = touchedRow,
                                            voiceIndex = voiceIndex,
                                            startX = change.position.x,
                                            startTime = System.currentTimeMillis()
                                        )
                                        
                                        vibratoPhases[voiceIndex] = 0f
                                        onNoteOn(voiceIndex, notes[touchedRow].frequency)
                                    }
                                }
                                
                                // Touch up
                                !change.pressed && change.previousPressed -> {
                                    activeTouches[pointerId]?.let { state ->
                                        onPitchBend(state.voiceIndex, 0f)
                                        onNoteOff(state.voiceIndex)
                                        vibratoPhases.remove(state.voiceIndex)
                                    }
                                    activeTouches.remove(pointerId)
                                }
                                
                                // Touch move - handle pitch bend
                                change.pressed -> {
                                    activeTouches[pointerId]?.let { state ->
                                        val currentBendSensitivity = bendSensitivity
                                        
                                        // Calculate horizontal displacement for bend (only right direction)
                                        // Right swipe (positive deltaX) activates bend
                                        // Left swipe (negative deltaX) returns to zero
                                        val deltaX = change.position.x - state.startX
                                        val manualBend = if (deltaX > 0) {
                                            (deltaX * currentBendSensitivity).coerceIn(0f, 2f)
                                        } else {
                                            0f  // Left swipe returns to zero
                                        }
                                        
                                        // If manually bending, stop auto-vibrato
                                        val isManuallyBending = manualBend > 0.2f
                                        
                                        if (isManuallyBending) {
                                            // Manual bend takes over
                                            if (kotlin.math.abs(manualBend - state.manualBend) > 0.05f) {
                                                activeTouches[pointerId] = state.copy(
                                                    manualBend = manualBend,
                                                    currentBend = manualBend,
                                                    isVibrating = false
                                                )
                                                onPitchBend(state.voiceIndex, manualBend)
                                            }
                                        } else if (!state.isVibrating) {
                                            // Not bending and not vibrating yet
                                            activeTouches[pointerId] = state.copy(manualBend = 0f)
                                        }
                                        
                                        // Check if moved to a different row (vertical movement)
                                        val currentRow = ((notes.size - 1) - (change.position.y / currentRowHeightInLoop).toInt())
                                            .coerceIn(0, notes.lastIndex)
                                        
                                        if (currentRow != state.rowIndex) {
                                            // Reset bend and switch note
                                            onPitchBend(state.voiceIndex, 0f)
                                            activeTouches[pointerId] = TouchState(
                                                rowIndex = currentRow,
                                                voiceIndex = state.voiceIndex,
                                                startX = change.position.x,
                                                startTime = System.currentTimeMillis()  // Reset timer on row change
                                            )
                                            vibratoPhases[state.voiceIndex] = 0f
                                            onNoteOn(state.voiceIndex, notes[currentRow].frequency)
                                        }
                                    }
                                }
                            }
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                    
                    // All touches released - clean up
                    activeTouches.keys.toList().forEach { pointerId ->
                        activeTouches[pointerId]?.let { state ->
                            onPitchBend(state.voiceIndex, 0f)
                            onNoteOff(state.voiceIndex)
                            vibratoPhases.remove(state.voiceIndex)
                        }
                        activeTouches.remove(pointerId)
                    }
                }
            }
    ) {
        // Draw the grid rows (reversed so bass is at bottom)
        Column(modifier = Modifier.fillMaxSize()) {
            notes.reversed().forEachIndexed { visualIndex, noteInfo ->
                val actualIndex = notes.size - 1 - visualIndex
                val touchState = activeTouches.values.find { it.rowIndex == actualIndex }
                val isActive = touchState != null
                val currentBend = touchState?.currentBend ?: 0f
                val isVibrating = touchState?.isVibrating ?: false
                
                NoteRow(
                    noteInfo = noteInfo,
                    rowIndex = actualIndex,
                    totalRows = notes.size,
                    isActive = isActive,
                    pitchBend = currentBend,
                    isVibrating = isVibrating,
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
    pitchBend: Float,
    isVibrating: Boolean,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    // Get color based on row position
    val colorIndex = (rowIndex * (GridRowColors.size - 1)) / (totalRows - 1).coerceAtLeast(1)
    val baseColor = GridRowColors.getOrElse(colorIndex) { GridRowColors.first() }
    
    // Blue notes get a completely distinct gold color
    val noteColor = if (noteInfo.isBlueNote) {
        BlueNoteColor
    } else baseColor
    
    // Active state makes the row brighter
    val rowColor = if (isActive) {
        noteColor.copy(alpha = 1f)
    } else {
        noteColor.copy(alpha = 0.6f)
    }
    
    // Shift color based on pitch bend for visual feedback
    val bendIntensity = pitchBend / 2f
    val bendColor = if (isActive && bendIntensity > 0.05f) {
        Color(
            red = (rowColor.red + (1f - rowColor.red) * bendIntensity).coerceIn(0f, 1f),
            green = (rowColor.green * (1f - bendIntensity * 0.5f)).coerceIn(0f, 1f),
            blue = (rowColor.blue * (1f - bendIntensity * 0.7f)).coerceIn(0f, 1f),
            alpha = rowColor.alpha
        )
    } else rowColor
    
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            bendColor.copy(alpha = if (isActive) 0.9f else 0.4f),
            bendColor,
            bendColor.copy(alpha = if (isActive) 0.9f else 0.4f)
        )
    )
    
    Box(
        modifier = modifier
            .padding(vertical = 1.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(gradientBrush),
        contentAlignment = Alignment.CenterStart
    ) {
        if (showLabel) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Note name with indicators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blue note indicator
                    if (noteInfo.isBlueNote) {
                        Text(
                            text = "♭",
                            color = Color(0xFF64B5F6),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = noteInfo.displayName,
                        color = if (noteInfo.isBlueNote) Color(0xFF64B5F6) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                    )
                    
                    // Show vibrato indicator
                    if (isActive && isVibrating) {
                        Text(
                            text = " ∿",  // Vibrato wave symbol
                            color = Color.Yellow,
                            fontSize = 14.sp
                        )
                    }
                    
                    // Show bend indicator
                    if (isActive && pitchBend > 0.1f && !isVibrating) {
                        Text(
                            text = " ↗ +${"%.1f".format(pitchBend)}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Frequency
                Text(
                    text = noteInfo.frequencyDisplay,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
        
        // Visual feedback for active state
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.15f))
            )
        }
    }
}
