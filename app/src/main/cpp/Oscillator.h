#ifndef OSCILLATOR_H
#define OSCILLATOR_H

#include "ADSREnvelope.h"
#include <vector>
#include <random>

/**
 * Oscillator - Generatore di forme d'onda
 * Supporta: Hammond B3, Synth Lead, Drums, Electric Bass, Electric Guitar (Distorted)
 */
class Oscillator {
public:
    enum class WaveType {
        Sine,      // Hammond B3 style (additive synthesis with drawbars)
        Sawtooth,  // Synth lead
        Drums,     // Electronic drum synthesis
        Bass,      // Electric Bass
        Guitar     // Electric Guitar with distortion and sustain
    };

    Oscillator();
    
    void setSampleRate(float sampleRate);
    void setFrequency(float frequency);
    void setWaveType(WaveType type);
    void setAmplitude(float amplitude);
    void setPitchBend(float semitones);  // Pitch bend in semitones (-2 to +2)
    
    // Guitar parameters (0.0 to 1.0)
    void setGuitarSustain(float sustain);
    void setGuitarGain(float gain);
    void setGuitarDistortion(float distortion);
    void setGuitarReverb(float reverb);
    
    // Wah pedal (Dunlop Cry Baby)
    void setWahEnabled(bool enabled);
    void setWahPosition(float position);  // 0.0 = heel down, 1.0 = toe down
    
    void noteOn(float frequency);
    void noteOff();
    void reset();
    
    float getNextSample();
    bool isActive() const;
    
    // Accesso all'envelope per configurazione
    ADSREnvelope& getEnvelope() { return envelope; }

private:
    float generateWave();
    float generateHammondB3() const;
    float generateElectricGuitar();
    float generateElectricBass();
    float generateDrum();  // Electronic drum synthesis
    void initStringModel();
    
    // Effects
    float applyDistortion(float input, float drive);
    float applyReverb(float input);
    float applyWah(float input);  // Wah pedal effect
    
    float sampleRate = 48000.0f;
    float frequency = 440.0f;
    float baseFrequency = 440.0f;  // Frequency without pitch bend
    float pitchBendSemitones = 0.0f;  // Current pitch bend amount
    float phase = 0.0f;
    float phaseIncrement = 0.0f;
    float amplitude = 0.8f;
    
    WaveType waveType = WaveType::Sawtooth;
    ADSREnvelope envelope;
    
    // Guitar parameters (0.0 to 1.0, will be scaled internally)
    float guitarSustain = 0.7f;
    float guitarGain = 0.7f;
    float guitarDistortion = 0.7f;
    float guitarReverb = 0.3f;
    
    // Wah pedal state
    bool wahEnabled = false;
    bool wahAutoMode = true;       // true = auto-wah LFO, false = manual
    float wahPosition = 0.5f;      // 0.0 heel, 1.0 toe (manual mode)
    float wahPhase = 0.0f;         // For auto-wah LFO
    float wahBandpass1 = 0.0f;     // Bandpass filter state
    float wahBandpass2 = 0.0f;     // Second stage
    
    // Reverb delay lines (simple comb filter reverb)
    static constexpr int REVERB_BUFFER_SIZE = 4800;  // 100ms at 48kHz
    std::vector<float> reverbBuffer1;
    std::vector<float> reverbBuffer2;
    std::vector<float> reverbBuffer3;
    int reverbIndex1 = 0;
    int reverbIndex2 = 0;
    int reverbIndex3 = 0;
    
    // String model delay line (for bass)
    std::vector<float> delayLine;
    int delayIndex = 0;
    float filterState = 0.0f;
    float filterState2 = 0.0f;  // Second filter for bass
    bool stringInitialized = false;
    float stringEnergy = 1.0f;  // Tracks remaining energy for sustain
    
    // Drum synthesis state
    float drumPhase2 = 0.0f;    // Second oscillator for FM
    float drumDecay = 1.0f;     // Amplitude decay
    float drumNoiseLevel = 0.0f;  // Noise component level
    
    // Random generator
    std::mt19937 rng;
    std::uniform_real_distribution<float> noiseDist{-1.0f, 1.0f};
    
    static constexpr float TWO_PI = 6.283185307179586f;
};

#endif // OSCILLATOR_H
