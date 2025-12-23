<p align="center">
  <img src="docs/assets/logo.png" alt="Assolo Logo" width="140" height="140">
</p>

<h1 align="center">🎸 Assolo</h1>

<p align="center">
  <strong>The intelligent blues instrument — always in key, always in tune</strong>
</p>

<p align="center">
  <em>Transform any backing track into your personal jam session. Zero wrong notes. Pure expression.</em>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-demo">Demo</a> •
  <a href="#-download">Download</a> •
  <a href="#-how-it-works">How It Works</a> •
  <a href="#%EF%B8%8F-architecture">Architecture</a> •
  <a href="#-build">Build</a> •
  <a href="#-contributing">Contributing</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Version-2.8.9-E94560?style=for-the-badge&logo=semantic-release&logoColor=white" alt="Version">
  <img src="https://img.shields.io/badge/Android-8.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform">
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Language">
  <img src="https://img.shields.io/badge/C++-17-00599C?style=for-the-badge&logo=cplusplus&logoColor=white" alt="C++">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Audio_Engine-Oboe_1.9-FF6B6B?style=flat-square" alt="Oboe">
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square" alt="Compose">
  <img src="https://img.shields.io/badge/Playback-Media3_ExoPlayer-34A853?style=flat-square" alt="ExoPlayer">
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License">
</p>

---

## 🎵 What is Assolo?

**Assolo** is a revolutionary Android application that turns your smartphone into an intelligent musical instrument designed for **blues and pentatonic improvisation**.

Unlike traditional instrument apps, Assolo **automatically detects the key** of your backing track and displays **only the notes that sound good** — eliminating dissonant notes and enabling anyone to play like a pro.

### The Magic ✨

1. 🎧 **Load any backing track** (MP3, WAV, AAC)
2. 🔍 **Assolo analyzes the audio** and detects the musical key
3. 🎹 **Play the blues scale** — every note sounds perfect
4. 🎸 **Bend notes like a guitar** with horizontal finger slides
5. 🎤 **Add vibrato automatically** by holding notes

<p align="center">
  <img src="docs/assets/screenshot_main.png" alt="Assolo Screenshot" width="280">
</p>

---

## 🚀 Features

### 🎹 Smart Note Grid

| Feature | Description |
|---------|-------------|
| **Blues Scale** | 6-note scale with authentic "blue notes" (♭5 for minor, ♭3 for major) |
| **Zero Wrong Notes** | Only harmonically correct notes are displayed |
| **Visual Distinction** | Blue notes highlighted in gray with azure labels for easy identification |
| **Configurable Rows** | Choose 10, 15, or 18 rows to match your playing style |
| **Note Labels** | Optional display of note names and frequencies |

### 🎸 Expressive Playing

| Feature | Description |
|---------|-------------|
| **Pitch Bend** | Horizontal drag to bend notes up to 2 semitones (guitar-style) |
| **Auto Vibrato** | Hold a note for 700ms and vibrato kicks in automatically (~8Hz) |
| **8-Voice Polyphony** | Play full chords with multitouch support |
| **Visual Feedback** | Notes change color when bent, show ↗ indicator with bend amount |

### 🏛️ 5 Professional Instrument Sounds

| Sound | Technology | Character |
|-------|------------|-----------||
| **⚡ Guitar** | Oscillator + Multi-stage Tube Distortion | Crunchy electric guitar with configurable sustain, gain, distortion & reverb |
| **🎹 Organ** | Hammond B3 Additive Synthesis | Warm, full drawbar sound (888800000 preset) |
| **🎷 Synth** | Classic Sawtooth | Bright, buzzy synth lead |
| **⬛ Square** | Square Wave | Hollow, retro 8-bit character |
| **🎸 Bass** | Fender P-Bass Oscillator | Deep, punchy bass with natural harmonics |

### 🎸 Dunlop Cry Baby Wah Pedal

The Guitar sound features a **realistic Cry Baby wah pedal**:

| Feature | Description |
|---------|-------------|
| **Auto Mode** | LFO-driven sweep oscillates automatically |
| **Manual Mode** | Touch-controlled pedal position |
| **Swipe Control** | Horizontal finger drag simulates foot movement |
| **Visual Feedback** | Animated pedal tilts following finger position |
| **Bandpass Sweep** | Authentic 400-2000 Hz frequency range |

### 🎵 Built-in Backing Tracks

Assolo includes **3 professional backing tracks** ready to jam:

- **Blues in E** — Classic 12-bar blues shuffle
- **Blues Shuffle in G** — Upbeat shuffle feel  
- **Slow Blues in A** — Expressive slow blues

All tracks are **automatically analyzed** for key detection when selected.

### 🔍 Intelligent Key Detection

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Audio File     │ ──▶ │  Bass-Focused    │ ──▶ │  Krumhansl-     │
│  (30 seconds)   │     │  Low-Pass Filter │     │  Schmuckler     │
│                 │     │  (40-300 Hz)     │     │  Key Profiles   │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                    Isolates bass frequencies
                    where root note is strongest
