# Gratuit-inator — Phased Development Plan

## Context

The repo has requirements (`tip-calculator-requirements.md`) and a bare Android/Compose Gradle scaffold (stock "Empty Activity" template — no calculator logic, no ViewModels, default purple Material3 theme). The user just added `app-design-files/`, a Claude Design canvas (`Gratuit-inator.dc.html`) containing three interactive visual directions for the same 4 screens (Main/Scan/Review/Split), plus a companion "Industry" design-token CSS system.

This plan turns that scaffold + spec + design file into a real app, broken into phases sized for separate coding sessions. Decisions locked in with the user before writing this plan:
- **Visual direction: "1c – Paper Tape"** only (receipt/monospace aesthetic, dashed rules, in-app paper/steel light-dark toggle, ±$1 stepper promoted as the primary control). 1a/1b are not being built — their shared calculation logic (below) is used only as an algorithm reference.
- **CI included early** (Phase 5) — GitHub Actions running `./gradlew test`/`assembleDebug` on push/PR.
- **Split screen shows only the aggregate "each person pays" total** — no per-person Subtotal/Tax/Tip breakdown, matching all three mockups (the requirements doc's fuller breakdown is explicitly not being built).

Package root: `com.mwickes.gratuitinator`. Proposed structure, built incrementally:
```
com.mwickes.gratuitinator/
  MainActivity.kt
  GratuitinatorApp.kt              # top-level Scaffold + NavHost (Phase 4)
  domain/
    TipCalculator.kt                # pure calc functions (Phase 1)
    TipState.kt                     # sealed TipMode: Pct/Abs, BillState (Phase 1)
  data/
    ReceiptParser.kt                # OCR text -> Subtotal/Tax heuristics (Phase 7)
  ocr/
    TextRecognitionClient.kt        # ML Kit wrapper (Phase 7)
  ui/
    theme/
      Color.kt, Theme.kt, Type.kt   # existing, replaced Phase 0
      PaperTape.kt                  # paper/steel tokens (Phase 0)
    components/
      DashedDivider.kt, TapeAmountField.kt, StepperBlock.kt, PctPresetRow.kt
    screens/
      main/MainScreen.kt, MainViewModel.kt
      split/SplitScreen.kt
      scan/ScanScreen.kt, ScanViewModel.kt
      review/ReviewScreen.kt, ReviewViewModel.kt
  navigation/
    NavGraph.kt, Destinations.kt
```

### Reference calculation logic (ported from the design prototype's embedded JS in `Gratuit-inator.dc.html` — use as the exact algorithm to translate to Kotlin)
```js
calc(o): sub = parseFloat(o.sub)||0; tax = o.full ? 0 : parseFloat(o.tax)||0;
  tip = max(0, mode==='pct' ? sub*pct/100 : abs); grand = sub+tax+tip;
roundTo(dir): base = sub+tax; target = dir>0 ? floor(grand+ε)+1 : ceil(grand-ε)-1; abs = max(0, target-base); mode='abs'
step(d): abs = max(0, round((tip+d)*100)/100); mode='abs'
onPct/preset chip click: mode='pct' (always resets/recalculates)
toggleFull: full = !full; resets active field + mode='pct'
```
Tip state is a discriminated union — `{mode: Pct, pct}` or `{mode: Abs, abs}` — this is the "single source of truth for how tip was derived" CLAUDE.md calls for.

---

## Phase 0 — Project Hygiene & Theme Foundation

**Goal:** Clean scaffold, establish Paper Tape design tokens (light "paper" + dark "steel"), remove placeholder cruft.

**Steps:**
1. Strip stock `Greeting`/preview from `MainActivity.kt`; have it just host a stub `GratuitinatorApp()`.
2. Replace `ui/theme/Color.kt` with Paper Tape tokens derived from `app-design-files/project/_ds/industry-*/styles.css`: paper/light bg `#f2f2f3`, surface `#e9e9ea`, ink `#1d1f20`, accent `#5980a6`; steel/dark = inverted dark-charcoal bg with off-white tape surface.
3. Add `ui/theme/PaperTape.kt`: a `CompositionLocal` of tape-specific roles (ink, dim, rule/dashed-divider color, cardBg, pageBg, accent, stepBg/stepFg) alongside a matching Material3 `ColorScheme` for standard widgets.
4. Update `Type.kt`: monospace family for numeric displays (stand-in for ui-monospace/Menlo), condensed sans for headers/labels; define `displayLarge` (grand total), `titleMedium`, `labelSmall` (uppercase letter-spaced labels).
5. Update `Theme.kt`: `GratuitinatorTheme(darkSteel: Boolean, content)` — explicit boolean, not OS-theme-driven (paper/steel is an app feature, not system dark mode); `dynamicColor = false` always.
6. Delete `ExampleUnitTest.kt` / `ExampleInstrumentedTest.kt` placeholders.
7. Set `app_name` to "Gratuit-inator" in `strings.xml`, drop unused stock strings.

**New dependencies:** none.

**Acceptance criteria:**
- App builds/launches showing an empty paper-toned screen; a temporary debug toggle flips paper/steel correctly (readable in both).
- No leftover stock purple theme or `Greeting` composable.
- `./gradlew assembleDebug` succeeds.

**Open decision:** default the paper/steel toggle from `isSystemInDarkTheme()` on first launch, then let the in-app toggle override thereafter (persist later in Phase 8). Confirm before Phase 2 locks in the UI.

---

## Phase 1 — Core Calculation State Machine (pure Kotlin, no UI)

**Goal:** Port `calc`/`roundTo`/`step`/`onPct`/`toggleFull` to Kotlin exactly, fully unit-tested before any UI touches it — highest-risk logic per CLAUDE.md.

**Steps:**
1. `domain/TipState.kt`:
   ```kotlin
   sealed interface TipMode {
       data class Pct(val pct: Double) : TipMode
       data class Abs(val amount: Double) : TipMode
   }
   data class BillState(
       val subtotalInput: String = "",
       val taxInput: String = "",
       val useFullBillAmount: Boolean = false,
       val tipMode: TipMode = TipMode.Pct(20.0),
       val peopleCount: Int = 1,
   )
   data class BillTotals(
       val subtotal: Double, val tax: Double, val tip: Double,
       val grandTotal: Double, val perPersonTotal: Double, val impliedTipPct: Double,
   )
   ```
2. `domain/TipCalculator.kt` — pure functions mirroring the JS 1:1: `parseAmount`, `calculate(state): BillTotals`, `roundTip(state, direction): TipMode.Abs` (with `1e-9` epsilon like the JS), `stepTip(currentAbs, delta): TipMode.Abs`, `onPercentChanged(pct): TipMode.Pct` (always resets mode), `onToggleFullBillAmount(state): BillState`.
   - Use `BigDecimal` (2dp, `HALF_UP`) internally for currency math instead of raw doubles — avoids float-boundary drift the JS reference has; convert to `Double`/`String` only at UI boundaries.
3. `app/src/test/java/.../domain/TipCalculatorTest.kt` covering: tip never applied to tax; `useFullBillAmount` forces tax to 0; blank tax → $0; round up/down lands Grand Total on a whole dollar and never drives tip negative; repeated ±$1 steps never go below $0 tip; **`onPercentChanged` always produces `Pct` mode even from `Abs`** (the critical slider-resets-rounding regression test); `impliedTipPct` correctness after Abs-mode round/step; per-person split math across N; divide-by-zero/malformed-input safety.

**New dependencies:** none (`kotlin-stdlib`, `java.math.BigDecimal`).

**Acceptance criteria:** `./gradlew test` green; module compiles standalone with zero Android framework imports (pure, trivially testable).

**Open decision:** when "use full bill amount" is toggled back off, should the previously-entered Tax value be cleared or preserved? Recommend clearing (avoids stale hidden values reappearing) — confirm before Phase 2.

---

## Phase 2 — Main Screen UI (Paper Tape), wired to the state machine

**Goal:** Build the "Bill" screen per the 1c mockup, backed by a `MainViewModel` wrapping Phase 1's pure functions.

**Steps:**
1. Add `androidx.lifecycle:lifecycle-viewmodel-compose` dependency.
2. `ui/screens/main/MainViewModel.kt`: `MutableStateFlow<BillState>` → derived `StateFlow<BillTotals>` via `calculate()`; action functions (`onSubtotalChanged`, `onTaxChanged`, `onToggleFullBillAmount`, `onPctPreset`, `onSliderChanged`, `onRoundDown`, `onRoundUp`, `onStepMinus1`, `onStepPlus1`) calling into `TipCalculator` — the single source of truth.
3. Reusable components: `DashedDivider.kt` (Canvas-drawn dashed rule), `TapeAmountField.kt` (right-aligned mono `$` numeric field), `StepperBlock.kt` (promoted large −/+ tip block + implied-% caption shown only in Abs mode), `PctPresetRow.kt` (15/18/20/25 chips, active-highlight only when `tipMode is Pct` matching that value).
4. `ui/screens/main/MainScreen.kt`: header w/ dark-mode toggle → tape card (Subtotal/Tax/Tip/dashed rule/Total + use-full-bill row) → stepper block → Round Down/Round Up row → tip-% card (preset chips + slider 0–40).
5. Host `MainScreen` from `MainActivity`/`GratuitinatorApp` temporarily as the only screen.
6. Compose UI test (`MainScreenTest.kt`): typing Subtotal updates Tip/Total; full-bill toggle disables Tax field; Round Down/Up shows implied "≈" %; **moving the slider after a round resets tip to % mode** (UI-level regression test for the same critical behavior).

**New dependencies:** `androidx.lifecycle:lifecycle-viewmodel-compose`.

**Acceptance criteria:** manual check — Subtotal 100, Tax 8, 20% chip → Tip 20.00/Total 128.00; Round Up → Total 129.00 with implied %; +$1 → 130.00; slider to 18 → Tip resets to 18.00 (not 22.00). Visual layout matches the 1c mockup order. `./gradlew test` + instrumented tests pass.

---

## Phase 3 — Split Feature

**Goal:** Dedicated Split screen — aggregate "each person pays" total only, ± people stepper.

**Steps:**
1. Promote `BillState`/the ViewModel to be **shared across screens** (instantiated once in `GratuitinatorApp`, passed down) rather than separate per-screen state — avoids "Split shows stale numbers." This decision also drives Phase 4's nav setup.
2. `ui/screens/split/SplitScreen.kt`: centered "EACH PERSON PAYS" card (large mono total), dashed rule, `grandTotal ÷ N` caption, ± stepper (floor at 1 person, 44dp+ touch targets) — no `SplitViewModel` needed, just add `incrementPeople`/`decrementPeople` to the shared ViewModel.
3. Tests: people-count floor at 1; per-person total recalculates on any Subtotal/Tax/Tip change; Compose UI test for +/- taps.

**Acceptance criteria:** Split shows only the aggregate total (no per-person breakdown line); editing Main then visiting Split shows live numbers without extra taps, validating the shared-state design.

---

## Phase 4 — Navigation & Bottom Nav

**Goal:** Wire all screens into one shell with the bottom bar (`BILL / SPLIT / SCAN`).

**Steps:**
1. Add `androidx.navigation:navigation-compose`.
2. `navigation/Destinations.kt` (Main, Split, Scan, Review — Review reachable only from Scan, not a bottom-nav tab) + `navigation/NavGraph.kt`.
3. Rewrite `GratuitinatorApp.kt`: `Scaffold` with a custom flat `Row`-based bottom bar (matches mockup better than rounded M3 `NavigationBar`) with tabs for Bill/Split/Scan. (A "TIP US ↗" Support tab is a **future enhancement**, not built in this phase — see the note below.)
4. Nav test: Split tab shows live shared numbers; back-stack sane (`popUpTo`/`launchSingleTop` on tab taps).

**New dependencies:** `androidx.navigation:navigation-compose`.

**Acceptance criteria:** all destinations reachable except Review (post-scan only).

> **Future enhancement — Support / "Buy Me a Coffee" link (requirement 7):** the requirements doc calls for replacing the old ad/Upgrade button with a "TIP US ↗" bottom-nav entry that opens an external browser Intent to a Buy-Me-a-Coffee-style donation page (never a WebView). This is being deliberately deprioritized — it may land whenever a real donation URL exists, or much later, or not at all. It's a self-contained addition (one nav item + one `Intent(ACTION_VIEW, ...)` call guarded by try/catch for the no-browser case) that can be slotted into the bottom bar at any point without touching the rest of the app. No placeholder URL/constant needs to exist until this is actually picked up.

---

## Phase 5 — CI Setup (GitHub Actions)

**Goal:** Automated build/test gating on push/PR, placed here (after a real test suite exists from Phases 1–4) per the user's request to include CI early.

**Steps:**
1. `.github/workflows/android-ci.yml`: trigger on push to `main` + PRs into `main`; `ubuntu-latest`, `actions/checkout@v4`, `actions/setup-java@v4` (Temurin — confirm JDK version AGP 9.4.0 requires, likely 17+), `gradle/actions/setup-gradle@v4` for caching; run `./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, optionally `./gradlew lint`. Skip `connectedAndroidTest` initially (needs an emulator runner — flag as a future enhancement via `reactivecircus/android-emulator-runner`).
2. Add a CI status badge to `README.md`.
3. Verify `gradlew` is executable on Linux runners (`git update-index --chmod=+x gradlew` — common Windows-authored-repo gotcha).

**Acceptance criteria:** a push/PR runs the workflow and passes; confirm it actually gates by breaking a test on a throwaway branch if desired.

---

## Phase 6 — Receipt Scan UI (capture/gallery, no OCR yet)

**Goal:** Scan screen shell + get a photo `Uri` into memory; OCR logic deferred to Phase 7 to isolate risk.

**Steps:**
1. **Use intent-based capture** (`ActivityResultContracts.TakePicture` + `FileProvider` cache `Uri`), not CameraX — the mockup shows a static viewfinder placeholder, not a live custom preview, so the system Camera app is sufficient and far simpler. (Flag: CameraX would give a true live in-app viewfinder if the user wants closer visual fidelity later.)
2. Manifest: `FileProvider` declaration + `res/xml/file_paths.xml` for the temp receipt cache file. No `CAMERA` permission needed for intent-based capture.
3. `ui/screens/scan/ScanViewModel.kt`: `capturedImageUri: StateFlow<Uri?>`, `onCaptureClick()`/`onGalleryClick()` triggering the respective launchers.
4. `ui/screens/scan/ScanScreen.kt` per mockup: viewfinder placeholder, "ON-DEVICE OCR · OFFLINE" caption, Capture/Gallery buttons; launchers registered via `rememberLauncherForActivityResult` in the composable.
5. Wire into `NavGraph.kt`: successful capture/pick navigates to Review (stub until Phase 7) with the `Uri`.
6. **Verify `AndroidManifest.xml` has zero `INTERNET`/network permissions** — this is what makes the "nothing leaves the phone" claim enforceable, not just policy.

**Acceptance criteria:** Capture and Gallery buttons both return a valid `Uri`; no network permissions anywhere in the manifest.

**Open decision:** confirm intent-based capture (recommended) vs. CameraX before starting.

---

## Phase 7 — ML Kit OCR + Review Screen + Parsing Heuristics

**Goal:** On-device text recognition on the captured image, heuristic Subtotal/Tax extraction (never tip lines), Review screen for confirm/edit — never auto-commits.

**Steps:**
1. Add `com.google.mlkit:text-recognition` (on-device variant) + `kotlinx-coroutines-android`/`core`.
2. `ocr/TextRecognitionClient.kt`: wraps `TextRecognition.getClient(...)`, `suspend fun recognize(uri): Result<Text>`.
3. `data/ReceiptParser.kt` — pure function `parseReceipt(lines: List<String>): ParsedReceipt` (testable without mocking ML Kit types):
   - Match `/subtotal/i`, `/tax/i` (+ variants) lines, extract nearest currency-looking number.
   - **Explicitly exclude/ignore `/tip|gratuity|suggested/i` lines** — never parse a suggested-tip line, per requirement 6.
   - Optional fallback: derive `subtotal = total - tax` if "Subtotal" isn't printed but Total+Tax are found.
   - Return `null` per field when not confidently matched (never guess) — drives the "couldn't read this" hint.
4. `ui/screens/review/ReviewViewModel.kt`: runs OCR+parse on the passed `Uri`, exposes editable Subtotal/Tax fields pre-filled from parse results (blank if null) plus readability flags per field.
5. `ui/screens/review/ReviewScreen.kt`: "WHAT WE READ" card, editable fields with hint styling when unreadable, disclosure text that tip lines are ignored by design, Retake (→ Scan) / "Use These" (commits into shared `BillViewModel`, navigates to Main) buttons. **OCR results only reach real bill state via explicit "Use These" tap.**
6. `data/ReceiptParserTest.kt`: standard Subtotal/Tax lines parse; a "Suggested Tip 20% $8.43" line is never captured; Total-minus-Tax fallback (if implemented); garbled/unreadable text → both null.
7. Manual/on-device test with real receipt photos, including **airplane mode** to directly verify the no-network claim.

**New dependencies:** `com.google.mlkit:text-recognition`, `kotlinx-coroutines-android`, `kotlinx-coroutines-core`.

**Acceptance criteria:** OCR works fully offline; parser tests pass (especially the tip-line-exclusion case); Review never silently commits; unreadable fields are visibly distinct.

**Open note:** parsing heuristics need real-world tuning against actual receipt formats — treat Phase 7 as a first pass, not a one-shot solve. Keep `ReceiptParser` isolated/well-tested so it's easy to iterate later.

---

## Phase 8 — Polish / Theming Detail Pass + App Icon

**Goal:** Close the gap to full mockup fidelity; real app icon.

**Steps:**
1. Pixel-fidelity pass (spacing, letter-spacing, dashed-rule rendering, ink-tinted card shadows) against the 1c mockup.
2. Optionally bundle real Barlow/Barlow Condensed fonts if closer fidelity is wanted.
3. Replace stock launcher icon assets with a real receipt/tape-motif icon (needs a design asset — flag as a follow-up, possibly via the `design` skill).
4. Persist the paper/steel toggle across restarts (`androidx.datastore:datastore-preferences`).
5. Accessibility pass: content descriptions on icon-only buttons, 44dp+ touch targets, contrast check in both modes.
6. Fill in any remaining Scan/Review Compose UI tests.

**Acceptance criteria:** visual side-by-side parity with the mockup; real app icon in launcher; theme choice survives restart; no critical accessibility issues.

---

## Phase 9 — Release Readiness

**Goal:** Prepare a real signed build.

**Steps:**
1. Release signing config in `app/build.gradle.kts` — keystore/passwords via `gradle.properties`/CI secrets, never committed. **Confirm with the user whether Play Store distribution is a goal** vs. personal sideload-only — changes scope materially.
2. Enable R8 minification for release (currently disabled) + add ML Kit's required keep rules to `app/src/main/keepRules/rules.keep`.
3. Decide versioning scheme (simple bump vs. semver/tags).
4. Final manifest audit: zero network permissions, correctly scoped camera/storage permissions.
5. Extend CI to run `lint` + an unsigned `assembleRelease` smoke build on PRs.
6. Tag `v1.0.0` once all phases' acceptance criteria are met.

**Acceptance criteria:** signed release build runs correctly with R8 on (verify ML Kit + Compose survive obfuscation); CI green on `main`; no secrets committed.

---

## New dependencies by phase
| Phase | Dependency |
|---|---|
| 2 | `androidx.lifecycle:lifecycle-viewmodel-compose` |
| 4 | `androidx.navigation:navigation-compose`, (test) `espresso-intents` |
| 7 | `com.google.mlkit:text-recognition`, `kotlinx-coroutines-android`, `kotlinx-coroutines-core` |
| 8 | (optional) `androidx.datastore:datastore-preferences`, Google Fonts Compose |

## Open decisions to confirm along the way
1. **Phase 0:** paper/steel toggle — default from system theme, then manual override persists.
2. **Phase 1:** toggling "use full bill amount" off — clear or preserve the hidden Tax value? (recommend clear)
3. **Phase 3/4:** single shared `BillViewModel` across screens (recommended) vs. per-screen state.
4. **Phase 6:** intent-based capture (recommended) vs. CameraX for a true live viewfinder.
5. **Phase 7:** receipt-parsing heuristics will need iterative real-world tuning.
6. **Phase 9:** Play Store distribution vs. sideload-only.
7. **Future enhancement (unscheduled):** Support / "Buy Me a Coffee" bottom-nav link (requirement 7) — deprioritized, may be picked up much later or not at all. See the note under Phase 4.

## Critical files
- `app/src/main/java/com/mwickes/gratuitinator/domain/TipCalculator.kt`
- `app/src/main/java/com/mwickes/gratuitinator/domain/TipState.kt`
- `app/src/main/java/com/mwickes/gratuitinator/ui/screens/main/MainViewModel.kt`
- `app/src/main/java/com/mwickes/gratuitinator/navigation/NavGraph.kt`
- `app/src/main/java/com/mwickes/gratuitinator/data/ReceiptParser.kt`
- `app/build.gradle.kts`, `gradle/libs.versions.toml`

## Verification
Each phase lists its own acceptance criteria (unit tests via `./gradlew test`, instrumented Compose UI tests via `./gradlew connectedAndroidTest`, and manual on-device checks). Phase 5 (CI) then gates unit tests + `assembleDebug` automatically on every push/PR from that point forward.
