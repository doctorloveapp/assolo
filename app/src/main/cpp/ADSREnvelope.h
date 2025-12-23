#ifndef ADSR_ENVELOPE_H
#define ADSR_ENVELOPE_H

/**
 * ADSREnvelope - Inviluppo ADSR per evitare click audio on/off
 * 
 * Attack: Tempo per raggiungere il volume massimo
 * Decay: Tempo per scendere al livello di sustain
 * Sustain: Livello mantenuto finché la nota è premuta
 * Release: Tempo per tornare a zero dopo il rilascio
 */
class ADSREnvelope {
public:
    enum class State {
        Idle,
        Attack,
        Decay,
        Sustain,
        Release
    };

    ADSREnvelope();
    
    void setSampleRate(float sampleRate);
    void setAttackTime(float seconds);
    void setDecayTime(float seconds);
    void setSustainLevel(float level);
    void setReleaseTime(float seconds);
    
    void noteOn();
    void noteOff();
    void reset();
    
    float getNextSample();
    bool isActive() const;
    State getState() const { return currentState; }

private:
    void calculateRates();
    
    float sampleRate = 48000.0f;
    
    // Tempi ADSR in secondi
    float attackTime = 0.01f;   // 10ms - molto veloce per synth
    float decayTime = 0.05f;    // 50ms
    float sustainLevel = 0.7f;  // 70% del volume
    float releaseTime = 0.1f;   // 100ms
    
    // Rates (incremento per sample)
    float attackRate = 0.0f;
    float decayRate = 0.0f;
    float releaseRate = 0.0f;
    
    // Stato corrente
    State currentState = State::Idle;
    float currentLevel = 0.0f;
};

#endif // ADSR_ENVELOPE_H