```

- **Bass-focused analysis**: Filters 40-300 Hz where bass plays the root
- **Async processing**: Non-blocking MediaCodec decoding
- **High accuracy**: Avoids harmonic confusion by ignoring upper frequencies
- **30-second analysis**: Extended sample for reliable detection

### 🎧 Professional Backing Track Player

- **Format Support**: MP3, WAV, AAC, FLAC via Media3 ExoPlayer
- **Built-in Tracks**: 3 blues backing tracks included (E, G, A)
- **Load from File**: Import your own backing tracks
- **Auto Key Detection**: Built-in tracks are automatically analyzed
- **Transport Controls**: Play, Pause, Stop, Seek
- **Independent Mixing**: Separate volume for track (default 80%) and synth (default 50%)
- **Collapsible Panel**: Hide player for maximum grid space

---

## 📱 Download

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.smartinstrument.app">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" width="220">
  </a>
</p>

### Requirements

| Requirement | Minimum |
|-------------|---------|
| **Android Version** | 8.0 Oreo (API 26) |
| **Architecture** | ARM64-v8a, ARMv7, x86, x86_64 |
| **Storage** | ~15 MB |
| **RAM** | 2 GB recommended |

---

## 🎯 How It Works

### The Blues Scale Advantage

The **blues scale** is the foundation of blues, rock, and jazz improvisation. It's a 6-note scale that adds the characteristic "blue note" to the minor pentatonic:

```
Minor Blues:  1 - ♭3 - 4 - ♭5 - 5 - ♭7
              R    m3   P4  dim5  P5  m7

Major Blues:  1 - 2 - ♭3 - 3 - 5 - 6
              R   M2   m3   M3  P5  M6
```

The blue note (♭5 in minor, ♭3 in major) creates that **signature blues tension** — and in Assolo, these notes are visually highlighted so you can target them for maximum expression.

### Touch-to-Sound Pipeline

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐     ┌──────────────┐
│ Touch Event │ ──▶ │ Kotlin/JNI   │ ──▶ │ C++ Oscillator  │ ──▶ │ Audio Output │
│             │     │ Bridge       │     │ + ADSR Envelope │     │              │
└─────────────┘     └──────────────┘     └─────────────────┘     └──────────────┘
     ~1ms               ~0.5ms                 ~8ms                  Real-time

     ↕ Vertical = Note Selection
     ↔ Horizontal = Pitch Bend (0 to +2 semitones)
```

**Total latency: < 10ms** — imperceptible to human hearing.

---

## 🏗️ Architecture

### Technology Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **UI Framework** | Jetpack Compose | Material3 | Declarative, reactive UI |
| **Audio Engine** | Google Oboe | 1.9.0 | Ultra-low-latency C++ audio |
| **Media Playback** | Media3 ExoPlayer | 1.5.0 | Professional audio/video playback |
| **Key Detection** | Custom Algorithm | — | Bass-focused autocorrelation |
| **Build System** | Gradle + CMake | 8.x / 3.22 | Multi-language build orchestration |
| **Language** | Kotlin + C++17 | 2.0 / 17 | JVM + Native performance |

### Audio Engine: Electric Guitar Synthesis

The guitar sound uses **oscillator-based synthesis** with a professional effects chain:

```cpp
// Electric Guitar Signal Chain
1. Oscillator Mix (Saw 70% + Square 30% with slight detune)
2. Single-coil Pickup Filter (high-pass 120Hz, low-pass 5kHz)
3. Multi-stage Tube Distortion (soft clipping with harmonics)
4. Cry Baby Wah Pedal (bandpass 400-2000Hz sweep)
5. Plate Reverb (3 comb filters with diffusion)
6. ADSR Envelope (configurable sustain)
```

### Audio Engine: Fender P-Bass Synthesis

The bass sound emulates a **Fender Precision Bass**:

```cpp
// P-Bass Oscillator Mix
sample = 0.7f * saw + 0.3f * square;  // Fat, punchy tone
// Low-pass filter at 1200Hz for warmth
// Subtle compression for punch
```

### Audio Engine: Hammond B3 Organ Synthesis

The organ sound uses **additive synthesis** with the classic drawbar configuration:

```cpp
// Hammond B3 "888800000" Jazz Preset
sample += sin(phase * 0.5);   // 16' Sub-octave
sample += sin(phase * 1.5);   // 5⅓' Quint
sample += sin(phase);         // 8' Fundamental
sample += sin(phase * 2.0);   // 4' Octave
sample = tanh(sample * 1.2);  // Subtle overdrive (key click)
```

### Project Structure

