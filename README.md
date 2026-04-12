# Fiscus

A personal finance application built with Jetpack Compose and Material Design 3. Fiscus provides a modern, intuitive, and secure environment for managing individual financial records.

## Features

### Financial Management
*   **Dynamic Dashboard**: Comprehensive overview of real-time balance, income distributions, and expense summaries.
*   **Account Management**: Support for multiple financial accounts with dedicated balance tracking and custom icons.
*   **Transaction Tracking**: Detailed history management with smart categorization and date-based grouping.
*   **Custom Categories**: Create, edit, and delete personalized spending/income categories with a rich icon and color library.
*   **Analytical Reporting**: Visual data representation through custom Bar, Line, and Pie charts using hardware-accelerated drawing.

### System Integration & Customization
*   **Privacy Mode**: A toggleable global masking mode that blurs or masks all sensitive balance and transaction amounts—perfect for use in public places.
*   **Adaptive Header Styles**: Support for **Standard** and **Dynamic Expanding (Large)** top bar styles throughout the application.
*   **Advanced Theme Engine**: 
    *   Full support for system-wide **Light and Dark modes**.
    *   **Dynamic Color** integration for Android 12+ (Material You).
    *   **Premium Accent Palettes**: Curated presets like **Emerald**, **Indigo**, **Sapphire**, and **Crimson** for when dynamic color is disabled.
*   **Biometric Security**: Secure local authentication using the Android Biometric Framework.
*   **Data Persistence**: Full offline capability using Room database with schema migration support and DataStore for persistent preferences.
*   **Backup & Migration**: Standardized JSON-based data export and import for seamless platform migration.

### Optimization & Engineering
*   **Pre-warmed Architecture**: ViewModels initialized at optimal scopes to ensure zero-latency data availability during transitions.
*   **UI Performance**: Fluid staggered entrance animations using GPU-layer properties to ensure a buttery-smooth 120Hz experience.
*   **Code Optimization**: Configured with Baseline Profiles to minimize startup time and eliminate frame drops.

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
    git clone https://github.com/prajwalpawar7744/fiscus.git
    ```
2.  **Environment Setup**: Open the project in the latest stable version of Android Studio.
3.  **Deployment**: Build and deploy to a device running Android 8.0 (API 26) or higher.
