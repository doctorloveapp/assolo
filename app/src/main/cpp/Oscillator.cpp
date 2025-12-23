#include "Oscillator.h"
#include <cmath>
#include <algorithm>

Oscillator::Oscillator() : rng(std::random_device{}()) {
    envelope.setSampleRate(sampleRate);
    
    // Initialize reverb buffers
    reverbBuffer1.resize(REVERB_BUFFER_SIZE, 0.0f);
    reverbBuffer2.resize(static_cast<int>(REVERB_BUFFER_SIZE * 0.77f), 0.0f);
    reverbBuffer3.resize(static_cast<int>(REVERB_BUFFER_SIZE * 0.63f), 0.0f);
}

void Oscillator::setSampleRate(float rate) {
    sampleRate = rate;
    envelope.setSampleRate(rate);
    phaseIncrement = (TWO_PI * frequency) / sampleRate;
}

void Oscillator::setFrequency(float freq) {
    baseFrequency = std::clamp(freq, 20.0f, 20000.0f);
    frequency = baseFrequency * std::pow(2.0f, pitchBendSemitones / 12.0f);
    phaseIncrement = (TWO_PI * frequency) / sampleRate;
}

void Oscillator::setPitchBend(float semitones) {
    pitchBendSemitones = std::clamp(semitones, -12.0f, 12.0f);
    frequency = baseFrequency * std::pow(2.0f, pitchBendSemitones / 12.0f);
    phaseIncrement = (TWO_PI * frequency) / sampleRate;
}

void Oscillator::setWaveType(WaveType type) {
    waveType = type;
}

void Oscillator::setAmplitude(float amp) {
    amplitude = std::clamp(amp, 0.0f, 1.0f);
}

// Guitar parameter setters
void Oscillator::setGuitarSustain(float sustain) {
    guitarSustain = std::clamp(sustain, 0.0f, 1.0f);
}

void Oscillator::setGuitarGain(float gain) {
    guitarGain = std::clamp(gain, 0.0f, 1.0f);
}

void Oscillator::setGuitarDistortion(float distortion) {
    guitarDistortion = std::clamp(distortion, 0.0f, 1.0f);
}

void Oscillator::setGuitarReverb(float reverb) {
    guitarReverb = std::clamp(reverb, 0.0f, 1.0f);
}

// Wah pedal setters
void Oscillator::setWahEnabled(bool enabled) {
    wahEnabled = enabled;
    wahAutoMode = true;  // Default to auto when enabling
    if (!enabled) {
        wahBandpass1 = 0.0f;
        wahBandpass2 = 0.0f;
    }
}

void Oscillator::setWahPosition(float position) {
    wahPosition = std::clamp(position, 0.0f, 1.0f);
    wahAutoMode = false;  // Switch to manual mode when position is set
}

void Oscillator::initStringModel() {
    // Only used for bass now (keeping for compatibility)
    int delaySize = static_cast<int>(sampleRate / frequency);
    if (delaySize < 2) delaySize = 2;
    
    delayLine.resize(delaySize);
    
    for (int i = 0; i < delaySize; i++) {
        float env = std::sin(static_cast<float>(i) / delaySize * M_PI);
        delayLine[i] = noiseDist(rng) * env;
    }
    
    delayIndex = 0;
    filterState = 0.0f;
    filterState2 = 0.0f;
    stringInitialized = true;
    stringEnergy = 1.0f;
}

void Oscillator::noteOn(float freq) {
    setFrequency(freq);
    pitchBendSemitones = 0.0f;
    phase = 0.0f;
    filterState = 0.0f;
    filterState2 = 0.0f;
    stringEnergy = 1.0f;
    stringInitialized = true;
    
    envelope.noteOn();
}

void Oscillator::noteOff() {
    envelope.noteOff();
}

void Oscillator::reset() {
    phase = 0.0f;
    envelope.reset();
    stringInitialized = false;
    stringEnergy = 1.0f;
    filterState = 0.0f;
    filterState2 = 0.0f;
    
    // Clear reverb buffers
    std::fill(reverbBuffer1.begin(), reverbBuffer1.end(), 0.0f);
    std::fill(reverbBuffer2.begin(), reverbBuffer2.end(), 0.0f);
    std::fill(reverbBuffer3.begin(), reverbBuffer3.end(), 0.0f);
}

/**
 * Simple plate-style reverb using multiple comb filters
 */
