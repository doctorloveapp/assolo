package com.smartinstrument.app.music

/**
 * MusicalKey - Represents a musical key with root note and scale type
 */
data class MusicalKey(
    val root: Note,
    val scaleType: ScaleType
) {
    val displayName: String
        get() = "${root.displayName} ${scaleType.displayName}"
    
    companion object {
        val DEFAULT = MusicalKey(Note.C, ScaleType.MINOR)
        
        fun fromString(keyString: String): MusicalKey? {
            val parts = keyString.trim().split(" ", limit = 2)
            if (parts.size != 2) return null
            
            val root = Note.fromString(parts[0]) ?: return null
            val scaleType = ScaleType.fromString(parts[1]) ?: return null
            
            return MusicalKey(root, scaleType)
        }
    }
}

/**
 * Note - Represents the 12 chromatic notes
 */
enum class Note(val semitone: Int, val displayName: String) {
    C(0, "C"),
    C_SHARP(1, "C#"),
    D(2, "D"),
    D_SHARP(3, "D#"),
    E(4, "E"),
    F(5, "F"),
    F_SHARP(6, "F#"),
    G(7, "G"),
    G_SHARP(8, "G#"),
    A(9, "A"),
    A_SHARP(10, "A#"),
    B(11, "B");
    
    companion object {
        fun fromString(str: String): Note? {
            val normalized = str.trim().uppercase()
                .replace("♯", "#")
                .replace("♭", "b")
            
            return when (normalized) {
                "C" -> C
                "C#", "DB" -> C_SHARP
                "D" -> D
                "D#", "EB" -> D_SHARP
                "E" -> E
                "F" -> F
                "F#", "GB" -> F_SHARP
                "G" -> G
                "G#", "AB" -> G_SHARP
                "A" -> A
                "A#", "BB" -> A_SHARP
                "B" -> B
                else -> null
            }
        }
        
        fun fromSemitone(semitone: Int): Note {
            val normalized = ((semitone % 12) + 12) % 12
            return entries.first { it.semitone == normalized }
        }
    }
}

/**
 * ScaleType - Defines the type of scale (Major, Minor, etc.)
 */
enum class ScaleType(val displayName: String) {
    MAJOR("Major"),
    MINOR("Minor");
    
    companion object {
        fun fromString(str: String): ScaleType? {
            return when (str.trim().lowercase()) {
                "major", "maj", "maggiore" -> MAJOR
                "minor", "min", "minore" -> MINOR
                else -> null
            }
        }
    }
}
