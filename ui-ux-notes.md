# UI/UX Notes (local, not tracked as issues)

Running list of things found while trying out the app. Items below have been fixed.

## 1. "Use full bill amount" toggle clears Tax instead of recalculating — FIXED

- **Was:** Turning it on cleared Tax to 0.00 instead of folding it into the calculation, and it wasn't recovered when toggled back off. Tip stayed frozen rather than recomputing.
- **Fix:** The toggle now only changes *what the tip % is computed against* — a calculation-basis switch, not a data-clearing one.
  - **Off (default):** Tip = Subtotal × %.
  - **On:** Tip = (Subtotal + Tax) × %.
  - Tax is never zeroed out (still shown, disabled, and restored on toggle-off), and Grand Total/Tip recalculate live when the toggle changes. See `TipCalculator.calculate`/`onToggleFullBillAmount`.

## 2. Subtotal/Tax/Tip row alignment — FIXED

- **Was:** The label, "$", and numeric value weren't vertically inline — Material3 `TextField`'s built-in content padding sat the text above the field's own bottom edge.
- **Fix:** `TapeAmountField` now uses `BasicTextField` with no extra padding, bottom-aligned in the same row as the label, so label/`$`/amount share a baseline.

## 3. Auto-insert decimal point on numeric entry — FIXED

- **Was:** Amount fields took raw digit entry without formatting.
- **Fix:** Typing digits now auto-formats as currency from the right, cash-register/POS style — e.g. "123" → 1.23, "1542" → 15.42. See `formatDigitsAsAmount` in `TapeAmountField`.

## 4. Redundant app title shown twice — FIXED

- **Was:** "GRATUIT-INATOR" appeared both in the top app bar and again as a header line inside the paper tape.
- **Fix:** Removed the duplicate title line from inside the tape card.
