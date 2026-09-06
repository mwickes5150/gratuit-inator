package com.mwickes.gratuitinator.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.ui.components.DashedDivider
import com.mwickes.gratuitinator.ui.components.TapeAmountField
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors

/**
 * "WHAT WE READ" — shows OCR-parsed Subtotal/Tax for confirmation/edit before it touches the
 * real bill state. Unreadable fields render with hint styling instead of a guessed value.
 * Retake discards and returns to Scan; "Use These" is the only path that commits into Main.
 */
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onRetake: () -> Unit,
    onUseThese: (subtotal: String, tax: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tape = LocalTapeColors.current
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tape.pageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "WHAT WE READ",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            color = tape.ink,
        )

        if (uiState.isLoading) {
            Text(
                text = "READING RECEIPT…",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = tape.dim,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(tape.cardBg)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            ReviewFieldRow(
                label = "SUBTOTAL",
                value = uiState.subtotalInput,
                onValueChange = viewModel::onSubtotalChanged,
                readable = uiState.subtotalReadable,
                tape = tape,
            )
            DashedDivider(color = tape.rule)
            ReviewFieldRow(
                label = "TAX",
                value = uiState.taxInput,
                onValueChange = viewModel::onTaxChanged,
                readable = uiState.taxReadable,
                tape = tape,
            )
        }

        Text(
            text = "Tip/gratuity lines are never read — set your tip on the Bill screen.",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontStyle = FontStyle.Italic,
            color = tape.dim,
        )
        Text(
            text = "Scanning can make mistakes — double-check these numbers against your receipt before using them.",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontStyle = FontStyle.Italic,
            color = tape.dim,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 46.dp),
            ) {
                Text(text = "RETAKE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 0.5.sp, color = tape.ink)
            }
            Button(
                onClick = { onUseThese(uiState.subtotalInput, uiState.taxInput) },
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 46.dp),
            ) {
                Text(text = "USE THESE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}

@Composable
private fun ReviewFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readable: Boolean,
    tape: com.mwickes.gratuitinator.ui.theme.TapeColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, color = tape.dim)
            TapeAmountField(
                value = value,
                onValueChange = onValueChange,
                ink = if (readable) tape.ink else tape.dim,
                dim = tape.dim,
            )
        }
        if (!readable) {
            Text(
                text = "COULDN'T READ THIS — ENTER MANUALLY",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                letterSpacing = 0.3.sp,
                fontStyle = FontStyle.Italic,
                color = tape.dim,
            )
        }
    }
}
