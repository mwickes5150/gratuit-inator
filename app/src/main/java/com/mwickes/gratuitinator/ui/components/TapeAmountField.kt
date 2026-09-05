package com.mwickes.gratuitinator.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.ui.theme.TapeMonoFamily

/**
 * A right-aligned, monospaced `$`-prefixed numeric field — the Subtotal/Tax input style from
 * the Paper Tape mockup. Free-form text in/out; [TipCalculator] parses defensively.
 */
@Composable
fun TapeAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    ink: Color,
    dim: Color,
    enabled: Boolean = true,
    testTag: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = "$",
            color = dim,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 3.dp),
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = { Text("0.00", textAlign = TextAlign.End, color = dim) },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = TapeMonoFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                textAlign = TextAlign.End,
                color = ink,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = ink,
            ),
            modifier = Modifier.width(130.dp).let { if (testTag != null) it.testTag(testTag) else it },
        )
    }
}
