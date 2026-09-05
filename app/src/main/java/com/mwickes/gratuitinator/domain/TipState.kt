package com.mwickes.gratuitinator.domain

/**
 * Discriminated union describing how the current tip amount was derived.
 *
 * This is the single source of truth for "how was the current tip amount derived" —
 * moving the Tip % slider (or tapping a preset chip) always produces [Pct] (recalculating
 * Tip = Subtotal x %), while rounding and the +/-$1 stepper always produce [Abs] (an amount
 * pinned directly, independent of subtotal changes until the next [Pct] action).
 */
sealed interface TipMode {
    data class Pct(val pct: Double) : TipMode
    data class Abs(val amount: Double) : TipMode
}

/**
 * Raw, UI-facing input state for the bill. Amount fields are kept as free-form strings so the
 * UI can hold partially-typed / blank / invalid input without losing keystrokes; [TipCalculator]
 * is responsible for parsing them defensively.
 */
data class BillState(
    val subtotalInput: String = "",
    val taxInput: String = "",
    val useFullBillAmount: Boolean = false,
    val tipMode: TipMode = TipMode.Pct(20.0),
    val peopleCount: Int = 1,
)

/**
 * Fully-resolved, calculated values derived from a [BillState] — safe for direct display.
 */
data class BillTotals(
    val subtotal: Double,
    val tax: Double,
    val tip: Double,
    val grandTotal: Double,
    val perPersonTotal: Double,
    val impliedTipPct: Double,
)
