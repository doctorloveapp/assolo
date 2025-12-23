#include "Oscillator.h"
#include <cmath>
#include <algorithm>

Oscillator::Oscillator() {
    envelope.setSampleRate(sampleRate);
}

void Oscillator::setSampleRate(float rate) {
    sampleRate = rate;
    envelope.setSampleRate(rate);
    // Ricalcola phase increment con il nuovo sample rate
    phaseIncrement = (TWO_PI * frequency) / sampleRate;
}

void Oscillator::setFrequency(float freq) {
    frequency = std::clamp(freq, 20.0f, 20000.0f);
    phaseIncrement = (TWO_PI * frequency) / sampleRate;
}

void Oscillator::setWaveType(WaveType type) {
    waveType = type;
}

void Oscillator::setAmplitude(float amp) {
    amplitude = std::clamp(amp, 0.0f, 1.0f);
}

void Oscillator::noteOn(float freq) {
    setFrequency(freq);
    phase = 0.0f; // Reset fase per consistenza
    envelope.noteOn();
}

void Oscillator::noteOff() {
    envelope.noteOff();
}

void Oscillator::reset() {
    phase = 0.0f;
    envelope.reset();
}

float Oscillator::generateWave() const {
    switch (waveType) {
        case WaveType::Sine:
            return std::sin(phase);
            
        case WaveType::Sawtooth:
            // Onda a dente di sega: va da -1 a 1 linearmente
            return (phase / static_cast<float>(M_PI)) - 1.0f;
            
        case WaveType::Square:
            // Onda quadra: 1 per la prima metà, -1 per la seconda
            return phase < static_cast<float>(M_PI) ? 1.0f : -1.0f;
            
        case WaveType::Triangle:
            // Onda triangolare
            if (phase < static_cast<float>(M_PI)) {
                return -1.0f + (2.0f * phase / static_cast<float>(M_PI));
            } else {
                return 3.0f - (2.0f * phase / static_cast<float>(M_PI));
            }
            
        default:
            return 0.0f;
    }
}

float Oscillator::getNextSample() {
    if (!envelope.isActive()) {
        return 0.0f;
    }
    
    // Genera l'onda
    float sample = generateWave();
    
    // Applica l'envelope ADSR
    float envelopeValue = envelope.getNextSample();
    sample *= envelopeValue;
    
    // Applica l'ampiezza
    sample *= amplitude;
    
    // Avanza la fase
    phase += phaseIncrement;
    if (phase >= TWO_PI) {
        phase -= TWO_PI;
    }
    
    return sample;
}

bool Oscillator::isActive() const {
    return envelope.isActive();
}
