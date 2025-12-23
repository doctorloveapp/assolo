#include <jni.h>
#include <memory>
#include "AudioEngine.h"

// Istanza globale dell'AudioEngine
static std::unique_ptr<AudioEngine> audioEngine;

extern "C" {

/**
 * Inizializza l'AudioEngine
 * @return true se l'inizializzazione ha successo
 */
JNIEXPORT jboolean JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeCreate(JNIEnv *env, jobject thiz) {
    if (audioEngine) {
        return JNI_TRUE; // Già creato
    }
    
    audioEngine = std::make_unique<AudioEngine>();
    return audioEngine != nullptr ? JNI_TRUE : JNI_FALSE;
}

/**
 * Avvia lo stream audio
 * @return true se l'avvio ha successo
 */
JNIEXPORT jboolean JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeStart(JNIEnv *env, jobject thiz) {
    if (!audioEngine) {
        return JNI_FALSE;
    }
    return audioEngine->start() ? JNI_TRUE : JNI_FALSE;
}

/**
 * Ferma lo stream audio
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeStop(JNIEnv *env, jobject thiz) {
    if (audioEngine) {
        audioEngine->stop();
    }
}

/**
 * Distrugge l'AudioEngine e libera le risorse
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeDestroy(JNIEnv *env, jobject thiz) {
    if (audioEngine) {
        audioEngine->stop();
        audioEngine.reset();
    }
}

/**
 * Attiva una nota (note on)
 * @param voiceIndex Indice della voce (0-7 per supporto multitouch)
 * @param frequency Frequenza in Hz
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeNoteOn(
        JNIEnv *env, jobject thiz, jint voiceIndex, jfloat frequency) {
    if (audioEngine) {
        audioEngine->noteOn(voiceIndex, frequency);
    }
}

/**
 * Disattiva una nota (note off)
 * @param voiceIndex Indice della voce
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeNoteOff(
        JNIEnv *env, jobject thiz, jint voiceIndex) {
    if (audioEngine) {
        audioEngine->noteOff(voiceIndex);
    }
}

/**
 * Disattiva tutte le note
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeAllNotesOff(JNIEnv *env, jobject thiz) {
    if (audioEngine) {
        audioEngine->allNotesOff();
    }
}

/**
 * Imposta il volume master
 * @param volume Volume da 0.0 a 1.0
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeSetMasterVolume(
        JNIEnv *env, jobject thiz, jfloat volume) {
    if (audioEngine) {
        audioEngine->setMasterVolume(volume);
    }
}

/**
 * Imposta il tipo di forma d'onda
 * @param waveType 0=Sine, 1=Sawtooth, 2=Square, 3=Triangle
 */
JNIEXPORT void JNICALL
Java_com_smartinstrument_app_audio_NativeAudioEngine_nativeSetWaveType(
        JNIEnv *env, jobject thiz, jint waveType) {
    if (audioEngine) {
        audioEngine->setWaveType(waveType);
    }
}

} // extern "C"
