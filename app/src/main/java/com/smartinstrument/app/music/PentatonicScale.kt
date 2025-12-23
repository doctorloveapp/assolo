package com.smartinstrument.app.music

import kotlin.math.pow

/**
 * PentatonicScale - Generates pentatonic scale frequencies for a given key
 * 
 * The pentatonic scale is a 5-note scale that sounds good over most chord progressions.
 * It's the safest scale for improvisation because it avoids dissonant intervals.
 * 
 * Major Pentatonic: 1, 2, 3, 5, 6 (intervals: 0, 2, 4, 7, 9 semitones)
 * Minor Pentatonic: 1, b3, 4, 5, b7 (intervals: 0, 3, 5, 7, 10 semitones)
 */
object PentatonicScale {
    
    // Semitone intervals from root for each scale type
    private val MAJOR_PENTATONIC_INTERVALS = intArrayOf(0, 2, 4, 7, 9)
    private val MINOR_PENTATONIC_INTERVALS = intArrayOf(0, 3, 5, 7, 10)
    
    // Reference frequency: A4 = 440 Hz
    private const val A4_FREQUENCY = 440.0f
    private const val A4_MIDI_NOTE = 69
    
    /**
     * Get the semitone intervals for a given scale type
     */
    fun getIntervals(scaleType: ScaleType): IntArray {
        return when (scaleType) {
            ScaleType.MAJOR -> MAJOR_PENTATONIC_INTERVALS
            ScaleType.MINOR -> MINOR_PENTATONIC_INTERVALS
        }
    }
    
    /**
     * Convert MIDI note number to frequency in Hz
     * Formula: f = 440 * 2^((n-69)/12)
     */
    fun midiToFrequency(midiNote: Int): Float {
        return A4_FREQUENCY * 2.0.pow((midiNote - A4_MIDI_NOTE) / 12.0).toFloat()
    }
    
    /**
     * Get the MIDI note number for a given note and octave
     * C4 (middle C) = MIDI 60
     */
    fun noteToMidi(note: Note, octave: Int): Int {
        return (octave + 1) * 12 + note.semitone
    }
    
    /**
     * Generate frequencies for a pentatonic scale grid
     * 
     * @param key The musical key (root note + scale type)
     * @param numRows Number of rows in the grid (typically 5 or 7)
     * @param baseOctave Starting octave (default 3 for bass, 4 for mid-range)
     * @return List of frequencies in Hz, from lowest (bottom) to highest (top)
     */
    fun generateGridFrequencies(
        key: MusicalKey,
        numRows: Int,
        baseOctave: Int = 3
    ): List<NoteInfo> {
        val intervals = getIntervals(key.scaleType)
        val notes = mutableListOf<NoteInfo>()
        
        var currentOctave = baseOctave
        var intervalIndex = 0
        
        repeat(numRows) { rowIndex ->
            val interval = intervals[intervalIndex]
            val noteSemitone = (key.root.semitone + interval) % 12
            val note = Note.fromSemitone(noteSemitone)
            
            // Check if we've wrapped around to a new octave
            if (intervalIndex > 0 && key.root.semitone + interval >= 12) {
                // We're in the next octave for this note
            }
            
            val midiNote = noteToMidi(note, currentOctave) + 
                          (if (key.root.semitone + interval >= 12) 0 else 0)
            
            val actualMidi = (currentOctave + 1) * 12 + key.root.semitone + interval
            val frequency = midiToFrequency(actualMidi)
            
            notes.add(
                NoteInfo(
                    note = note,
                    octave = actualMidi / 12 - 1,
                    frequency = frequency,
                    midiNote = actualMidi,
                    scaleDegree = intervalIndex + 1
                )
            )
            
            // Move to next interval
            intervalIndex++
            if (intervalIndex >= intervals.size) {
                intervalIndex = 0
                currentOctave++
            }
        }
        
        return notes
    }
    
    /**
     * Get note name with octave (e.g., "C4", "G#3")
     */
    fun getNoteDisplayName(note: Note, octave: Int): String {
        return "${note.displayName}$octave"
    }
}

/**
 * NoteInfo - Contains all information about a specific note in the scale
 */
data class NoteInfo(
    val note: Note,
    val octave: Int,
    val frequency: Float,
    val midiNote: Int,
    val scaleDegree: Int
) {
    val displayName: String
        get() = "${note.displayName}$octave"
    
    val frequencyDisplay: String
        get() = "${frequency.toInt()} Hz"
}
