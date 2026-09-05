package com.mwickes.gratuitinator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.domain.TipMode

private val presets = listOf(15.0, 18.0, 20.0, 25.0)

/**
 * The 15/18/20/25 preset chips. A chip is highlighted only when [tipMode] is currently
 * [TipMode.Pct] matching that exact value — Abs mode (post-round/step) highlights none of them.
 */
@Composable
fun PctPresetRow(
    tipMode: TipMode,
    onPresetClick: (Double) -> Unit,
    accent: Color,
    ink: Color,
    border: Color,
    modifier: Modifier = Modifier,
) {
    val activePct = (tipMode as? TipMode.Pct)?.pct
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        presets.forEach { pct ->
            val isActive = activePct == pct
            OutlinedButton(
                onClick = { onPresetClick(pct) },
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 44.dp),
                border = BorderStroke(1.dp, if (isActive) accent else border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isActive) accent else Color.Transparent,
                    contentColor = if (isActive) Color.White else ink,
                ),
            ) {
                Text(text = pct.toInt().toString(), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }
    }
}
