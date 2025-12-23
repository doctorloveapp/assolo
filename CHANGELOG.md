# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Audio file loading via Storage Access Framework
- ExoPlayer integration for backing track playback
- Automatic key detection using TarsosDSP
- Loop markers for practice sections

---

## [1.0.3] - 2025-12-23

### Added
- Initial public release
- Complete pentatonic scale instrument grid
- Low-latency audio engine with Oboe (C++)
- 8-voice polyphony with multitouch support
- 4 waveform types: Sine, Sawtooth, Square, Triangle
- ADSR envelope for smooth note transitions
- Manual key selection (all 12 keys, Major/Minor)
- Configurable grid rows (5, 7, 10, 12 notes)
- Dark theme optimized for stage use
- Professional README and documentation

---

## [1.0.0] - 2025-01-XX

### Added

#### Audio Engine
- Native C++ audio engine built with Google Oboe for ultra-low latency
- Sub-10ms touch-to-sound latency on supported devices
- 8-voice polyphony for full multitouch chord support
- ADSR envelope (Attack: 10ms, Decay: 50ms, Sustain: 70%, Release: 100ms)
- Four oscillator waveforms: Sine, Sawtooth, Square, Triangle
- Master volume control with real-time adjustment

#### Musical Features
- Pentatonic scale generation for any key (Major and Minor)
- All 12 root notes supported (C, C#, D, D#, E, F, F#, G, G#, A, A#, B)
- Configurable grid rows (5, 7, 10, or 12 notes)
- Proper music theory implementation with accurate frequency calculation
- MIDI note number to frequency conversion (A4 = 440Hz standard)

#### User Interface
- Modern Material3 design with Jetpack Compose
- Touch-optimized instrument grid with visual feedback
- Real-time note highlighting on touch
- Optional note labels showing pitch name and frequency
- Dark theme optimized for stage/studio use
- Collapsible settings panel for distraction-free playing
- Responsive layout supporting various screen sizes

#### Platform Support
- Minimum SDK: Android 8.0 (API 26)
- Target SDK: Android 15 (API 35)
- Supported ABIs: armeabi-v7a, arm64-v8a, x86, x86_64
- Optimized for both phones and tablets

### Technical Details
- Kotlin 2.0.21 with Jetpack Compose
- C++17 with Oboe 1.9.0
- CMake 3.22.1 build system
- ProGuard rules for release builds

---

## Version History

| Version | Date | Highlights |
|---------|------|------------|
| 1.0.0 | 2025-01 | Initial release with core features |

---

[Unreleased]: https://github.com/smartinstrument/smart-instrument/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/smartinstrument/smart-instrument/releases/tag/v1.0.0
