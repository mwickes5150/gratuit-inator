# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This repository currently contains only documentation (`README.md`, `tip-calculator-requirements.md`) — no Android project has been scaffolded yet (no `build.gradle`, `settings.gradle`, or `app/` module). The first substantial task here will likely be creating the Gradle/Kotlin/Compose project structure from scratch. Do not assume standard Android build files or directories exist without checking first.

## What this project is

Gratuit-inator is an ad-free Android tip calculator, planned as a rewrite of an existing app. Intended stack (per README.md): Kotlin + Jetpack Compose, Android SDK/Gradle, and on-device ML Kit Text Recognition for receipt scanning.

Once the project is scaffolded, expect the standard commands to apply:
- `./gradlew assembleDebug` — build a debug APK
- `./gradlew installDebug` — build and install to a connected/USB-debugging device
- `./gradlew test` — unit tests
- `./gradlew connectedAndroidTest` — instrumented tests on a device/emulator

## Requirements spec

`tip-calculator-requirements.md` is the authoritative feature spec — read it in full before implementing calculator logic. Key points that drive the architecture:

- **Tip is computed on Subtotal only, never on Tax.** Grand Total = Subtotal + Tax + Tip. Tax is entered separately and defaults to $0 (blank Tax collapses behavior to the old Bill-Total-only model).
- **"Use full bill amount" checkbox** collapses Subtotal+Tax into a single field (disables Tax) so tip is calculated on the whole bill.
- **Rounding and the $1 stepper adjust the tip amount directly** (not a separate line item), targeting Grand Total. Moving the Tip % slider always discards any rounding/stepping and recalculates Tip = Subtotal × %. Tip cannot go below $0; no upper ceiling.
- **Split divides Subtotal + Tax + Tip** evenly, not just Total/Tip as in the old app.
- **Receipt scanning is fully on-device** (ML Kit) — no image or extracted data may leave the phone. It only pre-fills Subtotal/Tax for user review (never auto-commits), and must not attempt to parse any tip-suggestion line printed on the receipt.
- No ads, no ad SDK, no in-app "Upgrade" purchase — only an external-browser link to a donation page (e.g. Buy Me a Coffee), not a webview.

When implementing, keep the tip/rounding/stepper/slider state machine centralized (a single source of truth for "how was the current tip amount derived") since the reset-on-slider-move vs. persists-through-Subtotal/Tax-edits behavior is easy to get subtly wrong.
