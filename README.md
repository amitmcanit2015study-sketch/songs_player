# 🎵 Songs Player by Amit Bharat

> **Modern Online & Offline Android Music Streaming and Playback Application**  
> Developed with ❤️ by **Amit Bharat** | **Rooys Soft Tech**  
> Email: `rooyssofttech2020@gmail.com`

---

## 📱 App Overview

**Songs Player** is a sleek, modern Android audio streaming and local playback application built with **Material Design 3**, **AndroidX Media3 (ExoPlayer)**, and **Room Database**. It provides unlimited online music discovery, real-time live search across international and Indian music catalogs, background streaming, and offline song/video downloading.

---

## ✨ Key Features & Functionality

### 1. 🧭 2-Tab Navigation (Clean & Intuitive Interface)
* **Online Tab:**
  * Real-time live search for any song, artist, album, or mix.
  * Instant category & genre filter chips:
    * *All*
    * *Indian classical music*
    * *90's Hindi Hits*
    * *Best Sufi Ghazal*
    * *Deep House Mix*
    * *Lo fi Beats*
    * *Focus & Work*
    * *Bollywood Mashup*
  * Wide 16:9 thumbnails with duration badge overlays in the bottom-right corner.
  * Endless infinite scroll pagination with visible scrollbars.
  * Pull-to-refresh to fetch fresh trending songs.
* **Offline Tab:**
  * Local media storage scanner via `MediaStoreScanner`.
  * Filter chips: *All Offline*, *Downloaded*, *Favorites*.
  * Search bar for offline music.
  * Dedicated **Active Downloads Card** showing live percentage (`Downloading: 45%`, `78%`, `100%`) with animated progress bar and file size.
  * Pull-to-refresh to rescan device storage.

### 2. 🎬 Smart Download Format Selector (MP3 Audio or MP4 Video)
* Whenever a user taps **Download**, a Material 3 bottom sheet dialog allows selecting:
  * 🎵 **Audio (.mp3 / 320kbps High Quality Audio)** – Fast download for audio-only listening (~4-8 MB).
  * 🎬 **Video (.mp4 / HD Video)** – Downloads the full HD video stream (~20-50 MB).
* Managed in the background via `WorkManager` and `DownloadWorker` with real-time percentage updates.

### 3. 🎛️ Full-Featured Audio Player
* **Mini-Player:** Persistent bottom mini-player across all screens with play/pause, next track, and favorite toggle.
* **Full-Screen Player:**
  * Album artwork with ambient glow.
  * Real-time seekbar with position/duration time indicators.
  * 5-band Equalizer with presets (*Bass Boost*, *3D Virtualizer*, *Loudness Enhancer*).
  * Sleep Timer (15, 30, 45, 60 minutes).
  * Variable Playback Speed (0.5x to 2.0x).
  * Repeat (Off, Repeat All, Repeat One) & Shuffle modes.
  * Real-time Queue management (reorder, remove, play next).

### 4. 🎨 Rich Aesthetic & Branded Screens
* **Splash Screen (Front Page):**
  * Full-screen deep indigo to vibrant purple gradient.
  * Centered squircle app icon, `Songs Player` title, `Online + Offline Music Player` subtitle.
  * Bottom branding: `Developed by Amit Bharat`.
* **Top Header:**
  * Two-line layout: **`Songs Player`** in bold 18.5sp, and `by Amit Bharat` on the second line.
* **About Page:**
  * Version: `v1.0.0 (Production Build)`.
  * Developer Card with company and email details.
  * Share App & Send Feedback actions.
  * Footer: `100% Free • No Ads • Zero Tracking`.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Language** | Java (JDK 17) |
| **UI Framework** | Android XML, Material Design 3, ConstraintLayout, SwipeRefreshLayout |
| **Audio Engine** | AndroidX Media3 ExoPlayer 1.3.1, MediaSessionService |
| **Local Database** | Room 2.6.1, SQLite, SharedPreferences |
| **Networking** | OkHttp 4.12.0, Retrofit 2.9.0, Gson 2.10.1 |
| **Image Loading** | Glide 4.16.0 |
| **Background Tasks** | AndroidX WorkManager 2.9.0 |

---

## 🔍 Playback Troubleshooting & Current Status

### Problem Description
* When attempting to play online songs, playback may appear stuck at `0:00` or fail to buffer.

### Root Cause Analysis
1. **Cloudflare / Proxy Verification Blocks:**
   * Third-party YouTube / Invidious proxy mirrors frequently return HTML challenge pages (`text/html`) instead of raw audio bytes (`audio/mp4`, `audio/mpeg`). ExoPlayer cannot decode HTML pages and stalls.
2. **Android Cleartext Network Security Restrictions:**
   * Android 9+ (API 28+) blocks non-HTTPS / cross-protocol HTTP connections by default unless `android:usesCleartextTraffic="true"` and a permissive `network_security_config.xml` are configured.

### Resolutions Applied
1. **Network Security Permissions:**
   * Added `android:usesCleartextTraffic="true"`, `android:requestLegacyExternalStorage="true"`, and `android:networkSecurityConfig="@xml/network_security_config"` in `AndroidManifest.xml`.
2. **Direct 320kbps Master Audio Streams:**
   * Connected `UniversalStreamEngine.java` to direct 320kbps CDNs (`SaavnAudioDecoder.java`) delivering raw, unblocked audio files directly into ExoPlayer.
3. **ExoPlayer Configuration:**
   * Configured `PlaybackService.java` with `DefaultHttpDataSource.Factory`, cross-protocol redirect handling (`setAllowCrossProtocolRedirects(true)`), and mobile user-agent headers.

---

## 🚀 How to Build and Run

1. Open the project in **Android Studio**.
2. Ensure the Android SDK location is configured in `local.properties`:
   ```properties
   sdk.dir=C\:\\Users\\Administrator\\AppData\\Local\\Android\\Sdk
   ```
3. Sync Gradle and click the green **Run** ▶️ button to build the debug APK.
