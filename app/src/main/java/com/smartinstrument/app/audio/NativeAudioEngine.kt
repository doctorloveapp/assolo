package com.smartinstrument.app.audio

/**
 * NativeAudioEngine - Wrapper Kotlin per l'AudioEngine C++/Oboe
 * 
 * Gestisce la comunicazione JNI con il motore audio nativo.
 * Supporta fino a 8 voci simultanee per il multitouch.
 */
class NativeAudioEngine {
    
    companion object {
        init {
            System.loadLibrary("smartinstrument")
        }
        
        const val MAX_VOICES = 8
        
        // Tipi di strumento
        const val WAVE_SINE = 0      // Hammond B3 organ
        const val WAVE_SAWTOOTH = 1  // Synth lead
        const val WAVE_SQUARE = 2    // Retro square
        const val WAVE_BASS = 3      // Electric Bass with slap
        const val WAVE_GUITAR = 4    // Electric Guitar with distortion
    }
    
    private var isCreated = false
    private var isStarted = false
    
    /**
     * Inizializza l'engine audio nativo
     * @return true se l'inizializzazione ha successo
     */
    fun create(): Boolean {
        if (isCreated) return true
        isCreated = nativeCreate()
        return isCreated
    }
    
    /**
     * Avvia lo stream audio
     * @return true se l'avvio ha successo
     */
    fun start(): Boolean {
        if (!isCreated) {
            if (!create()) return false
        }
        if (isStarted) return true
        isStarted = nativeStart()
        return isStarted
    }
    
    /**
     * Ferma lo stream audio
     */
    fun stop() {
        if (isStarted) {
            nativeStop()
            isStarted = false
        }
    }
    
    /**
     * Distrugge l'engine e libera le risorse
     */
    fun destroy() {
        stop()
        if (isCreated) {
            nativeDestroy()
            isCreated = false
        }
    }
    
    /**
     * Attiva una nota
     * @param voiceIndex Indice della voce (0 to MAX_VOICES-1)
     * @param frequency Frequenza in Hz
     */
    fun noteOn(voiceIndex: Int, frequency: Float) {
        if (isStarted && voiceIndex in 0 until MAX_VOICES) {
            nativeNoteOn(voiceIndex, frequency)
        }
    }
    
    /**
     * Disattiva una nota
     * @param voiceIndex Indice della voce
     */
    fun noteOff(voiceIndex: Int) {
        if (isStarted && voiceIndex in 0 until MAX_VOICES) {
            nativeNoteOff(voiceIndex)
        }
    }
    
    /**
     * Disattiva tutte le note
     */
    fun allNotesOff() {
        if (isStarted) {
            nativeAllNotesOff()
        }
    }
    
    /**
     * Imposta il volume master dello strumento
     * @param volume Volume da 0.0 a 1.0
     */
    fun setMasterVolume(volume: Float) {
        if (isCreated) {
            nativeSetMasterVolume(volume.coerceIn(0f, 1f))
        }
    }
    
    /**
     * Imposta il tipo di forma d'onda
     * @param waveType Una delle costanti WAVE_*
     */
    fun setWaveType(waveType: Int) {
        if (isCreated) {
            nativeSetWaveType(waveType)
        }
    }
    
    /**
     * Imposta il pitch bend per una voce (bending della nota)
     * @param voiceIndex Indice della voce
     * @param semitones Quantità di bend in semitoni (tipicamente -2 a +2)
     */
    fun setPitchBend(voiceIndex: Int, semitones: Float) {
        if (isStarted && voiceIndex in 0 until MAX_VOICES) {
            nativeSetPitchBend(voiceIndex, semitones)
        }
    }
    
    /**
     * Imposta i parametri della chitarra elettrica
     * @param sustain 0.0-1.0 durata della nota
     * @param gain 0.0-1.0 volume/presenza  
     * @param distortion 0.0-1.0 quantità di distorsione
     * @param reverb 0.0-1.0 quantità di riverbero
     */
    fun setGuitarParams(sustain: Float, gain: Float, distortion: Float, reverb: Float) {
        if (isCreated) {
            nativeSetGuitarParams(
                sustain.coerceIn(0f, 1f),
                gain.coerceIn(0f, 1f),
                distortion.coerceIn(0f, 1f),
                reverb.coerceIn(0f, 1f)
            )
        }
    }
    
    /**
     * Attiva/disattiva il Wah pedal (Dunlop Cry Baby)
     */
    fun setWahEnabled(enabled: Boolean) {
        if (isCreated) {
            nativeSetWahEnabled(enabled)
        }
    }
    
    /**
     * Imposta la posizione del Wah pedal (modalità manuale)
     * @param position 0.0 = heel (suono basso), 1.0 = toe (suono acuto)
     */
    fun setWahPosition(position: Float) {
        if (isCreated) {
            nativeSetWahPosition(position.coerceIn(0f, 1f))
        }
    }
    
    // Metodi JNI nativi
    private external fun nativeCreate(): Boolean
    private external fun nativeStart(): Boolean
    private external fun nativeStop()
    private external fun nativeDestroy()
    private external fun nativeNoteOn(voiceIndex: Int, frequency: Float)
    private external fun nativeNoteOff(voiceIndex: Int)
    private external fun nativeAllNotesOff()
    private external fun nativeSetMasterVolume(volume: Float)
    private external fun nativeSetWaveType(waveType: Int)
    private external fun nativeSetPitchBend(voiceIndex: Int, semitones: Float)
    private external fun nativeSetGuitarParams(sustain: Float, gain: Float, distortion: Float, reverb: Float)
    private external fun nativeSetWahEnabled(enabled: Boolean)
    private external fun nativeSetWahPosition(position: Float)
}
