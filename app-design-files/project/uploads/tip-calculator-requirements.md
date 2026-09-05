# Gratuit-inator — Requirements

## Overview
Ad-free rewrite of the current tip calculator app, renamed **Gratuit-inator**, adding tax-aware calculation, dollar-rounding with fine adjustment, and receipt photo scanning.

## Baseline (current app, for reference)
- **Bill Total** — manual entry
- **Tip $** — calculated from Bill Total × Tip %
- **Total** — Bill Total + Tip
- **Tip %** — slider (default 18%)
- **Split** — number of people (slider)
- **Split Tip / Split Total** — Tip and Total divided evenly across Split count
- **Round Total** — Down / Up buttons
- Ad banner, Upgrade / Share / Extra bottom nav

## New Requirements

### 1. Separate Subtotal and Tax
- Split the single "Bill Total" field into **Subtotal** (food/drink amount) and **Tax** (optional — defaults to $0 if left blank).
- **Tip % is calculated on Subtotal only**, never on Tax.
- **Grand Total = Subtotal + Tax + Tip**.
- If Tax is left blank, behavior collapses to the old model (Grand Total = Subtotal + Tip) — no forced tax entry.

### 2. Round to even dollar
- Down / Up buttons adjust the **tip amount** so that Grand Total lands on a whole dollar — not a separate line item, and not changed by editing Tip % after the fact.
- Works identically whether or not Tax was entered, since rounding always targets Grand Total (Subtotal + Tax + Tip).
- Displayed Tip % after rounding should update to reflect the new implied percentage (informational — it's no longer the driver of the tip amount at that point).
- **Moving the Tip % slider always resets and recalculates** — it discards any rounding/step adjustment and sets Tip = Subtotal × new %. This is the "fast path" past the calculated tip: if 20% isn't the dollar amount you want, drag the slider to get in the neighborhood instead of tapping +$1 repeatedly, then fine-tune with the stepper from there.

### 3. Step by $1 after rounding
- Once rounded, +$1 / −$1 controls let the user nudge Grand Total further in either direction.
- Each step adjusts the tip amount by $1.
- **Floor: tip cannot go below $0** (Grand Total can't drop below Subtotal + Tax). **No upper ceiling** — confirmed unbounded.

### 4. Split (updated)
- Split now divides **Subtotal + Tax + Tip** evenly across the person count, not just Total/Tip as before.
- Suggested per-person breakdown to display: Split Subtotal, Split Tax, Split Tip, Split Total — surfaces the same transparency the main screen now has. Flagging this as a proposed addition, not something you explicitly asked for — trim it back to just Split Total if you'd rather keep the per-person view simple.

### 5. Receipt photo scan
- **On-device only** — no image or extracted data leaves the phone. (Android's ML Kit Text Recognition is the natural fit: fully on-device, no network call.)
- Capture via camera or pick from gallery.
- Attempts to parse **Subtotal** and **Tax** from the recognized text (grand total on the receipt, minus tax, minus any pre-existing tip line, gives Subtotal; tax is usually printed as its own line).
- **Pre-fills the Subtotal/Tax fields with its best guess** — user reviews and edits before confirming, nothing is auto-committed.
- If it can't confidently find a value, leave that field blank with a "couldn't read this — enter manually" note rather than guessing silently.
- **Out of scope:** OCR should not attempt to read any tip-suggestion line printed on the receipt (e.g., "18% = $X") — Subtotal and Tax only.

### 6. "Use full bill amount" checkbox
- A checkbox on the main screen, **default unchecked**, labeled **"Use full bill amount"**.
- When checked: the full amount entered (or scanned) goes into **Subtotal**, and the **Tax field is disabled** — tip is then calculated on the whole bill, tax included.
- When unchecked (default): behaves per Requirement 1 — Subtotal and Tax stay separate, tip calculated on Subtotal only.
- Primary use case: scanning the receipt via camera but not wanting the subtotal/tax split — checking the box collapses the two fields into one so the full printed total gets tipped on.

### 7. Ads / monetization
- Remove the ad banner and any ad SDK entirely.
- Drop the old "Upgrade" button (no premium tier — ad-free is the whole point now).
- Add a **"Support the developer"** entry point (bottom nav, where Upgrade used to sit) that opens a Buy Me a Coffee (or similar) link in the external browser — not a webview, so it reads as a donation link rather than an in-app purchase.

