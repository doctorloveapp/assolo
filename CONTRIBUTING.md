# Contributing to Assolo

First off, thank you for considering contributing to Assolo! It's people like you that make this project a great tool for musicians everywhere.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How Can I Contribute?](#how-can-i-contribute)
- [Style Guidelines](#style-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to [support@smartinstrument.app](mailto:support@smartinstrument.app).

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or newer
- Git

### Setting Up Your Development Environment

1. **Fork the repository** on GitHub

2. **Clone your fork locally**
   ```bash
   git clone https://github.com/YOUR_USERNAME/smart-instrument.git
   cd smart-instrument
   ```

3. **Add the upstream remote**
   ```bash
   git remote add upstream https://github.com/smartinstrument/smart-instrument.git
   ```

4. **Open the project in Android Studio**
   - File → Open → Select the SmartInstrument folder
   - Wait for Gradle sync to complete

5. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples**
- **Describe the behavior you observed and what you expected**
- **Include device information** (model, Android version, etc.)
- **Include logs** if applicable

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description of the proposed feature**
- **Explain why this enhancement would be useful**
- **List any alternatives you've considered**

### Pull Requests

1. **Ensure your code compiles** without errors
2. **Write or update tests** for your changes
3. **Follow the style guidelines** below
4. **Update documentation** if needed
5. **Write clear commit messages**

## Style Guidelines

### Kotlin Code Style

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with these additions:

```kotlin
// Use meaningful names
val audioEngine: NativeAudioEngine  // ✅ Good
val ae: NativeAudioEngine           // ❌ Bad

// Use trailing commas for multi-line declarations
data class NoteInfo(
    val note: Note,
    val octave: Int,
    val frequency: Float,  // Trailing comma
)

// Prefer expression bodies for simple functions
fun isActive(): Boolean = envelope.isActive()

// Use named arguments for clarity
audioEngine.noteOn(
    voiceIndex = 0,
    frequency = 440f,
)
```

### C++ Code Style

We follow the [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html):

```cpp
// Use descriptive names with camelCase for functions
void AudioEngine::noteOn(int voiceIndex, float frequency) {
    // Implementation
}

// Use UPPER_CASE for constants
static constexpr int MAX_VOICES = 8;

// Always use braces for control structures
if (isActive) {
    processAudio();
}
```

### Jetpack Compose Style

Follow the [Compose API Guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md):

```kotlin
// Use PascalCase for Composable functions
@Composable
fun InstrumentGrid(
    notes: List<NoteInfo>,
    onNoteOn: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,  // Modifier should be the last parameter with default
) {
    // Implementation
}

// Hoist state when appropriate
@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Stateless implementation
}
```

## Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### Types

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that don't affect code meaning (formatting, etc.)
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `perf`: Performance improvement
- `test`: Adding or correcting tests
- `chore`: Changes to build process or auxiliary tools

### Examples

```
feat(audio): add triangle wave oscillator

fix(ui): resolve multitouch tracking issue on API 33+

docs: update README with build instructions

refactor(music): extract scale generation to separate module
```

## Pull Request Process

1. **Update the README.md** with details of changes if applicable

2. **Ensure all tests pass**
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

3. **Request review** from at least one maintainer

4. **Address review feedback** promptly

5. **Squash commits** if requested before merging

### PR Title Format

Use the same format as commit messages:
```
feat(audio): implement MIDI output support
```

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)

## Testing
Describe how you tested your changes

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented my code where necessary
- [ ] I have updated the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix/feature works
```

---

Thank you for contributing! 🎵
