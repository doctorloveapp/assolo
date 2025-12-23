#ifndef OSCILLATOR_H
#define OSCILLATOR_H

#include "ADSREnvelope.h"

/**
 * Oscillator - Generatore di forme d'onda
 * Supporta: Sine, Sawtooth, Square, Triangle
 */
class Oscillator {
public:
    enum class WaveType {
        Sine,
        Sawtooth,
        Square,
        Triangle
    };

    Oscillator();
    
    void setSampleRate(float sampleRate);
    void setFrequency(float frequency);
    void setWaveType(WaveType type);
    void setAmplitude(float amplitude);
    
    void noteOn(float frequency);
    void noteOff();
    void reset();
    
    float getNextSample();
    bool isActive() const;
    
    // Accesso all'envelope per configurazione
    ADSREnvelope& getEnvelope() { return envelope; }

private:
    float generateWave() const;
    
    float sampleRate = 48000.0f;
    float frequency = 440.0f;
    float phase = 0.0f;
    float phaseIncrement = 0.0f;
    float amplitude = 0.8f;
    
    WaveType waveType = WaveType::Sawtooth;
    ADSREnvelope envelope;
    
    static constexpr float TWO_PI = 6.283185307179586f;
};

#endif // OSCILLATOR_H
