package com.smartinstrument.app.music

import kotlin.math.pow

/**
 * PentatonicScale - Generates blues scale frequencies for a given key
 * 
 * The blues scale is a pentatonic scale with added "blue notes" - 
 * chromatic notes that add tension and expressiveness.
 * 
 * Major Blues: 1, 2, b3, 3, 5, 6 (intervals: 0, 2, 3, 4, 7, 9 semitones)
 * Minor Blues: 1, b3, 4, b5, 5, b7 (intervals: 0, 3, 5, 6, 7, 10 semitones)
 * 
 * The b5 (flatted fifth) is the classic "blue note" that gives blues its characteristic sound.
 */
object PentatonicScale {
    
    // Blues scale intervals (pentatonic + blue notes)
    // Minor blues: root, minor 3rd, 4th, flat 5th (blue note), 5th, minor 7th
    private val MINOR_BLUES_INTERVALS = intArrayOf(0, 3, 5, 6, 7, 10)
    
    // Major blues: root, 2nd, minor 3rd (blue note), major 3rd, 5th, 6th
    private val MAJOR_BLUES_INTERVALS = intArrayOf(0, 2, 3, 4, 7, 9)
    
    // Reference frequency: A4 = 440 Hz
    private const val A4_FREQUENCY = 440.0f
    private const val A4_MIDI_NOTE = 69
    
    /**
     * Get the semitone intervals for a given scale type (with blue notes)
     */
    fun getIntervals(scaleType: ScaleType): IntArray {
        return when (scaleType) {
            ScaleType.MAJOR -> MAJOR_BLUES_INTERVALS
            ScaleType.MINOR -> MINOR_BLUES_INTERVALS
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
     * Generate frequencies for a blues scale grid
     * 
     * @param key The musical key (root note + scale type)
     * @param numRows Number of rows in the grid
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
            
            val actualMidi = (currentOctave + 1) * 12 + key.root.semitone + interval
            val frequency = midiToFrequency(actualMidi)
            
            // Determine if this is a blue note
            val isBlueNote = when (key.scaleType) {
                ScaleType.MINOR -> interval == 6  // b5 is the blue note in minor
                ScaleType.MAJOR -> interval == 3  // b3 is the blue note in major
            }
            
            notes.add(
                NoteInfo(
                    note = note,
                    octave = actualMidi / 12 - 1,
                    frequency = frequency,
                    midiNote = actualMidi,
                    scaleDegree = intervalIndex + 1,
                    isBlueNote = isBlueNote
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
    val scaleDegree: Int,
    val isBlueNote: Boolean = false
) {
    val displayName: String
        get() = "${note.displayName}$octave"
    
    val frequencyDisplay: String
        get() = "${frequency.toInt()} Hz"
}