float Oscillator::applyReverb(float input) {
    if (guitarReverb < 0.01f) return input;
    
    // Read from delay lines
    float rev1 = reverbBuffer1[reverbIndex1];
    float rev2 = reverbBuffer2[reverbIndex2];
    float rev3 = reverbBuffer3[reverbIndex3];
    
    // Mix reverb tails
    float reverbMix = (rev1 + rev2 + rev3) / 3.0f;
    
    // Decay factor based on reverb amount
    float decay = 0.3f + guitarReverb * 0.5f;
    
    // Write to delay lines with feedback
    reverbBuffer1[reverbIndex1] = input + rev1 * decay;
    reverbBuffer2[reverbIndex2] = input + rev2 * decay * 0.9f;
    reverbBuffer3[reverbIndex3] = input + rev3 * decay * 0.8f;
    
    // Advance indices
    reverbIndex1 = (reverbIndex1 + 1) % reverbBuffer1.size();
    reverbIndex2 = (reverbIndex2 + 1) % reverbBuffer2.size();
    reverbIndex3 = (reverbIndex3 + 1) % reverbBuffer3.size();
    
    // Mix dry/wet based on reverb amount
    return input * (1.0f - guitarReverb * 0.5f) + reverbMix * guitarReverb;
}

/**
 * Heavy Distortion - Marshall/Mesa Boogie style tube amp simulation
 * Now with configurable drive!
 */
float Oscillator::applyDistortion(float input, float drive) {
    // Scale drive by user parameter
    float effectiveDrive = drive * (0.5f + guitarDistortion * 1.5f);
    
    // STAGE 1: Pre-amp gain
    float x = input * effectiveDrive;
    
    // STAGE 2: Tube-style asymmetric soft clipping
    float stage1;
    if (x > 0) {
        stage1 = 1.0f - std::exp(-x * 1.5f);
    } else {
        stage1 = -1.0f + std::exp(x * 1.2f);
    }
    
    // STAGE 3: Second gain stage (cranked amp)
    float stage2 = stage1 * (2.0f + guitarDistortion * 2.0f);
    stage2 = std::tanh(stage2);
    
    // STAGE 4: Add odd harmonics for aggressive bite
    float harmonics = stage2 + 0.3f * std::tanh(stage2 * 3.0f);
    
    // Final saturation
    float output = std::tanh(harmonics * 1.2f);
    
    return output;
}

/**
 * Dunlop Cry Baby Wah Pedal Simulation
 * Classic wah is a bandpass filter with sweeping center frequency
 * Cry Baby: Q ~= 5-8, frequency range ~400Hz to ~2.2kHz
 * Supports both auto-wah (LFO) and manual pedal control
 */
float Oscillator::applyWah(float input) {
    if (!wahEnabled) return input;
    
    float currentPosition;
    
    if (wahAutoMode) {
        // Auto-wah: LFO sweeps the pedal position automatically
        // Speed: about 3-4 Hz for classic wah-wah sound
        wahPhase += (TWO_PI * 3.5f) / sampleRate;
        if (wahPhase >= TWO_PI) wahPhase -= TWO_PI;
        
        // Sweep between heel (0) and toe (1) using sine LFO
        currentPosition = 0.5f + 0.5f * std::sin(wahPhase);
    } else {
        // Manual mode: use wahPosition directly (controlled by UI)
        currentPosition = wahPosition;
    }
    
    // Cry Baby frequency range: ~400 Hz (heel) to ~2200 Hz (toe)
    // Using normalized frequency (0-1 range relative to sample rate)
    float minFreq = 400.0f / sampleRate;
    float maxFreq = 2200.0f / sampleRate;
    float centerFreq = minFreq + currentPosition * (maxFreq - minFreq);
    
    // Bandpass filter coefficients (state variable filter)
    // Q factor: Cry Baby has high Q for that vocal "wah" character
    float Q = 6.0f;  // High Q for narrow, vocal-like sweep
    float f = 2.0f * std::sin(3.14159f * centerFreq);  // Filter frequency
    float q = 1.0f / Q;
    
    // State variable filter (bandpass output)
    float hp = input - wahBandpass2 - q * wahBandpass1;
    wahBandpass1 += f * hp;
    wahBandpass2 += f * wahBandpass1;
    
    // Bandpass output with resonance boost
    float bandpass = wahBandpass1 * Q * 0.5f;
    
    // Mix: mostly wah effect with some dry signal for clarity
    float wet = 0.75f * bandpass + 0.25f * input;
    
    // Slight saturation for that classic Cry Baby warmth
    wet = std::tanh(wet * 1.5f);
    
    return wet;
}

