
# 📱 Ez Health – Android Mobile App


---

Ez Health is a smart Android application that empowers users to manage their health records, schedule appointments, receive nutrition advice, and access real-time health support — all in one place. The app integrates Firebase, AI recommendations, TensorFlow, and a chatbot for a modern, intelligent healthcare experience.

---

## 🚀 Features

- 📄 Upload and view personal health reports
- 🧠 AI-powered health recommendations
- 🤖 Integrated chatbot using Google Gemini for health queries
- 📅 Schedule and track appointments
- ⚖️ Calculate BMI and monitor health goals
- 🆘 Quick emergency call button
- 🔒 Firebase Authentication & Realtime Sync
- ☁️ Cloud Storage support for files (images & PDFs)

---

## 🧠 AI Modules

| Module | Description |
|--------|-------------|
| **Chatbot** | Built using Google Gemini Kit to provide conversational health support and general queries |
| **TensorFlow** | Used for health report image analysis and classification |
| **Nutritionist Model** | Python-based backend model (Streamlit + pandas) generates diet plans |

---


---

## 🛠 Tech Stack

| Technology                                                                                                               | Description                                    |
| ------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------- |
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white)                  | Main language for Android development          |
| ![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge\&logo=java\&logoColor=white)                        | Used in some legacy modules                    |
| ![XML](https://img.shields.io/badge/XML-E44D26?style=for-the-badge\&logo=xml\&logoColor=white)                           | UI Layouts                                     |
| ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge\&logo=firebase\&logoColor=black)            | Authentication, Realtime Database, and Storage |
| ![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge\&logo=tensorflow\&logoColor=white)      | Report image analysis                          |
| ![Google Gemini](https://img.shields.io/badge/Gemini%20Chatbot-4285F4?style=for-the-badge\&logo=google\&logoColor=white) | AI Chatbot integration                         |

---

## 📸 Screenshots

| Login & Home                                         | Report Upload                                          | Chatbot                                                  | Recommendations                                                          |
| ---------------------------------------------------- | ------------------------------------------------------ | -------------------------------------------------------- | ------------------------------------------------------------------------ |
| ![Login](https://raw.githubusercontent.com/MTalhaofc/Ez-Health-Android-App/refs/heads/main/ChatGPT%20Image%20Jun%2015%2C%202025%2C%2003_20_35%20PM.png) | ![Upload](https://raw.githubusercontent.com/MTalhaofc/Ez-Health-Android-App/refs/heads/main/WhatsApp%20Image%202025-06-15%20at%202.41.45%20PM.jpeg) | ![Chatbot](https://raw.githubusercontent.com/MTalhaofc/Ez-Health-Android-App/refs/heads/main/WhatsApp%20Image%202025-06-15%20at%202.41.44%20PM.jpeg) | ![Recommendations](https://raw.githubusercontent.com/MTalhaofc/Ez-Health-Android-App/refs/heads/main/WhatsApp%20Image%202025-06-15%20at%202.41.48%20PM.jpeg) |

---

## 📂 Project Structure

```bash
EzHealthApp/
├── app/
│   ├── java/com/ezhealth/    # App logic in Kotlin
│   ├── res/layout/           # UI screens (XML)
│   └── AndroidManifest.xml
├── firebase/                 # Firebase services
├── tensorflow/               # TensorFlow model loading
├── build.gradle
└── README.md
```

---

## ⚙️ Getting Started

### 1️⃣ Clone the Repository

```
git clone https://github.com/your-username/ez-health-app.git
cd ez-health-app
```

### 2️⃣ Open in Android Studio
```
* Open the project
* Sync Gradle dependencies
```
### 3️⃣ Firebase Setup
```
* Add your `google-services.json` to `/app`
* Enable Firebase Auth, Storage, and Realtime DB in your console
```
### 4️⃣ TensorFlow Setup
```
* Add your TFLite model under `/assets` folder
* Load and run inference using Android TFLite Interpreter
```
### 5️⃣ Run the App

```
Run ▶️ in Android Studio
```

---

## 📄 License

This project is licensed under the **MIT License**.

---

## 📬 Contact

For any queries, feel free to reach out via [LinkedIn](https://www.linkedin.com/in/mtalhaofc/)

---

## ⭐ Support

If you found this project useful or interesting, please consider giving it a ⭐ star and following me on GitHub for more cool projects!

[![GitHub Follow](https://img.shields.io/github/followers/MTalhaofc?label=Follow&style=social)](https://github.com/MTalhaofc)

---

> ⚡ **Ez Health – Personalized health support in your pocket.**




