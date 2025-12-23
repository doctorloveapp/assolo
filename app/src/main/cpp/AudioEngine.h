#ifndef AUDIO_ENGINE_H
#define AUDIO_ENGINE_H

#include <oboe/Oboe.h>
#include <array>
#include <mutex>
#include "Oscillator.h"

/**
 * AudioEngine - Engine audio a bassa latenza usando Oboe
 * 
 * Gestisce multiple voci per supporto multitouch (polifonia).
 * Ogni voce è un oscillatore indipendente con il proprio envelope ADSR.
 */
class AudioEngine : public oboe::AudioStreamCallback {
public:
    static constexpr int MAX_VOICES = 8; // Supporta fino a 8 note simultanee

    AudioEngine();
    ~AudioEngine();
    
    // Controllo engine
    bool start();
    void stop();
    
    // Controllo note (chiamate da JNI)
    void noteOn(int voiceIndex, float frequency);
    void noteOff(int voiceIndex);
    void allNotesOff();
    void setPitchBend(int voiceIndex, float semitones);  // Pitch bend per una voce
    
    // Configurazione
    void setMasterVolume(float volume);
    void setWaveType(int type); // 0=Sine, 1=Sawtooth, 2=Square, 3=Triangle
    
    // Callback Oboe
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) override;
    
    void onErrorBeforeClose(oboe::AudioStream *audioStream, oboe::Result error) override;
    void onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result error) override;

private:
    bool openStream();
    void restartStream();
    
    std::shared_ptr<oboe::AudioStream> stream;
    std::array<Oscillator, MAX_VOICES> voices;
    std::mutex voiceMutex;
    
    float masterVolume = 0.8f;
    int sampleRate = 48000;
    int framesPerBuffer = 0;
    
    bool isRunning = false;
};

#endif // AUDIO_ENGINE_H
