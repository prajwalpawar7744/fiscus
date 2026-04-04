# Budgetear

A personal finance application built with Jetpack Compose and Material Design 3. Budgetear provides a modern, intuitive, and secure environment for managing individual financial records.

## Features

### Financial Management
*   **Dynamic Dashboard**: Comprehensive overview of real-time balance, income distributions, and expense summaries.
*   **Transaction Tracking**: Detailed history management with smart categorization and date-based grouping.
*   **Analytical Reporting**: Visual data representation through custom Bar, Line, and Pie charts using hardware-accelerated drawing.

### System Integration
*   **Adaptive Theme Engine**: Support for system-wide light and dark modes, with optional Dynamic Color integration for Android 12+ devices.
*   **Biometric Security**: Secure local authentication using the Android Biometric Framework.
*   **Data Persistence**: Full offline capability using Room database for relational data and DataStore for preferences.
*   **Backup & Migration**: Standardized JSON-based data export and import for seamless platform migration.

### Optimization & Engineering
*   **Pre-warmed Architecture**: ViewModels are initialized at the activity scope to ensure data availability before screen transitions.
*   **UI Performance**: Optimized staggered entrance animations using GPU-layer properties to bypass heavy layout passes.
*   **Code Optimization**: Configured with Baseline Profiles to improve cold startup time and eliminate frame drops.

## Technical Specifications

| Layer | Implementation |
|---|---|
| **Language** | Kotlin |
| **Framework** | Jetpack Compose (Material 3) |
| **Dependency Injection** | Hilt (Dagger) |
| **Database** | Room |
| **Concurrency** | Kotlin Coroutines & Flow |
| **Persistence** | DataStore Preferences |
| **Versioning** | Build-time dynamic VersionConfig |

## Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/prajwalpawar7744/budgetear.git
    ```
2.  **Environment Setup**: Open the project in the latest stable version of Android Studio.
3.  **Deployment**: Build and deploy to a device running Android 8.0 (API 26) or higher.
