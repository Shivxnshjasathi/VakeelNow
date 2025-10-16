# VakeelNow: AI Legal Assistant for Android

**VakeelNow** is a sophisticated, multi-lingual AI-powered legal assistant for Android, built entirely with *Jetpack Compose*. It's designed to provide users with structured, informational analysis of legal situations based on Indian law. The app features a clean, modern user interface with both light and dark themes, robust localization, and text-to-speech capabilities.

## ✨ Key Features

  - **🤖 AI-Powered Chat:** Engage in a conversation with a legal AI to get detailed analysis of your legal queries.
  - **🇮🇳 Multi-Language Support:** Fully localized for **11 languages**, including English and major Indian languages, making legal information more accessible.
  - **🔊 Text-to-Speech (TTS):** Listen to the AI's responses with built-in TTS. Users can select from available voices for their chosen language.
  - **📝 Structured Legal Analysis:** The AI uses a sophisticated prompt to structure its responses, covering:
      - Initial & Final Disclaimers
      - Summary of Facts
      - Identification of Key Legal Issues
      - And more...
  - **🎨 Modern UI:** A beautiful and responsive interface built with `Jetpack Compose` and `Material 3` design principles.
  - **🌗 Light & Dark Themes:** Seamlessly switch between light and dark modes to suit your preference.
  - **📂 Local Chat History:** All conversations are saved on your device, allowing you to review them anytime.
  - **🔗 Share & Copy:** Easily copy individual messages or share entire conversations with others.
  - **⚖️ Find a Lawyer:** A convenient shortcut to search for local lawyers on Google.
  - **📄 Full Markdown Support:** AI responses are rendered with full markdown support for better readability.

## 🛠️ Tech Stack & Architecture

  - **Language:** [Kotlin](https://kotlinlang.org/)
  - **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
  - **Design System:** [Material 3](https://m3.material.io/)
  - **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
  - **Local Storage:** `SharedPreferences` for storing chat history and user preferences.
  - **Serialization:** [Gson](https://github.com/google/gson) for saving and loading conversation data.
  - **AI Backend:** Uses the `pollinations.ai` text generation API to power the chat.
  - **Text-to-Speech:** Android's native `TextToSpeech` engine.

## 🌐 Localization

The app is fully localized to support the following languages:

  - English (English)
  - Hindi (हिन्दी)
  - Bengali (বাংলা)
  - Gujarati (ગુજરાતી)
  - Punjabi (ਪੰਜਾਬੀ)
  - Kannada (ಕನ್ನಡ)
  - Malayalam (മലയാളം)
  - Odia (ଓଡ଼ିଆ)
  - Urdu (اردو)
  - Tamil (தமிழ்)
  - Telugu (తెలుగు)

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

  - Android Studio Iguana | 2023.2.1 or later.
  - Android SDK targeting API level 34.

### Installation

1.  **Clone the repo**
    ```bash
    git clone https://github.com/your_username/ailegalassistant.git
    ```
2.  **Open in Android Studio**
      - Open Android Studio.
      - Select `File > Open` and navigate to the cloned repository directory.
      - Let Android Studio sync and build the project.
3.  **Run the App**
      - Select an emulator or connect a physical device.
      - Click the 'Run' button.

### Disclaimer

> This application is intended for informational and educational purposes only. The AI-generated content does not constitute legal advice, and no attorney-client relationship is formed by using this app. For formal legal advice, please consult a qualified legal professional.

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📜 License

Distributed under the MIT License. See `LICENSE.txt` for more information.
