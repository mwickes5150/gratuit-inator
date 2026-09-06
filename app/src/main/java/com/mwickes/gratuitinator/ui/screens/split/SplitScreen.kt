package com.mwickes.gratuitinator.ui.screens.split

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.ui.components.DashedDivider
import com.mwickes.gratuitinator.ui.screens.main.MainViewModel
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors
import java.util.Locale

private fun money(value: Double): String = String.format(Locale.US, "$%.2f", value)

/**
 * The "Split" screen: aggregate "each person pays" total only (per DEVELOPMENT_PLAN.md — no
 * per-person Subtotal/Tax/Tip breakdown), plus a +/- people stepper. Backed by the shared
 * [MainViewModel] so it always reflects live Subtotal/Tax/Tip from the Bill screen.
 */
@Composable
fun SplitScreen(
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(tape.cardBg)
                .padding(vertical = 22.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "EACH PERSON PAYS",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                letterSpacing = 1.5.sp,
                color = tape.dim,
            )
            Text(
                text = money(totals.perPersonTotal),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 44.sp,
                color = tape.ink,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag("perPersonAmountText"),
            )
            DashedDivider(
                color = tape.rule,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            )
            Text(
                text = "${money(totals.grandTotal)} ÷ ${state.peopleCount}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                letterSpacing = 0.5.sp,
                color = tape.dim,
                modifier = Modifier.testTag("splitDivisionText"),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            OutlinedButton(
                onClick = viewModel::decrementPeople,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("splitDownButton"),
                border = BorderStroke(1.dp, tape.rule),
                contentPadding = PaddingValues(4.dp),
            ) {
                Text(text = "−", fontFamily = FontFamily.SansSerif, fontSize = 26.sp, color = tape.ink)
            }
            OutlinedButton(
                onClick = viewModel::incrementPeople,
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("splitUpButton"),
                border = BorderStroke(1.dp, tape.rule),
                contentPadding = PaddingValues(4.dp),
            ) {
                Text(text = "+", fontFamily = FontFamily.SansSerif, fontSize = 26.sp, color = tape.ink)
            }
        }
    }
}
