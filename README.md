# Motion Cues

An Android app that reduces motion sickness (kinetosis) by displaying animated dots along the edges of the screen. The dots move in sync with the vehicle's real-time acceleration and turns, creating an artificial horizon that helps your brain reconcile visual and vestibular signals.

Works as a system overlay on top of **any app** — read, watch videos, browse — while the dots subtly indicate vehicle motion in your peripheral vision.

## How It Works

1. **Sensor pipeline** — reads `TYPE_LINEAR_ACCELERATION` from the device accelerometer (gravity-filtered), with automatic fallback to raw accelerometer + manual gravity subtraction on devices that lack the fused sensor.
2. **Low-pass filter** — smooths out road vibrations and high-frequency noise, keeping only the vehicle's actual motion vectors.
3. **Spring physics** — drives dot offset with a damped spring model so dots drift smoothly and return to rest naturally.
4. **Overlay rendering** — draws semi-transparent dots via `Canvas.onDraw()` in a system overlay window (`TYPE_APPLICATION_OVERLAY`) on top of all other apps.

## Features

### Modes

| Mode | Description |
|---|---|
| **OFF** | Overlay disabled. |
| **ON** | Overlay always active. |
| **Auto** | Activity Recognition API (Google Play Services) automatically enables the overlay when it detects you're in a vehicle (`IN_VEHICLE`), and disables it when you leave. |

### Settings

Adjustable in real-time via sliders:

- **Sensitivity** — how strongly dots react to motion (0.2x - 2x)
- **Dot Opacity** — transparency level (10% - 90%)
- **Dot Size** — dot radius (2dp - 16dp)
- **Dot Count** — number of dots per edge (4 - 30)

Dots automatically invert color based on the system dark/light theme for visibility.

## Tech Stack

- **Language:** Kotlin
- **Settings UI:** Jetpack Compose + Material 3
- **Overlay rendering:** `WindowManager` + custom `View.onDraw(Canvas)` — not Compose (performance-critical path)
- **Background:** Foreground Service with `specialUse` type (Android 14+)
- **Sensors:** `SensorManager` — `TYPE_LINEAR_ACCELERATION` with fallback
- **Auto mode:** Activity Recognition Transition API (Google Play Services)
- **Min SDK:** 26 (Android 8.0) / **Target SDK:** 34 (Android 14)

## Architecture

```
app/src/main/java/com/motioncues/
├── MainActivity.kt                  # Permissions + Compose UI host
├── MotionCuesApp.kt                 # Application + notification channel
├── activity/
│   └── ActivityRecognitionManager.kt  # Transition API for IN_VEHICLE
├── overlay/
│   ├── DotOverlayManager.kt         # WindowManager lifecycle
│   └── DotOverlayView.kt            # Canvas-based dot rendering
├── sensors/
│   ├── SensorConfig.kt              # All tuning knobs in one data class
│   ├── SensorDataProcessor.kt       # Sensor → filter → spring → offset
│   └── SettingsStore.kt             # Singleton config shared with service
├── service/
│   └── MotionCuesService.kt         # Foreground service orchestrating everything
└── ui/
    ├── screens/MainScreen.kt        # Settings UI with mode selector + sliders
    └── theme/                       # Material 3 dynamic colors
```

### Key Design Decisions

- **Canvas over Compose for overlay** — `onDraw()` has zero UI-thread overhead; Compose would add unnecessary frames on a system overlay redrawn ~30 times per second.
- **Single `SensorConfig` data class** — all filter constants, spring parameters, and visual settings live in one place. The settings UI writes to `SettingsStore`, which propagates changes to the running service via a callback.
- **Spring physics instead of raw sensor values** — damped spring model produces organic-feeling motion that decays naturally, avoiding jarring jumps.
- **30 fps throttle** — sensor data arrives at ~16 Hz (`SENSOR_DELAY_UI`), overlay updates are capped at 30 fps to minimize CPU/GPU and battery impact.
- **Orientation-safe** — axis remapping handles portrait/landscape rotation; activity uses `configChanges` to avoid recreation.

## Build & Run

```bash
# Prerequisites: Java 17+ and Android SDK with platform 34
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=$HOME/Library/Android/sdk

./gradlew assembleDebug    # → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug     # build + install on connected device
```

## Required Permissions

| Permission | Why | Runtime prompt |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw overlay over other apps | Yes — special system settings screen |
| `FOREGROUND_SERVICE` | Keep the service alive in background | Manifest only |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Android 14+ foreground service type | Manifest + justification |
| `ACTIVITY_RECOGNITION` | Auto mode (detect IN_VEHICLE) | Yes — runtime permission |
| `POST_NOTIFICATIONS` | Foreground service notification | Yes — Android 13+ |

## License

MIT