/**
 * Hammond B3 style organ - LOUD VERSION
 */
float Oscillator::generateHammondB3() const {
    float sample = 0.0f;
    
    // Drawbar settings: 888888888 (Full Gospel/Rock)
    sample += 1.0f * std::sin(phase * 0.5f);    // 16' - sub-octave
    sample += 1.0f * std::sin(phase * 1.5f);    // 5⅓' - fifth  
    sample += 1.0f * std::sin(phase);           // 8' - fundamental
    sample += 1.0f * std::sin(phase * 2.0f);    // 4' - octave
    sample += 0.6f * std::sin(phase * 3.0f);    // 2⅔' - fifth above octave
    sample += 0.6f * std::sin(phase * 4.0f);    // 2' - 2 octaves
    sample += 0.3f * std::sin(phase * 5.0f);    // 1⅗' - major 3rd
    sample += 0.3f * std::sin(phase * 6.0f);    // 1⅓' - fifth
    sample += 0.2f * std::sin(phase * 8.0f);    // 1' - 3 octaves
    
    // Normalize but keep LOUD
    sample /= 3.0f;
    
    // Leslie speaker / overdrive character
    sample = std::tanh(sample * 2.5f);
    
    return sample;
}

/**
 * SCREAMING ELECTRIC GUITAR - Oscillator-based with configurable parameters
 * Pickups + Tubes + Distortion + Reverb
 */
float Oscillator::generateElectricGuitar() {
    // ===========================================
    // OSCILLATOR BASE: Rich harmonics like pickups capture
    // ===========================================
    
    // Sawtooth base (humbucker character)
    float saw = (phase / static_cast<float>(M_PI)) - 1.0f;
    
    // Pulse for single-coil character
    float pulseWidth = 0.65f + 0.1f * std::sin(phase * 0.01f);  // Slight PWM
    float pulse = phase < (M_PI * pulseWidth) ? 1.0f : -1.0f;
    
    // Mix for rich harmonic content
    float oscillator = 0.6f * saw + 0.4f * pulse;
    
    // ===========================================
    // HARMONICS: Guitar overtone series
    // ===========================================
    float harmonics = 0.0f;
    harmonics += 0.5f * std::sin(phase * 2.0f);   // Octave
    harmonics += 0.35f * std::sin(phase * 3.0f);  // Fifth
    harmonics += 0.25f * std::sin(phase * 4.0f);  // 2 octaves
    harmonics += 0.15f * std::sin(phase * 5.0f);  // Major 3rd
    harmonics += 0.1f * std::sin(phase * 6.0f);   // Added brightness
    
    float raw = 0.65f * oscillator + 0.35f * harmonics;
    
    // ===========================================
    // PICKUP + FILTER
    // ===========================================
    float cutoff = 0.6f + guitarGain * 0.2f;  // Brighter with more gain
    filterState = filterState + cutoff * (raw - filterState);
    float pickupSignal = filterState;
    
    // Sub-harmonic warmth
    pickupSignal += 0.15f * std::sin(phase * 0.5f);
    
    // ===========================================
    // AMP + DISTORTION with user parameters
    // ===========================================
    
    // Pre-amp boost based on gain setting
    float preamp = pickupSignal * (2.0f + guitarGain * 3.0f);
    
    // Distortion with user-controlled drive
    float drive = 15.0f + guitarDistortion * 15.0f;  // 15-30 range
    float distorted = applyDistortion(preamp, drive);
    
    // Presence/bite
    float presence = (0.15f + guitarGain * 0.15f) * (pickupSignal - filterState);
    distorted += presence;
    
    // ===========================================
    // FEEDBACK/SUSTAIN based on user parameter
    // ===========================================
    float feedbackAmount = 0.1f + guitarSustain * 0.2f;
    float feedback = feedbackAmount * std::sin(phase) * stringEnergy;
    feedback += feedbackAmount * 0.5f * std::sin(phase * 2.0f) * stringEnergy;
    distorted += feedback;
    
    // Energy decay based on sustain setting (higher = slower decay)
    float decayRate = 0.9995f + guitarSustain * 0.00045f;  // 0.9995 to 0.99995
    if (stringEnergy > (0.3f + guitarSustain * 0.4f)) {
        stringEnergy *= decayRate;
    }
    // Minimum energy for sustain
    float minEnergy = 0.3f + guitarSustain * 0.5f;
    if (stringEnergy < minEnergy) stringEnergy = minEnergy;
    
    // ===========================================
    // OUTPUT + WAH + REVERB
    // ===========================================
    float output = distorted * (1.3f + guitarGain * 0.7f);
    
    // Apply Cry Baby Wah (before reverb for classic sound)
    output = applyWah(output);
    
    // Apply reverb
    output = applyReverb(output);
    
    // Final soft limiter
    output = std::tanh(output);
    
    return output;
}

