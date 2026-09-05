package com.mwickes.gratuitinator.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure calculation functions for the bill/tip/split state machine. No Android framework
 * imports — trivially unit-testable, and the highest-risk logic in the app per CLAUDE.md.
 *
 * Ported 1:1 from the design prototype's reference JS (see DEVELOPMENT_PLAN.md), but using
 * [BigDecimal] (2dp, HALF_UP) for all currency math instead of raw doubles/floats, to avoid
 * the float-boundary drift the JS reference is prone to. Doubles are used only at the UI
 * boundary (parsing typed input, exposing display totals).
 */
object TipCalculator {

    private val ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)

    /** Parses free-form user input into a non-negative, 2dp [BigDecimal]. Never throws. */
    private fun parseAmount(input: String): BigDecimal {
        val parsed = input.trim().toDoubleOrNull() ?: return ZERO
        if (parsed.isNaN() || parsed.isInfinite()) return ZERO
        val value = BigDecimal.valueOf(parsed).setScale(2, RoundingMode.HALF_UP)
        return if (value.signum() < 0) ZERO else value
    }

    private fun money(value: Double): BigDecimal {
        if (value.isNaN() || value.isInfinite()) return ZERO
        val scaled = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP)
        return if (scaled.signum() < 0) ZERO else scaled
    }

    private fun BigDecimal.orZeroFloor(): BigDecimal = if (this.signum() < 0) ZERO else this

    /** Tip amount (2dp, never negative) implied by [state]'s current [TipMode] and subtotal. */
    private fun tipAmount(subtotal: BigDecimal, tipMode: TipMode): BigDecimal = when (tipMode) {
        is TipMode.Pct -> {
            val pct = BigDecimal.valueOf(tipMode.pct)
            subtotal.multiply(pct).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).orZeroFloor()
        }
        is TipMode.Abs -> money(tipMode.amount)
    }

    /**
     * Resolves a [BillState] into display-ready [BillTotals]. Tip is computed on Subtotal only,
     * never on Tax. "Use full bill amount" forces Tax to $0 (Subtotal then represents the whole
     * bill, and tip is calculated on it).
     */
    fun calculate(state: BillState): BillTotals {
        val subtotal = parseAmount(state.subtotalInput)
        val tax = if (state.useFullBillAmount) ZERO else parseAmount(state.taxInput)
        val tip = tipAmount(subtotal, state.tipMode)
        val grandTotal = subtotal.add(tax).add(tip)
        val people = if (state.peopleCount < 1) 1 else state.peopleCount
        val perPerson = grandTotal.divide(BigDecimal(people), 2, RoundingMode.HALF_UP)
        val impliedTipPct = if (subtotal.signum() > 0) {
            tip.multiply(BigDecimal(100)).divide(subtotal, 4, RoundingMode.HALF_UP).toDouble()
        } else {
            0.0
        }
        return BillTotals(
            subtotal = subtotal.toDouble(),
            tax = tax.toDouble(),
            tip = tip.toDouble(),
            grandTotal = grandTotal.toDouble(),
            perPersonTotal = perPerson.toDouble(),
            impliedTipPct = impliedTipPct,
        )
    }

    /**
     * Rounds the Grand Total to the next whole dollar (up if [roundUp], else down) by adjusting
     * the tip amount directly — never touches Subtotal/Tax. Tip is clamped at $0; if the target
     * whole dollar would require a negative tip, the tip is set to $0 instead (round-down floor).
     * Always switches to [TipMode.Abs] — mirrors the JS reference's `roundTo`.
     */
    fun roundTip(state: BillState, roundUp: Boolean): TipMode.Abs {
        val subtotal = parseAmount(state.subtotalInput)
        val tax = if (state.useFullBillAmount) ZERO else parseAmount(state.taxInput)
        val base = subtotal.add(tax)
        val tip = tipAmount(subtotal, state.tipMode)
        val grand = base.add(tip)

        val target = if (roundUp) {
            grand.setScale(0, RoundingMode.FLOOR).add(BigDecimal.ONE)
        } else {
            grand.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE)
        }

        val newTip = target.subtract(base).orZeroFloor()
        return TipMode.Abs(newTip.setScale(2, RoundingMode.HALF_UP).toDouble())
    }

    /**
     * Nudges the current tip amount by [delta] dollars (e.g. +/-1.00 for the $1 stepper).
     * Clamped at $0 — tip never goes negative. Always switches to [TipMode.Abs] — mirrors the
     * JS reference's `step`.
     */
    fun stepTip(currentTip: Double, delta: Double): TipMode.Abs {
        val stepped = money(currentTip).add(BigDecimal.valueOf(delta).setScale(2, RoundingMode.HALF_UP))
        return TipMode.Abs(stepped.orZeroFloor().toDouble())
    }

    /**
     * A tip-% slider move or preset chip tap — always resets/recalculates to [TipMode.Pct],
     * discarding any prior rounding/stepping. This is the critical slider-resets-rounding
     * behavior CLAUDE.md calls out.
     */
    fun onPercentChanged(pct: Double): TipMode.Pct {
        val clamped = if (pct < 0.0) 0.0 else pct
        return TipMode.Pct(clamped)
    }

    /**
     * Toggling "use full bill amount": clears the (now hidden/shown) Tax field to avoid a stale
     * value silently reappearing later, and resets the tip mode to Pct (preserving the current
     * percentage if already in Pct mode, otherwise defaulting to 20%) — mirrors the JS
     * reference's `toggleFull`.
     */
    fun onToggleFullBillAmount(state: BillState): BillState {
        val resetPct = when (val mode = state.tipMode) {
            is TipMode.Pct -> mode.pct
            is TipMode.Abs -> 20.0
        }
        return state.copy(
            useFullBillAmount = !state.useFullBillAmount,
            taxInput = "",
            tipMode = TipMode.Pct(resetPct),
        )
    }
}
