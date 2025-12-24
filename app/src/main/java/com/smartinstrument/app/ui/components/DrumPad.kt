package com.smartinstrument.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartinstrument.app.ui.theme.AccentPink
import com.smartinstrument.app.ui.theme.DarkBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DrumPad - Electronic drum pad interface
 * 
 * Displays colored pads for different drum sounds:
 * - Kick (bass drum)
 * - Snare
 * - Hi-Hat (closed/open)
 * - Toms
 * - Crash cymbal
 * - Ride cymbal
 */

// Drum sound types with frequencies (used for synthesis)
enum class DrumSound(val displayName: String, val emoji: String, val baseFreq: Float, val color: Color) {
    KICK("Kick", "🦶", 60f, Color(0xFF8B0000)),        // Dark red
    SNARE("Snare", "🪘", 280f, Color(0xFFFF6B35)),     // Orange
    HIHAT_CLOSED("Hi-Hat", "🎩", 900f, Color(0xFFFFD93D)),  // Yellow - high freq
    HIHAT_OPEN("Open HH", "💫", 750f, Color(0xFFFFF176)),   // Light yellow - high freq
    TOM_HIGH("Hi Tom", "🔴", 180f, Color(0xFF4CAF50)),      // Green
    TOM_MID("Mid Tom", "🟠", 150f, Color(0xFF2196F3)),      // Blue
    TOM_LOW("Low Tom", "🟡", 100f, Color(0xFF9C27B0)),      // Purple - low tom
    CRASH("Crash", "💥", 500f, Color(0xFFE91E63)),          // Pink - cymbal range
    RIDE("Ride", "✨", 600f, Color(0xFF00BCD4))             // Cyan - cymbal range
}

@Composable
fun DrumPad(
    onDrumHit: (voiceIndex: Int, frequency: Float) -> Unit,
    onDrumRelease: (voiceIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track which pads are pressed
    val activePads = remember { mutableStateMapOf<DrumSound, Boolean>() }
    
    // Layout: 3x3 grid of drum pads
    val drumLayout = listOf(
        listOf(DrumSound.CRASH, DrumSound.HIHAT_CLOSED, DrumSound.RIDE),
        listOf(DrumSound.TOM_HIGH, DrumSound.SNARE, DrumSound.TOM_MID),
        listOf(DrumSound.TOM_LOW, DrumSound.KICK, DrumSound.HIHAT_OPEN)
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        drumLayout.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { colIndex, drum ->
                    val voiceIndex = rowIndex * 3 + colIndex
                    val isPressed = activePads[drum] == true
                    
                    DrumPadButton(
                        drum = drum,
                        isPressed = isPressed,
                        onPress = {
                            activePads[drum] = true
                            onDrumHit(voiceIndex, drum.baseFreq)
                        },
                        onRelease = {
                            activePads[drum] = false
                            onDrumRelease(voiceIndex)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun DrumPadButton(
    drum: DrumSound,
    isPressed: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var animatedPressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPressed || animatedPressed) 
                    drum.color 
                else 
                    drum.color.copy(alpha = 0.6f)
            )
            .scale(if (isPressed || animatedPressed) 0.95f else 1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        animatedPressed = true
                        onPress()
                        try {
                            // Wait for release
                            awaitRelease()
                        } finally {
                            // Quick animation for drum release
                            coroutineScope.launch {
                                delay(100)
                                animatedPressed = false
                            }
                            onRelease()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = drum.emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = drum.displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Highlight border when pressed
        if (isPressed || animatedPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    }
}
