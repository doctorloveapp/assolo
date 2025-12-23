#include "ADSREnvelope.h"
#include <algorithm>

ADSREnvelope::ADSREnvelope() {
    calculateRates();
}

void ADSREnvelope::setSampleRate(float rate) {
    sampleRate = rate;
    calculateRates();
}

void ADSREnvelope::setAttackTime(float seconds) {
    attackTime = std::max(0.001f, seconds); // Minimo 1ms
    calculateRates();
}

void ADSREnvelope::setDecayTime(float seconds) {
    decayTime = std::max(0.001f, seconds);
    calculateRates();
}

void ADSREnvelope::setSustainLevel(float level) {
    sustainLevel = std::clamp(level, 0.0f, 1.0f);
}

void ADSREnvelope::setReleaseTime(float seconds) {
    releaseTime = std::max(0.001f, seconds);
    calculateRates();
}

void ADSREnvelope::calculateRates() {
    // Calcola quanto incrementare/decrementare per ogni sample
    attackRate = 1.0f / (attackTime * sampleRate);
    decayRate = (1.0f - sustainLevel) / (decayTime * sampleRate);
    releaseRate = sustainLevel / (releaseTime * sampleRate);
}

void ADSREnvelope::noteOn() {
    currentState = State::Attack;
    // Non resettiamo currentLevel per evitare click se ri-triggeriamo velocemente
}

void ADSREnvelope::noteOff() {
    if (currentState != State::Idle) {
        currentState = State::Release;
        // Ricalcola releaseRate basato sul livello corrente
        if (currentLevel > 0.001f) {
            releaseRate = currentLevel / (releaseTime * sampleRate);
        }
    }
}

void ADSREnvelope::reset() {
    currentState = State::Idle;
    currentLevel = 0.0f;
}

float ADSREnvelope::getNextSample() {
    switch (currentState) {
        case State::Idle:
            return 0.0f;
            
        case State::Attack:
            currentLevel += attackRate;
            if (currentLevel >= 1.0f) {
                currentLevel = 1.0f;
                currentState = State::Decay;
            }
            break;
            
        case State::Decay:
            currentLevel -= decayRate;
            if (currentLevel <= sustainLevel) {
                currentLevel = sustainLevel;
                currentState = State::Sustain;
            }
            break;
            
        case State::Sustain:
            // Mantieni il livello di sustain
            currentLevel = sustainLevel;
            break;
            
        case State::Release:
            currentLevel -= releaseRate;
            if (currentLevel <= 0.0f) {
                currentLevel = 0.0f;
                currentState = State::Idle;
            }
            break;
    }
    
    return currentLevel;
}

bool ADSREnvelope::isActive() const {
    return currentState != State::Idle;
}
