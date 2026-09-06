package com.mwickes.gratuitinator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.domain.TipMode
import java.util.Locale

/**
 * The promoted tip-adjustment block — Round Down/Up flank the tip amount (the more commonly
 * reached-for controls), with the ±$1 stepper underneath. Shows the implied tip% caption only
 * while [tipMode] is [TipMode.Abs] (rounded/stepped); in [TipMode.Pct] mode it shows where the
 * amount came from instead.
 */
@Composable
fun StepperBlock(
    tipAmount: Double,
    tipMode: TipMode,
    onRoundDown: () -> Unit,
    onRoundUp: () -> Unit,
    onMinus1: () -> Unit,
    onPlus1: () -> Unit,
    ink: Color,
    border: Color,
    stepBg: Color,
    stepFg: Color,
    modifier: Modifier = Modifier,
) {
    val adjNote = when (tipMode) {
        is TipMode.Abs -> "adjusted by hand"
        is TipMode.Pct -> "from ${tipMode.pct.toInt()}% of subtotal"
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(90.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onRoundDown,
                modifier = Modifier.width(76.dp).fillMaxHeight(),
                border = BorderStroke(1.dp, border),
                contentPadding = PaddingValues(4.dp),
            ) {
                Text(
                    text = "ROUND DOWN",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = ink,
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(stepBg)
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "TIP",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    letterSpacing = 1.sp,
                    color = stepFg,
                )
                Text(
                    text = String.format(Locale.US, "$%.2f", tipAmount),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    color = stepFg,
                    modifier = Modifier.testTag("stepperTipAmountText"),
                )
                Text(
                    text = adjNote,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = stepFg,
                )
            }
            OutlinedButton(
                onClick = onRoundUp,
                modifier = Modifier.width(76.dp).fillMaxHeight(),
                border = BorderStroke(1.dp, border),
                contentPadding = PaddingValues(4.dp),
            ) {
                Text(
                    text = "ROUND UP",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = ink,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onMinus1,
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 46.dp),
                border = BorderStroke(1.dp, border),
            ) {
                Text(text = "− \$1", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = ink)
            }
            OutlinedButton(
                onClick = onPlus1,
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 46.dp),
                border = BorderStroke(1.dp, border),
            ) {
                Text(text = "+ \$1", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = ink)
            }
        }
    }
}
