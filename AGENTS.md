# AGENTS.md — Motion Cues

## Project

Android app that reduces motion sickness by drawing animated dots along screen edges,
offset by real-time vehicle acceleration/turns (via device sensors), overlaid on top of any app.

## Tech Stack

- **Language:** Kotlin
- **Settings UI:** Jetpack Compose
- **Overlay rendering:** `WindowManager` + custom `View.onDraw(Canvas)` — NOT Compose (performance-critical)
- **Background:** Foreground Service (system kills overlay without it)
- **Sensors:** `SensorManager` — `TYPE_LINEAR_ACCELERATION` + `TYPE_GYROSCOPE`
- **Auto mode:** Activity Recognition API (Google Play Services, `IN_VEHICLE`)

## Architecture Constraints

- Overlay is a system overlay (`TYPE_APPLICATION_OVERLAY`), drawn on Canvas via `onDraw()`.
  Do NOT rewrite it in Compose — Canvas is chosen for frame-rate and zero UI-thread overhead.
- Sensor pipeline: raw data → low-pass / Kalman filter → spring animation → dot offset.
  Filtering and spring constants live in one place; tweak there, not scattered.
- Axis mapping depends on device orientation (portrait vs landscape).
  X sensor → lateral (turns), Y/Z sensor → longitudinal (accel/brake) — but which is Y vs Z flips with rotation.
- Overlay window flags: `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE` — must never intercept user input.

## Required Permissions

| Permission | Why | Runtime prompt |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw overlay over other apps | Yes — special settings screen |
| `FOREGROUND_SERVICE` | Keep service alive | Manifest only |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Android 14+ foreground type | Manifest + justification |
| `ACTIVITY_RECOGNITION` | Auto mode (detect IN_VEHICLE) | Yes — runtime permission |
| `POST_NOTIFICATIONS` | Foreground service notification | Yes — Android 13+ |

## Build Order (Roadmap)

1. **MVP** — Activity + Foreground Service + overlay permission + static dots on screen
2. **Sensors** — SensorManager → filter → animate dots from acceleration data
3. **Auto mode** — Activity Recognition API → start/stop overlay on `IN_VEHICLE`
4. **Polish** — orientation handling, dark/light theme inversion, settings UI

## Build & Run

```bash
# Requires Java 17+ (openjdk@17 via Homebrew) and Android SDK with platform 34
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/Users/andrew696/Library/Android/sdk

./gradlew assembleDebug              # build debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug               # build + install on connected device
```

## Key Gotchas

- `SYSTEM_ALERT_WINDOW` cannot be requested via normal permission flow — user must be sent to system Settings overlay page.
- Android 14+ requires `foregroundServiceType` in manifest AND must declare a specific type; use `specialUse` with justification.
- Low-pass filter alpha and spring stiffness are tuning knobs — keep them in a single config/data class so settings UI can adjust them.
- `TYPE_LINEAR_ACCELERATION` is software-fused on most devices (gravity removed). If unavailable on a device, fall back to accelerometer + manual gravity subtraction.
