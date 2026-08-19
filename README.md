# 🪙 Expense Tracker

A professional, minimalist expense tracking application built with **Kotlin** and **Jetpack Compose**. Designed for users who value privacy, security, and a clean user experience.

---

## ✨ Features

- **📊 Smart Dashboard**: Track your balance, income, and expenses at a glance with beautiful progress indicators.
- **🛡️ Advanced Security**: Protect your financial data with Biometric authentication (Fingerprint/Face), PIN, Pattern, or Password.
- **🔥 Gamified Tracking**: Stay motivated with a daily logging streak banner.
- **📈 Detailed Insights**: Visualize your spending habits with interactive donut charts and daily bar graphs.
- **🏷️ Customizable Categories**: Organize transactions with vibrant colors and icons.
- **📥 Data Portability**: Export your transactions to CSV for external analysis.
- **🌑 Modern UI**: Full support for Light and Dark themes with smooth Material 3 transitions.
- **🔒 Privacy First**: All data is stored locally using Room Database. No cloud syncing, no data tracking.

## 🛠️ Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture**: MVVM with Clean Architecture principles
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Local Storage**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for preferences
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Security**: [AndroidX Biometric](https://developer.android.com/training/sign-in/biometric-auth)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1+)
- JDK 17
- Android Device/Emulator (API 26+)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/SoamyaDevelops/Expense-Tracker.git
   ```
2. Open the project in **Android Studio**.
3. Sync the project with Gradle files.
4. Run the app on your device or emulator.

## 📦 Project Structure

```text
app/src/main/java/com/expensetracker/app/
├── data/        # Room entities, DAOs, Database, Repositories
├── ui/          # UI Components
│   ├── home/    # Dashboard screen
│   ├── stats/   # Insights and Charts
│   ├── lock/    # Security screens (PIN/Biometric)
│   └── theme/   # Material 3 Theme definition
├── util/        # Helpers (Date, Icon, CSV, Hashing)
└── ExpenseApp   # Application class
```

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Developed with ❤️ by [Soamya](https://github.com/SoamyaDevelops)*
