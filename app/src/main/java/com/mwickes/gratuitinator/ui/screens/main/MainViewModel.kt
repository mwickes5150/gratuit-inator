package com.mwickes.gratuitinator.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwickes.gratuitinator.domain.BillState
import com.mwickes.gratuitinator.domain.BillTotals
import com.mwickes.gratuitinator.domain.TipCalculator
import com.mwickes.gratuitinator.domain.TipMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Wraps [BillState] and drives it exclusively through [TipCalculator] — the single source of
 * truth for "how was the current tip amount derived" that CLAUDE.md calls for. No calculation
 * logic lives here; every action function below just calls into the pure domain layer and
 * replaces [state].
 */
class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(BillState())
    val state: StateFlow<BillState> = _state

    val totals: StateFlow<BillTotals> = _state
        .map { TipCalculator.calculate(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TipCalculator.calculate(_state.value),
        )

    fun onSubtotalChanged(value: String) {
        _state.value = _state.value.copy(subtotalInput = value)
    }

    fun onTaxChanged(value: String) {
        _state.value = _state.value.copy(taxInput = value)
    }

    fun onToggleFullBillAmount() {
        _state.value = TipCalculator.onToggleFullBillAmount(_state.value)
    }

    /** Resets Subtotal/Tax/Tip/full-bill-toggle back to their defaults for a fresh bill. */
    fun onClear() {
        _state.value = BillState()
    }

    fun onPctPreset(pct: Double) {
        _state.value = _state.value.copy(tipMode = TipCalculator.onPercentChanged(pct))
    }

    fun onSliderChanged(pct: Double) {
        _state.value = _state.value.copy(tipMode = TipCalculator.onPercentChanged(pct))
    }

    fun onRoundDown() {
        _state.value = _state.value.copy(tipMode = TipCalculator.roundTip(_state.value, roundUp = false))
    }

    fun onRoundUp() {
        _state.value = _state.value.copy(tipMode = TipCalculator.roundTip(_state.value, roundUp = true))
    }

    fun onStepMinus1() {
        _state.value = _state.value.copy(
            tipMode = TipCalculator.stepTip(totals.value.tip, delta = -1.0),
        )
    }

    fun onStepPlus1() {
        _state.value = _state.value.copy(
            tipMode = TipCalculator.stepTip(totals.value.tip, delta = 1.0),
        )
    }

    /** Split people-count controls, floored at 1 (no upper ceiling). */
    fun incrementPeople() {
        _state.value = _state.value.copy(peopleCount = _state.value.peopleCount + 1)
    }

    fun decrementPeople() {
        _state.value = _state.value.copy(peopleCount = (_state.value.peopleCount - 1).coerceAtLeast(1))
    }

    /** Current tip-mode percentage for slider/chip display: the live [TipMode.Pct] value, or the calculated implied % while in [TipMode.Abs]. */
    fun currentPctForDisplay(): Double = when (val mode = _state.value.tipMode) {
        is TipMode.Pct -> mode.pct
        is TipMode.Abs -> totals.value.impliedTipPct
    }
}