```
Assolo/
├── app/src/main/
│   ├── cpp/                          # 🔊 Native Audio Engine
│   │   ├── AudioEngine.cpp/h         # Oboe stream management
│   │   ├── Oscillator.cpp/h          # Waveform generators (5 types)
│   │   ├── ADSREnvelope.cpp/h        # Amplitude envelope
│   │   └── native-lib.cpp            # JNI bridge
│   │
│   ├── java/.../app/
│   │   ├── audio/
│   │   │   ├── NativeAudioEngine.kt  # JNI wrapper
│   │   │   ├── TrackPlayer.kt        # ExoPlayer wrapper
│   │   │   └── KeyDetector.kt        # Key detection algorithm
│   │   │
│   │   ├── music/
│   │   │   ├── MusicalKey.kt         # Key/Scale models
│   │   │   └── PentatonicScale.kt    # Blues scale generation
│   │   │
│   │   └── ui/
│   │       ├── components/
│   │       │   ├── InstrumentGrid.kt # Touch grid + gestures
│   │       │   └── Controls.kt       # Settings UI
│   │       ├── screens/
│   │       │   └── MainScreen.kt     # Main composable
│   │       └── theme/                # Material3 theming
│   │
│   └── res/
│       └── drawable/                 # Icons, backgrounds
│
├── docs/assets/                      # Screenshots, logo
├── gradle/libs.versions.toml         # Version catalog
└── README.md                         # You are here
```

---

## 🔧 Build

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **Android Studio** | Ladybug (2024.2.1)+ | Arctic Fox minimum |
| **JDK** | 17+ | Bundled with Android Studio |
| **Android NDK** | r25c+ | Auto-installed by AGP |
| **CMake** | 3.22.1+ | Auto-installed by AGP |

### Quick Start

```bash
# Clone
git clone https://github.com/AlessandroGiannetti/Assolo.git
cd Assolo

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or build release
./gradlew assembleRelease
```

### Build Variants

| Variant | Minification | Debuggable | Use Case |
|---------|--------------|------------|----------|
| `debug` | ❌ | ✅ | Development |
| `release` | ✅ R8 | ❌ | Production |

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint

# Full verification
./gradlew check
```

---

## 🗺️ Roadmap

### ✅ Version 1.x — Foundation
- [x] Oboe-based low-latency audio engine
- [x] Pentatonic scale grid
- [x] 8-voice polyphony with multitouch
- [x] Manual key selection
- [x] Basic waveforms (Sine, Saw, Square, Triangle)

### ✅ Version 2.x — Expression (Current: 2.8.9)
- [x] Backing track player with ExoPlayer
- [x] Automatic key detection (bass-focused algorithm)
- [x] Guitar-style pitch bend
- [x] Automatic vibrato (700ms hold trigger)
- [x] Blues scale with highlighted blue notes
- [x] **Hammond B3 organ** sound (additive synthesis)
- [x] **Electric Guitar** with multi-stage tube distortion
- [x] **Dunlop Cry Baby Wah pedal** (auto & manual modes)
- [x] **Fender P-Bass** oscillator-based synthesis
- [x] **Plate Reverb** effect (3 comb filters)
- [x] **Built-in backing tracks** (3 blues tracks)
- [x] Independent volume controls
- [x] Configurable row count (10/15/18)
- [x] Guitar settings dialog (sustain, gain, distortion, reverb)

### 🔮 Version 3.x — Pro Features (Planned)
- [ ] Loop markers for backing tracks
- [ ] Chord detection display
- [ ] MIDI output support
- [ ] Custom scale editor
- [ ] Recording and export (WAV/MP3)
- [ ] Effects: Delay, Chorus, Phaser
- [ ] Bluetooth audio optimization

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md).

### Development Workflow

```bash
# 1. Fork & clone
git clone https://github.com/YOUR_USERNAME/Assolo.git

# 2. Create feature branch
git checkout -b feature/amazing-feature

# 3. Make changes & test
./gradlew check

# 4. Commit with conventional commits
git commit -m "feat: add amazing feature"

# 5. Push & open PR
git push origin feature/amazing-feature
```

### Code Style

| Language | Style Guide |
|----------|-------------|
| **Kotlin** | [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) |
| **C++** | [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html) |
| **Compose** | [Compose API Guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md) |

---

## 📄 License

```
MIT License

Copyright (c) 2025 Alessandro Giannetti

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

| Project | Use |
|---------|-----|
| [Google Oboe](https://github.com/google/oboe) | High-performance audio I/O |
| [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/exoplayer) | Media playback |
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | Modern UI toolkit |
| [Dunlop Cry Baby](https://www.jimdunlop.com/cry-baby/) | Wah pedal inspiration |
| [Fender P-Bass](https://www.fender.com) | Bass tone reference |

---

<p align="center">
  <img src="https://img.shields.io/badge/Made_with-❤️-E94560?style=for-the-badge" alt="Made with love">
</p>

<p align="center">
  <strong>Built for musicians, by musicians</strong>
</p>

<p align="center">
  <a href="https://github.com/AlessandroGiannetti/Assolo/issues">Report Bug</a> •
  <a href="https://github.com/AlessandroGiannetti/Assolo/issues">Request Feature</a> •
  <a href="https://github.com/AlessandroGiannetti/Assolo/discussions">Discussions</a>
</p>

<p align="center">
  ⭐ Star this repo if Assolo helps you make music!
</p>
