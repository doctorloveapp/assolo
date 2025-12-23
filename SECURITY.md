# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We take security seriously at Assolo. If you discover a security vulnerability, please report it responsibly.

### How to Report

1. **Do NOT** open a public GitHub issue for security vulnerabilities
2. Email us at: [security@smartinstrument.app](mailto:security@smartinstrument.app)
3. Include as much information as possible:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

### What to Expect

- **Acknowledgment**: Within 48 hours of your report
- **Initial Assessment**: Within 7 days
- **Resolution Timeline**: Depends on severity, typically 30-90 days
- **Credit**: We'll credit you in the release notes (unless you prefer anonymity)

### Scope

Security issues we're interested in:

- Unauthorized access to user data
- Code execution vulnerabilities in native code
- Privacy leaks
- Memory safety issues in C++ code
- JNI bridge vulnerabilities

### Out of Scope

- Issues requiring physical device access
- Social engineering attacks
- Denial of service on the local device
- Issues in third-party dependencies (report upstream)

## Security Measures

Assolo implements the following security practices:

### Code Security
- ProGuard/R8 obfuscation for release builds
- No hardcoded secrets or API keys
- Safe JNI boundary handling

### Data Security
- No network communication
- No data collection or transmission
- All processing happens locally

### Build Security
- Reproducible builds
- Dependency verification
- Signed releases

## Security Updates

Security patches are released as soon as possible after verification. Users are notified through:

- GitHub Releases
- Play Store update notes
- CHANGELOG.md

Thank you for helping keep Assolo secure! 🔒
