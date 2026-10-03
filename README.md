# 🛡️ SmishGuard: Offline-First SMS Phishing Shield

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![ONNX](https://img.shields.io/badge/ONNX-005CED?style=for-the-badge&logo=onnx&logoColor=white)

SmishGuard is a privacy-first Android application engineered to intercept, analyze, and block malicious SMS messages (smishing) in real-time. Unlike traditional security applications that send private user texts to cloud servers for analysis, SmishGuard executes complex machine learning models **100% offline** directly on the device.

## 🚀 The Problem & Our Solution
Cloud-based AI scanners drain device battery, introduce network latency, and compromise user privacy by transmitting personal messages to remote servers. 

**SmishGuard solves this by bridging Python ML with Android mobile infrastructure:**
We trained a highly optimized **Naive Bayes classifier**, converted it into a universal `.onnx` blueprint, and embedded it directly into the Android application. Using the Microsoft ONNX Runtime, the app performs mathematical tensor calculations on the phone's local CPU in milliseconds, ensuring your private texts never leave your physical hardware.

## ✨ Key Features

*   **🧠 Local On-Device Inference:** Uses an embedded `.onnx` model to classify texts without an internet connection.
*   **🔋 Two-Tiered Processing System:** 
    *   *Tier 1 (Whitelist):* Intercepts incoming texts and cross-references the sender with the local Contacts database. Saved numbers safely bypass the AI, preserving battery and eliminating false positives.
    *   *Tier 2 (AI Scan):* Texts from unknown senders are silently routed through the AI engine in the background.
*   **🚨 Real-Time System Alerts:** Triggers high-priority Android notifications if a threat is detected, warning the user before they click malicious links.
*   **📊 Persistent Dashboard:** A clean, Material-inspired UI that logs scan history and threat statistics locally using `SharedPreferences`.
*   **🧪 Manual Sandbox:** Allows users to paste and test suspicious texts manually via the app interface.

## 🛠️ Technical Architecture

1.  **Machine Learning:** Scikit-learn (Naive Bayes & CountVectorizer) → `skl2onnx` → Frozen `smishguard.onnx` model.
2.  **Mobile Engine:** `onnxruntime-android` executes the frozen ML model natively within the Kotlin environment.
3.  **Background OS Integration:** Utilizes Android `BroadcastReceiver` to listen for system-level `SMS_RECEIVED` intents.

## 📱 Installation & Testing

Because this application intercepts live text messages, it requires high-level system permissions (`RECEIVE_SMS`, `READ_SMS`, `READ_CONTACTS`). 

1. Download the latest `app-debug.apk` from the releases page (or build directly via Android Studio / Gradle).
2. Sideload the APK onto your Android device. 
3. *Note:* Google Play Protect may flag the app as an "Unrecognized Developer" due to the sensitive SMS permissions. Tap **More Details -> Install Anyway**.
4. Launch the app and tap **Grant Background Shield Permissions**.
5. Send a test SMS to the device to view the background AI in action.

## 🎓 Why Naive Bayes?
While heavy neural networks (like BERT) are powerful for complex NLP tasks, they are entirely unsuited for background mobile processes. Naive Bayes was specifically selected because it relies on simple probability multiplication rather than massive continuous matrix calculations. This allows the model to wake up, classify an SMS, and shut down in milliseconds, creating a zero-lag experience that doesn't drain the user's battery.