/**
 * FENDER PRECISION BASS - Oscillator-based, deep and punchy
 * NO plucked string - continuous powerful bass tone
 */
float Oscillator::generateElectricBass() {
    // ===========================================
    // OSCILLATOR BASE: Fender P-Bass character
    // Split-coil pickup = fat, round, punchy
    // ===========================================
    
    // Fundamental is KING for bass
    float fundamental = std::sin(phase);
    
    // Sub-octave for that chest-thumping low end
    float subOctave = 0.4f * std::sin(phase * 0.5f);
    
    // Slight sawtooth content for growl (like roundwound strings)
    float saw = 0.3f * ((phase / static_cast<float>(M_PI)) - 1.0f);
    
    // Square-ish component for punch (P-bass character)
    float square = 0.2f * (phase < static_cast<float>(M_PI) ? 1.0f : -1.0f);
    
    // Mix - heavy on fundamental
    float oscillator = fundamental + subOctave + saw + square;
    
    // ===========================================
    // HARMONICS: Bass needs controlled overtones
    // ===========================================
    float harmonics = 0.0f;
    harmonics += 0.25f * std::sin(phase * 2.0f);  // Octave (string attack)
    harmonics += 0.1f * std::sin(phase * 3.0f);   // Fifth (growl)
    
    float raw = oscillator + harmonics * 0.3f;
    
    // ===========================================
    // TONE CONTROL: Deep low-pass for that Fender thump
    // ===========================================
    float cutoff = 0.2f;  // Low cutoff = deep bass
    filterState = filterState + cutoff * (raw - filterState);
    
    // Second filter for extra smoothness
    filterState2 = filterState2 + 0.15f * (filterState - filterState2);
    
    float bassSignal = filterState2;
    
    // ===========================================
    // AMP SIMULATION: Warm tube compression
    // ===========================================
    // Slight overdrive for warmth (not harsh distortion)
    float amped = bassSignal * 2.5f;
    amped = std::tanh(amped * 1.5f);  // Soft compression
    
    // Add some "amp presence" - slight mid boost
    float midBoost = 0.1f * (filterState - filterState2);
    amped += midBoost;
    
    // ===========================================
    // ATTACK/SUSTAIN: Envelope shaping
    // ===========================================
    // Initial attack emphasis
    float attack = stringEnergy * 0.3f;
    amped *= (1.0f + attack);
    
    // Slow decay for sustained bass
    stringEnergy *= 0.9998f;
    if (stringEnergy < 0.7f) stringEnergy = 0.7f;  // High sustain minimum
    
    // ===========================================
    // OUTPUT: Big and loud!
    // ===========================================
    float output = amped * 1.8f;  // Volume boost
    
    // Final limiter
    output = std::tanh(output);
    
    return output;
}

float Oscillator::generateWave() {
    switch (waveType) {
        case WaveType::Sine:
            return generateHammondB3();
            
        case WaveType::Sawtooth:
            return (phase / static_cast<float>(M_PI)) - 1.0f;
            
        case WaveType::Square:
            return phase < static_cast<float>(M_PI) ? 1.0f : -1.0f;
            
        case WaveType::Bass:
            return generateElectricBass();
            
        case WaveType::Guitar:
            return generateElectricGuitar();
            
        default:
            return 0.0f;
    }
}

float Oscillator::getNextSample() {
    if (!envelope.isActive()) {
        return 0.0f;
    }
    
    float sample = generateWave();
    
    // Apply ADSR envelope
    float envelopeValue = envelope.getNextSample();
    
    if (waveType == WaveType::Guitar || waveType == WaveType::Bass) {
        // String instruments have natural sustain, envelope mainly for note-off
        sample *= std::min(1.0f, envelopeValue * 1.5f);
    } else {
        sample *= envelopeValue;
    }
    
    sample *= amplitude;
    
    // Advance phase
    phase += phaseIncrement;
    if (phase >= TWO_PI) {
        phase -= TWO_PI;
    }
    
    return sample;
}

bool Oscillator::isActive() const {
    return envelope.isActive();
}
