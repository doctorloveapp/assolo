<p align="center">
  <img src="docs/assets/logo.png" alt="Assolo Logo" width="120" height="120">
</p>

<h1 align="center">Assolo</h1>

<p align="center">
  <strong>Play in perfect harmony — always in key, always in tune</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#download">Download</a> •
  <a href="#how-it-works">How It Works</a> •
  <a href="#technical-architecture">Architecture</a> •
  <a href="#building-from-source">Build</a> •
  <a href="#contributing">Contributing</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-2.0.5-E94560?style=flat&logo=semantic-release&logoColor=white" alt="Version">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Platform">
  <img src="https://img.shields.io/badge/Min%20SDK-26-blue?style=flat" alt="Min SDK">
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white" alt="Language">
  <img src="https://img.shields.io/badge/Audio-Oboe%20C++-00599C?style=flat&logo=cplusplus&logoColor=white" alt="Audio Engine">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="UI">
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat" alt="License">
</p>

---

## 🎵 Overview

**Assolo** is a revolutionary Android application that transforms your device into an intelligent musical instrument. Unlike traditional instrument apps, Assolo ensures you **never play a wrong note** — the interface dynamically adapts to display only harmonically correct notes based on the detected or selected musical key.

Whether you're a professional musician looking for a portable practice tool, a beginner learning music theory, or a creative artist seeking new ways to jam along with your favorite tracks, Assolo provides an intuitive, low-latency playing experience.

<p align="center">
  <img src="docs/assets/screenshot_main.png" alt="App Screenshot" width="300">
</p>

---

## ✨ Features

### 🎹 Intelligent Note Grid
- **Pentatonic Scale Mapping**: The grid displays only notes from the pentatonic scale of the selected key, eliminating dissonant notes
- **Configurable Layout**: Choose between 5, 7, 10, or 12 rows for different playing styles
- **Visual Note Labels**: Optional display of note names and frequencies for learning

### 🎸 Pitch Bend (Guitar-Style)
- **Horizontal Drag**: Slide finger left or right while holding a note to bend the pitch
- **Guitar Feel**: Pitch always bends upward (like pulling a guitar string)
- **Visual Feedback**: Note turns orange/red with ↗ indicator showing bend amount
- **Range**: Up to 2 semitones (one whole tone) of bend

### ⚡ Ultra-Low Latency Audio
- **Native C++ Audio Engine**: Built with Google's [Oboe](https://github.com/google/oboe) library for professional-grade audio performance
- **Sub-10ms Latency**: Optimized for real-time performance with minimal perceptible delay
- **8-Voice Polyphony**: Play full chords with multitouch support

### 🎛️ Synthesizer Controls
- **4 Wave Types**: Sine, Sawtooth, Square, and Triangle waveforms
- **ADSR Envelope**: Smooth attack and release to eliminate audio artifacts
- **Master Volume**: Fine-grained control over output levels

### 🎼 Musical Intelligence
- **Automatic Key Detection**: Fast async analysis using MediaCodec decoding + **bass-focused autocorrelation** (low-pass filter isolates bass frequencies where the root note is most prominent) + Krumhansl-Schmuckler key profiles
- **High Accuracy**: Correctly identifies root notes by analyzing bass frequencies (40-300 Hz) instead of full spectrum, avoiding harmonic confusion
- **Manual Key Selection**: Choose from all 12 root notes (C through B) with Major/Minor modes
- **Blues Scale**: Full blues scale with "blue notes" (♭5 for minor, ♭3 for major) highlighted in gold

### 🎧 Backing Track Player
- **Audio File Loading**: Import MP3/WAV/AAC files via Storage Access Framework
- **ExoPlayer Integration**: Professional playback with play/pause, stop, and seek controls
- **Mix Control**: Independent volume sliders for backing track and synthesizer
- **Collapsible Player**: Hide the player panel for more screen space during performance

---

