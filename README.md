# BGX Music — Offline Android Music Player

Full Kotlin + Jetpack Compose + Media3 + Room + Hilt offline music player.

## What's included
- **Data layer**: Room database (songs, albums, artists, playlists, favorites,
  history, play counts), MediaStore scanner (offline, no network), DataStore
  preferences for every setting.
- **Playback engine**: Media3 ExoPlayer + MediaSessionService running as a
  foreground service, lock-screen/notification controls, Bluetooth/headset
  handling, audio focus, sleep timer, equalizer/bass boost/virtualizer with
  graceful per-device degradation.
- **UI**: Home, Songs, Albums, Artists, Playlists, Folders, Search, Now
  Playing, Queue, Equalizer, Settings — all in Jetpack Compose with
  Light/Dark/AMOLED/Dynamic-color theming, a persistent Mini Player, and
  runtime permission handling for Android 13+ vs older.
- **Branding**: adaptive launcher icon, splash screen, accent color #6C5CE7.

## Building the APK
The APK is built automatically by GitHub Actions (`.github/workflows/android-build.yml`).

- Push to `main` (or run the **Android APK Build** workflow manually from the
  Actions tab).
- The workflow installs JDK 17, the Android SDK (platform 35), builds the
  `debug` variant, and uploads the APK as the artifact **`BGXMusic-debug-apk`**.
- Download the artifact, unzip it, and install `app-debug.apk` on a device.

## To build locally
1. Open this folder in Android Studio (Koala or newer).
2. Let Gradle sync — it pulls Compose BOM 2024.09.03, Media3 1.4.1, Room
   2.6.1, Hilt 2.51.1 and friends.
3. Run on a device/emulator with local audio files (or add some via
   `adb push` to `/sdcard/Music`).

## Notes
- `minSdk 24`, `targetSdk 35`, `compileSdk 35`.
- The `debug` APK is signed with the standard debug key — fine for testing,
  not for store distribution.
- 100% offline: the app never sends data off the device.
