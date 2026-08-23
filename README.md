# PazEnCasa

Android app that gamifies caring for your relationship. Log affectionate gestures — kind words, sweets, flowers, dates, jewelry — and watch a live "happiness meter" and daily streak react in real time.

## Features

- **Login / registration** with email & password (Firebase Authentication), including password change, re-authentication, and account deletion
- **Happiness meter** — a weighted score computed from recent actions (words said today, sweets this week, flowers/jewelry over longer windows), shown with a mood icon and color that shifts from happy to furious
- **Streaks** — tracks consecutive days with at least one logged action
- **Smart suggestions** — contextual tips like *"No le dijiste nada lindo hoy"* based on what's missing
- **Action history** — full log of past actions in a scrollable list
- **Playful random events** — occasional pop-ups that react to your stats (with a bit of humor)

## Tech stack

- **Language:** Java
- **Auth:** Firebase Authentication
- **Persistence:** Room (local action history)
- **UI:** Android Views, RecyclerView, Material Components
- **Architecture:** Activities + DAO, background work on `ExecutorService`

## Running the project

1. Open the project in Android Studio
2. Sync Gradle (`./gradlew build`)
3. Run on an emulator or device with `minSdk 24+`

The included `google-services.json` points to the project's own Firebase instance, so Auth works out of the box for local builds.