## 📱 Download

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.smartinstrument.app">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" width="200">
  </a>
</p>

**Minimum Requirements:**
- Android 8.0 (API 26) or higher
- ARM64 or x86_64 processor
- ~50MB storage space

---

## 🎯 How It Works

### The Pentatonic Advantage

The pentatonic scale is a 5-note scale used across virtually every musical culture worldwide. Its unique property is that **any note in the scale sounds good with any other note** — there are no dissonant intervals.

```
Major Pentatonic: 1 - 2 - 3 - 5 - 6
Minor Pentatonic: 1 - ♭3 - 4 - 5 - ♭7
```

By constraining the instrument to only display pentatonic notes matching the backing track's key, Assolo eliminates the possibility of playing "wrong" notes while preserving full creative expression.

### Touch-to-Sound Pipeline

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐     ┌─────────────┐
│ Touch Event │ ──▶ │ Kotlin/JNI   │ ──▶ │ C++ Oboe    │ ──▶ │ Audio Out   │
│ (DOWN/MOVE) │     │ Bridge       │     │ Oscillator  │     │ (Speaker)   │
└─────────────┘     └──────────────┘     └─────────────┘     └─────────────┘
      ~1ms                ~0.5ms              ~8ms               Real-time
      
      Vertical: Note Selection    Horizontal: Pitch Bend (±2 semitones)
```

---

## 🏗️ Technical Architecture

### Project Structure

```
Assolo/                                   # Project root
├── app/
│   ├── libs/                             # Local JAR dependencies
│   │   └── TarsosDSP-latest.jar          # Audio analysis library
│   ├── src/main/
│   │   ├── cpp/                          # Native C++ audio engine
│   │   │   ├── CMakeLists.txt            # CMake build configuration
│   │   │   ├── AudioEngine.cpp/h         # Main Oboe audio stream handler
│   │   │   ├── Oscillator.cpp/h          # Waveform generator (Sin/Saw/Sq/Tri)
│   │   │   ├── ADSREnvelope.cpp/h        # Attack-Decay-Sustain-Release envelope
│   │   │   └── native-lib.cpp            # JNI bridge functions
│   │   │
│   │   ├── java/com/smartinstrument/app/
│   │   │   ├── MainActivity.kt           # Main activity entry point
│   │   │   ├── audio/
│   │   │   │   ├── NativeAudioEngine.kt  # Kotlin wrapper for JNI calls
│   │   │   │   ├── TrackPlayer.kt        # ExoPlayer wrapper for backing tracks
│   │   │   │   └── KeyDetector.kt        # Bass-focused pitch detection + key analysis
│   │   │   ├── music/
│   │   │   │   ├── MusicalKey.kt         # Key/Scale data models
│   │   │   │   └── PentatonicScale.kt    # Scale generation logic
│   │   │   └── ui/
│   │   │       ├── components/           # Reusable Compose components
│   │   │       │   ├── InstrumentGrid.kt # Touch-enabled note grid
│   │   │       │   └── Controls.kt       # Settings UI components
│   │   │       ├── screens/
│   │   │       │   └── MainScreen.kt     # Main instrument screen
│   │   │       └── theme/                # Material3 theming
│   │   │
│   │   ├── res/                          # Android resources
│   │   └── AndroidManifest.xml
│   │
│   └── build.gradle.kts                  # App-level build config
│
├── docs/                                 # Documentation assets
│   └── assets/                           # Screenshots, logos
│
├── documenti/                            # Project documents (local)
│
├── gradle/
│   └── libs.versions.toml                # Dependency version catalog
│
├── .gitignore                            # Git ignore rules
├── build.gradle.kts                      # Project-level build config
├── settings.gradle.kts                   # Gradle settings
├── README.md                             # This file
├── CHANGELOG.md                          # Version history
├── CONTRIBUTING.md                       # Contribution guidelines
├── LICENSE                               # MIT License
├── PRIVACY_POLICY.md                     # Privacy policy
└── SECURITY.md                           # Security policy
```

### Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **UI** | Jetpack Compose | Declarative, reactive UI framework |
| **Audio Engine** | Oboe (C++) | Low-latency audio I/O |
| **Key Detection** | Bass-focused Autocorrelation | Pitch detection with low-pass filter + Krumhansl-Schmuckler profiles |
| **Playback** | Media3 ExoPlayer | Backing track playback |
| **Build** | Gradle + CMake | Kotlin/C++ multi-platform build |

### Audio Engine Details

The native audio engine is built for maximum performance:

- **Oboe Configuration**:
  - Performance Mode: `LowLatency`
  - Sharing Mode: `Exclusive`
  - Sample Rate: 48kHz
  - Format: Float32
  - Channel Count: Mono

- **Oscillator**:
  - Real-time waveform generation
  - Phase-continuous frequency changes
  - Amplitude normalization

- **ADSR Envelope**:
  - Attack: 10ms (instant response)
  - Decay: 50ms
  - Sustain: 70%
  - Release: 100ms (smooth fade)

---

## 🔧 Building from Source

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **Android NDK**: r25c or newer (auto-installed by AGP)
- **CMake**: 3.22.1+ (auto-installed by AGP)
- **JDK**: 17 or newer

### Build Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/smart-instrument.git
   cd smart-instrument
   ```

