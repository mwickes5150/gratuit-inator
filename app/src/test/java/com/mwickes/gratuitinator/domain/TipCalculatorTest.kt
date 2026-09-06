package com.mwickes.gratuitinator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TipCalculatorTest {

    private fun state(
        subtotal: String = "",
        tax: String = "",
        fullBill: Boolean = false,
        tipMode: TipMode = TipMode.Pct(20.0),
        people: Int = 1,
    ) = BillState(
        subtotalInput = subtotal,
        taxInput = tax,
        useFullBillAmount = fullBill,
        tipMode = tipMode,
        peopleCount = people,
    )

    // --- Basic calculation: tip on subtotal only ---

    @Test
    fun `tip is computed on subtotal only, never on tax`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "100", tax = "8", tipMode = TipMode.Pct(20.0)),
        )
        assertEquals(20.0, totals.tip, 0.001)
        assertEquals(8.0, totals.tax, 0.001)
        assertEquals(128.0, totals.grandTotal, 0.001)
    }

    @Test
    fun `grand total is subtotal plus tax plus tip`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "50", tax = "4.50", tipMode = TipMode.Abs(10.0)),
        )
        assertEquals(64.50, totals.grandTotal, 0.001)
    }

    @Test
    fun `useFullBillAmount computes tip on subtotal plus tax, without zeroing tax`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "100", tax = "8", fullBill = true, tipMode = TipMode.Pct(20.0)),
        )
        assertEquals(8.0, totals.tax, 0.001)
        assertEquals(21.6, totals.tip, 0.001)
        assertEquals(129.6, totals.grandTotal, 0.001)
    }

    @Test
    fun `blank tax defaults to zero`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "100", tax = "", tipMode = TipMode.Pct(20.0)),
        )
        assertEquals(0.0, totals.tax, 0.001)
        assertEquals(120.0, totals.grandTotal, 0.001)
    }

    @Test
    fun `malformed subtotal and tax are treated as zero, no crash`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "abc", tax = "!!", tipMode = TipMode.Pct(20.0)),
        )
        assertEquals(0.0, totals.subtotal, 0.001)
        assertEquals(0.0, totals.tax, 0.001)
        assertEquals(0.0, totals.tip, 0.001)
        assertEquals(0.0, totals.grandTotal, 0.001)
    }

    @Test
    fun `negative typed input is clamped to zero, not negative`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "-50", tax = "-5", tipMode = TipMode.Pct(20.0)),
        )
        assertEquals(0.0, totals.subtotal, 0.001)
        assertEquals(0.0, totals.tax, 0.001)
    }

    // --- Rounding ---

    @Test
    fun `round up lands grand total on the next whole dollar`() {
        val s = state(subtotal = "100", tax = "8", tipMode = TipMode.Pct(20.0)) // grand = 128.00
        val rounded = TipCalculator.roundTip(s, roundUp = true)
        val totals = TipCalculator.calculate(s.copy(tipMode = rounded))
        assertEquals(129.0, totals.grandTotal, 0.001)
        assertTrue(rounded is TipMode.Abs)
    }

    @Test
    fun `round down lands grand total on the previous whole dollar`() {
        val s = state(subtotal = "100", tax = "8", tipMode = TipMode.Pct(20.0)) // grand = 128.00
        val rounded = TipCalculator.roundTip(s, roundUp = false)
        val totals = TipCalculator.calculate(s.copy(tipMode = rounded))
        assertEquals(127.0, totals.grandTotal, 0.001)
    }

    @Test
    fun `round up from an already-whole grand total still advances a full dollar`() {
        val s = state(subtotal = "92", tax = "8", tipMode = TipMode.Pct(0.0)) // grand = 100.00
        val rounded = TipCalculator.roundTip(s, roundUp = true)
        val totals = TipCalculator.calculate(s.copy(tipMode = rounded))
        assertEquals(101.0, totals.grandTotal, 0.001)
    }

    @Test
    fun `round down never drives tip negative`() {
        val s = state(subtotal = "10", tax = "0", tipMode = TipMode.Pct(0.0)) // grand = 10.00
        val rounded = TipCalculator.roundTip(s, roundUp = false)
        val totals = TipCalculator.calculate(s.copy(tipMode = rounded))
        assertTrue(totals.tip >= 0.0)
        // subtotal+tax alone (10) already exceeds the whole dollar below (9), so tip clamps to
        // $0 instead of going negative — grand total can't drop below subtotal+tax.
        assertEquals(0.0, rounded.amount, 0.001)
        assertEquals(10.0, totals.grandTotal, 0.001)
    }

    @Test
    fun `round down clamps to zero tip rather than negative when base already exceeds target`() {
        // subtotal+tax alone (11) already exceeds the dollar below current grand (10)
        val s = state(subtotal = "11", tax = "0", tipMode = TipMode.Abs(0.40)) // grand = 11.40
        val rounded = TipCalculator.roundTip(s, roundUp = false)
        assertEquals(0.0, rounded.amount, 0.001)
    }

    // --- Stepper ---

    @Test
    fun `stepper increases tip by one dollar`() {
        val stepped = TipCalculator.stepTip(currentTip = 20.0, delta = 1.0)
        assertEquals(21.0, stepped.amount, 0.001)
    }

    @Test
    fun `repeated negative steps never drive tip below zero`() {
        var tip = 1.50
        repeat(5) {
            val stepped = TipCalculator.stepTip(currentTip = tip, delta = -1.0)
            tip = stepped.amount
        }
        assertEquals(0.0, tip, 0.001)
    }

    // --- The critical slider-resets-rounding regression ---

    @Test
    fun `onPercentChanged always produces Pct mode, even from an existing Abs mode`() {
        val fromAbs = TipCalculator.onPercentChanged(18.0)
        assertTrue(fromAbs is TipMode.Pct)
        assertEquals(18.0, fromAbs.pct, 0.001)
    }

    @Test
    fun `moving the slider after a round discards the rounded amount`() {
        var s = state(subtotal = "100", tax = "8", tipMode = TipMode.Pct(20.0))
        val rounded = TipCalculator.roundTip(s, roundUp = true) // Abs, grand=129
        s = s.copy(tipMode = rounded)

        val afterSlide = TipCalculator.onPercentChanged(18.0)
        s = s.copy(tipMode = afterSlide)

        val totals = TipCalculator.calculate(s)
        assertEquals(18.0, totals.tip, 0.001) // 100 * 18%, not derived from the rounded amount
    }

    // --- Implied tip % ---

    @Test
    fun `implied tip pct reflects an Abs-mode tip after round or step`() {
        val s = state(subtotal = "100", tax = "0", tipMode = TipMode.Abs(25.0))
        val totals = TipCalculator.calculate(s)
        assertEquals(25.0, totals.impliedTipPct, 0.001)
    }

    @Test
    fun `implied tip pct is zero when subtotal is zero, no divide-by-zero crash`() {
        val s = state(subtotal = "0", tax = "0", tipMode = TipMode.Abs(5.0))
        val totals = TipCalculator.calculate(s)
        assertEquals(0.0, totals.impliedTipPct, 0.001)
    }

    // --- Full bill toggle ---

    @Test
    fun `toggling full bill amount on preserves tax and resets to pct mode`() {
        val s = state(
            subtotal = "100",
            tax = "8",
            fullBill = false,
            tipMode = TipMode.Abs(25.0),
        )
        val toggled = TipCalculator.onToggleFullBillAmount(s)
        assertTrue(toggled.useFullBillAmount)
        assertEquals("8", toggled.taxInput)
        assertTrue(toggled.tipMode is TipMode.Pct)
    }

    @Test
    fun `toggling full bill amount off restores the tax value`() {
        val s = state(subtotal = "100", tax = "8", fullBill = true, tipMode = TipMode.Pct(20.0))
        val toggled = TipCalculator.onToggleFullBillAmount(s)
        assertTrue(!toggled.useFullBillAmount)
        assertEquals("8", toggled.taxInput)
    }

    @Test
    fun `toggling full bill amount recalculates tip and grand total live`() {
        val on = state(subtotal = "100", tax = "8", fullBill = true, tipMode = TipMode.Pct(20.0))
        val off = on.copy(useFullBillAmount = false)
        val onTotals = TipCalculator.calculate(on)
        val offTotals = TipCalculator.calculate(off)
        // On: tip = (100+8) * 20% = 21.6. Off: tip = 100 * 20% = 20.0 — different, not frozen.
        assertEquals(21.6, onTotals.tip, 0.001)
        assertEquals(20.0, offTotals.tip, 0.001)
    }

    // --- Split ---

    @Test
    fun `per person total divides grand total evenly across N people`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "100", tax = "8", tipMode = TipMode.Pct(20.0), people = 4), // grand=128
        )
        assertEquals(32.0, totals.perPersonTotal, 0.001)
    }

    @Test
    fun `people count of zero or negative is treated as one person, no crash`() {
        val totals = TipCalculator.calculate(
            state(subtotal = "100", tax = "0", tipMode = TipMode.Pct(0.0), people = 0),
        )
        assertEquals(100.0, totals.perPersonTotal, 0.001)
    }
}
