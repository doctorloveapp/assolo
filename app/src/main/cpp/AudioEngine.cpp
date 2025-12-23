#include "AudioEngine.h"
#include <android/log.h>
#include <algorithm>

#define LOG_TAG "AudioEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

AudioEngine::AudioEngine() {
    LOGI("AudioEngine created");
}

AudioEngine::~AudioEngine() {
    stop();
    LOGI("AudioEngine destroyed");
}

bool AudioEngine::start() {
    if (isRunning) {
        return true;
    }
    
    if (openStream()) {
        isRunning = true;
        LOGI("AudioEngine started successfully");
        return true;
    }
    
    LOGE("Failed to start AudioEngine");
    return false;
}

void AudioEngine::stop() {
    if (!isRunning) {
        return;
    }
    
    isRunning = false;
    
    if (stream) {
        stream->stop();
        stream->close();
        stream.reset();
    }
    
    LOGI("AudioEngine stopped");
}

bool AudioEngine::openStream() {
    oboe::AudioStreamBuilder builder;
    
    builder.setDirection(oboe::Direction::Output)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setSharingMode(oboe::SharingMode::Exclusive)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(oboe::ChannelCount::Mono)
           ->setSampleRate(sampleRate)
           ->setCallback(this);
    
    // LowLatency gestisce automaticamente il buffer ottimale
    
    oboe::Result result = builder.openStream(stream);
    
    if (result != oboe::Result::OK) {
        LOGE("Failed to open stream: %s", oboe::convertToText(result));
        return false;
    }
    
    // Ottieni i parametri effettivi dello stream
    sampleRate = stream->getSampleRate();
    framesPerBuffer = stream->getFramesPerBurst();
    
    LOGI("Stream opened: sampleRate=%d, framesPerBurst=%d, latency=%d ms",
         sampleRate, framesPerBuffer,
         (framesPerBuffer * 1000) / sampleRate);
    
    // Configura gli oscillatori con il sample rate effettivo
    for (auto& voice : voices) {
        voice.setSampleRate(static_cast<float>(sampleRate));
        voice.setWaveType(Oscillator::WaveType::Sawtooth);
    }
    
    // Avvia lo stream
    result = stream->requestStart();
    
    if (result != oboe::Result::OK) {
        LOGE("Failed to start stream: %s", oboe::convertToText(result));
        stream->close();
        stream.reset();
        return false;
    }
    
    return true;
}

void AudioEngine::noteOn(int voiceIndex, float frequency) {
    if (voiceIndex < 0 || voiceIndex >= MAX_VOICES) {
        LOGE("Invalid voice index: %d", voiceIndex);
        return;
    }
    
    std::lock_guard<std::mutex> lock(voiceMutex);
    voices[voiceIndex].noteOn(frequency);
    LOGI("Note ON: voice=%d, freq=%.2f Hz", voiceIndex, frequency);
}

void AudioEngine::noteOff(int voiceIndex) {
    if (voiceIndex < 0 || voiceIndex >= MAX_VOICES) {
        LOGE("Invalid voice index: %d", voiceIndex);
        return;
    }
    
    std::lock_guard<std::mutex> lock(voiceMutex);
    voices[voiceIndex].noteOff();
    LOGI("Note OFF: voice=%d", voiceIndex);
}

void AudioEngine::allNotesOff() {
    std::lock_guard<std::mutex> lock(voiceMutex);
    for (auto& voice : voices) {
        voice.noteOff();
    }
    LOGI("All notes OFF");
}

void AudioEngine::setPitchBend(int voiceIndex, float semitones) {
    if (voiceIndex < 0 || voiceIndex >= MAX_VOICES) {
        return;
    }
    
    std::lock_guard<std::mutex> lock(voiceMutex);
    voices[voiceIndex].setPitchBend(semitones);
}

void AudioEngine::setMasterVolume(float volume) {
    masterVolume = std::clamp(volume, 0.0f, 1.0f);
    LOGI("Master volume set to: %.2f", masterVolume);
}

void AudioEngine::setWaveType(int type) {
    Oscillator::WaveType waveType;
    
    switch (type) {
        case 0: waveType = Oscillator::WaveType::Sine; break;     // Hammond B3
        case 1: waveType = Oscillator::WaveType::Sawtooth; break; // Synth Lead
        case 2: waveType = Oscillator::WaveType::Square; break;   // Retro
        case 3: waveType = Oscillator::WaveType::Bass; break;     // Electric Bass
        case 4: waveType = Oscillator::WaveType::Guitar; break;   // Electric Guitar
        default: waveType = Oscillator::WaveType::Sawtooth; break;
    }
    
    std::lock_guard<std::mutex> lock(voiceMutex);
    for (auto& voice : voices) {
        voice.setWaveType(waveType);
    }
    
    LOGI("Wave type set to: %d", type);
}

void AudioEngine::setGuitarParams(float sustain, float gain, float distortion, float reverb) {
    std::lock_guard<std::mutex> lock(voiceMutex);
    for (auto& voice : voices) {
        voice.setGuitarSustain(sustain);
        voice.setGuitarGain(gain);
        voice.setGuitarDistortion(distortion);
        voice.setGuitarReverb(reverb);
    }
    LOGI("Guitar params: sustain=%.2f, gain=%.2f, dist=%.2f, reverb=%.2f", 
         sustain, gain, distortion, reverb);
}

void AudioEngine::setWahEnabled(bool enabled) {
    std::lock_guard<std::mutex> lock(voiceMutex);
    for (auto& voice : voices) {
        voice.setWahEnabled(enabled);
    }
    LOGI("Wah pedal: %s", enabled ? "ON" : "OFF");
}

void AudioEngine::setWahPosition(float position) {
    std::lock_guard<std::mutex> lock(voiceMutex);
    for (auto& voice : voices) {
        voice.setWahPosition(position);
    }
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {
    
    auto *outputBuffer = static_cast<float *>(audioData);
    
    // Azzera il buffer
    std::fill(outputBuffer, outputBuffer + numFrames, 0.0f);
    
    // Mix di tutte le voci attive
    {
        std::lock_guard<std::mutex> lock(voiceMutex);
        
        for (auto& voice : voices) {
            if (voice.isActive()) {
                for (int i = 0; i < numFrames; ++i) {
                    outputBuffer[i] += voice.getNextSample();
                }
            }
        }
    }
    
    // Applica master volume con attenuazione base (synth troppo forte rispetto alle basi)
    const float synthAttenuation = 0.25f;  // Riduce il volume massimo del synth
    for (int i = 0; i < numFrames; ++i) {
        outputBuffer[i] *= masterVolume * synthAttenuation;
        // Soft clipping per evitare distorsione
        outputBuffer[i] = std::clamp(outputBuffer[i], -1.0f, 1.0f);
    }
    
    return oboe::DataCallbackResult::Continue;
}

void AudioEngine::onErrorBeforeClose(oboe::AudioStream *audioStream, oboe::Result error) {
    LOGE("Error before close: %s", oboe::convertToText(error));
}

void AudioEngine::onErrorAfterClose(oboe::AudioStream *audioStream, oboe::Result error) {
    LOGE("Error after close: %s", oboe::convertToText(error));
    
    // Prova a riavviare lo stream
    if (isRunning) {
        restartStream();
    }
}

void AudioEngine::restartStream() {
    LOGI("Restarting audio stream...");
    
    if (stream) {
        stream->close();
        stream.reset();
    }
    
    if (openStream()) {
        LOGI("Stream restarted successfully");
    } else {
        LOGE("Failed to restart stream");
    }
}
