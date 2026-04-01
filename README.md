# AarogyaWhatsAI - Android App

Android wrapper for AarogyaWhatsAI web application. Push to GitHub and get APK automatically.

## How to Get APK (3 Steps)

### Step 1: Set Your URL

Open `app/src/main/java/com/aarogya/whatsai/MainActivity.kt` and change:

```kotlin
private val webUrl = "https://aarogya-whatsai.vercel.app"
//                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                    Replace with YOUR deployed URL
```

Also update the domain in `AndroidManifest.xml`:
```xml
<data android:host="aarogya-whatsai.vercel.app" />
<!--                 ^^^ your domain here       -->
```

### Step 2: Push to GitHub

```bash
git init
git add .
git commit -m "Initial Android app"
gh repo create AarogyaWhatsAI-Android --public --push
```

### Step 3: Download APK

1. Go to your GitHub repo
2. Click **Actions** tab
3. Click the latest "Build APK" workflow run
4. Scroll down to **Artifacts**
5. Download `AarogyaWhatsAI-debug` or `AarogyaWhatsAI-release`
6. Unzip and install the `.apk` on your phone

## Create a Versioned Release

To create a proper release with APK download link:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers GitHub to create a Release page with the APK attached.

## Features

- Full-screen WebView (no browser bar)
- Pull-to-refresh
- Offline detection with retry screen
- Back button navigation (goes back in web history)
- File upload support (patient documents)
- External links open in browser/WhatsApp
- WhatsApp-green themed splash + progress bar
- Adaptive app icon
- ProGuard minification for release builds

## For Signed Release APK (Play Store)

To publish on Google Play Store, you need a signed APK:

1. Generate a keystore:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias aarogya
```

2. Add to GitHub Secrets:
   - `KEYSTORE_BASE64` (base64 encode your .jks file)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

3. Update the release build in `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../release-key.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ... rest of config
    }
}
```

## Requirements

- Deployed AarogyaWhatsAI web app (Vercel/Railway)
- GitHub account (for Actions CI/CD)
- Android phone to test (or emulator)