2. **Open in Android Studio**
   ```
   File → Open → Select the project root folder (Assolo)
   ```

3. **Sync Gradle**
   - Android Studio will automatically download dependencies
   - NDK and CMake will be installed if needed

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use the ▶️ Run button in Android Studio

### Build Variants

| Variant | Description |
|---------|-------------|
| `debug` | Development build with debugging enabled |
| `release` | Optimized build with ProGuard/R8 minification |

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint
```

---

## 🗺️ Roadmap

### Version 1.0 ✅
- [x] Low-latency audio engine with Oboe
- [x] Pentatonic scale grid UI
- [x] Multitouch support (8-voice polyphony)
- [x] Manual key selection
- [x] Multiple waveform types

### Version 2.0 ✅ (Current)
- [x] Audio file loading (SAF)
- [x] ExoPlayer integration for backing tracks
- [x] Independent volume controls (synth + track)
- [x] Automatic key detection (bass-focused algorithm for high accuracy)
- [x] Collapsible player panel
- [x] **Pitch Bend** - Guitar-style note bending with horizontal drag
- [x] **Vibrato** - Automatic vibrato after 700ms hold
- [x] **Blues Scale** - Blue notes (♭5/♭3) with distinct gold color

### Version 2.1 (Planned)
- [ ] Loop markers for backing tracks
- [ ] Chord detection display
- [ ] Scale selection (pentatonic, blues, modes)

### Version 3.0 (Future)
- [ ] MIDI output support
- [ ] Custom scale editor
- [ ] Recording and export
- [ ] SoundFont support for realistic instruments
- [ ] Bluetooth audio optimization

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting PRs.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes with clear commit messages
4. Ensure tests pass: `./gradlew test`
5. Submit a Pull Request

### Code Style

- **Kotlin**: Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **C++**: Follow [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- **Compose**: Follow [Compose API Guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md)

---

## 📄 License

```
MIT License

Copyright (c) 2025 Assolo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

- [Google Oboe](https://github.com/google/oboe) — High-performance audio library
- [TarsosDSP](https://github.com/JorenSix/TarsosDSP) — Audio analysis toolkit
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Modern Android UI toolkit
- [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/exoplayer) — Media playback library

---

<p align="center">
  Made with ❤️ for musicians everywhere
</p>

<p align="center">
  <a href="https://twitter.com/smartinstrument">Twitter</a> •
  <a href="https://discord.gg/smartinstrument">Discord</a> •
  <a href="mailto:support@smartinstrument.app">Support</a>
</p>
