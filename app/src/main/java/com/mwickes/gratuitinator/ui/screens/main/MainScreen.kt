package com.mwickes.gratuitinator.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.domain.BillTotals
import com.mwickes.gratuitinator.domain.TipMode
import com.mwickes.gratuitinator.ui.components.DashedDivider
import com.mwickes.gratuitinator.ui.components.PctPresetRow
import com.mwickes.gratuitinator.ui.components.StepperBlock
import com.mwickes.gratuitinator.ui.components.TapeAmountField
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors
import java.util.Locale

private fun money(value: Double): String = String.format(Locale.US, "$%.2f", value)

/** Formats a tip's percentage label: the exact preset in Pct mode, "≈X.X%" implied in Abs mode. */
private fun pctLabel(totals: BillTotals, tipMode: TipMode): String = when (tipMode) {
    is TipMode.Pct -> "${tipMode.pct.toInt()}%"
    is TipMode.Abs -> "≈${String.format(Locale.US, "%.1f", totals.impliedTipPct)}%"
}

/**
 * The "Bill" screen: tape receipt card (Subtotal/Tax/Tip/Total), the promoted +/-$1 stepper,
 * Round Down/Up, and the Set-Tip-% card (presets + slider). Backed by [MainViewModel], which is
 * the single source of truth for how the current tip amount was derived.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val tape = LocalTapeColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tape.pageBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Receipt tape card: Subtotal / Tax / Tip / Total / Use-full-bill row.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(tape.cardBg)
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            AmountRow(label = "SUBTOTAL", dim = tape.dim) {
                TapeAmountField(
                    value = state.subtotalInput,
                    onValueChange = viewModel::onSubtotalChanged,
                    ink = tape.ink,
                    dim = tape.dim,
                    testTag = "subtotalField",
                )
            }
            AmountRow(label = "TAX", dim = tape.dim) {
                TapeAmountField(
                    value = state.taxInput,
                    onValueChange = viewModel::onTaxChanged,
                    ink = tape.ink,
                    dim = tape.dim,
                    enabled = !state.useFullBillAmount,
                    testTag = "taxField",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "TIP ${pctLabel(totals, state.tipMode)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    color = tape.accent,
                )
                Text(
                    text = money(totals.tip),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = tape.accent,
                    modifier = Modifier.testTag("tipAmountText"),
                )
            }
            DashedDivider(color = tape.rule)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(text = "TOTAL", fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = tape.ink)
                Text(
                    text = money(totals.grandTotal),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 36.sp,
                    color = tape.ink,
                    modifier = Modifier.testTag("totalAmountText"),
                )
            }
            DashedDivider(color = tape.rule)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .clickable { viewModel.onToggleFullBillAmount() }
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.useFullBillAmount,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = tape.ink),
                )
                Text(
                    text = "USE FULL BILL AMOUNT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.5.sp,
                    color = tape.dim,
                )
            }
        }

        // Promoted tip-adjustment block: Round Down/Up flank the amount, +/-$1 underneath.
        StepperBlock(
            tipAmount = totals.tip,
            tipMode = state.tipMode,
            onRoundDown = viewModel::onRoundDown,
            onRoundUp = viewModel::onRoundUp,
            onMinus1 = viewModel::onStepMinus1,
            onPlus1 = viewModel::onStepPlus1,
            ink = tape.ink,
            border = tape.rule,
            stepBg = tape.stepBg,
            stepFg = tape.stepFg,
            modifier = Modifier.fillMaxWidth(),
        )

        // Set Tip % card: preset chips + slider.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(tape.cardBg)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "SET TIP %", fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.sp, color = tape.dim)
                Text(text = pctLabel(totals, state.tipMode), fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = tape.dim)
            }
            PctPresetRow(
                tipMode = state.tipMode,
                onPresetClick = viewModel::onPctPreset,
                accent = tape.accent,
                ink = tape.ink,
                border = tape.rule,
            )
            val sliderPct = viewModel.currentPctForDisplay().toFloat().coerceIn(0f, 40f)
            Slider(
                value = sliderPct,
                onValueChange = { viewModel.onSliderChanged(it.toDouble()) },
                valueRange = 0f..40f,
                steps = 39,
            )
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    dim: Color,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = dim)
        content()
    }
}
