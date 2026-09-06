package com.mwickes.gratuitinator.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.ui.theme.TapeMonoFamily

/**
 * Formats a raw digit string (e.g. typed keystrokes "1", "15", "154", "1542") as a 2dp dollar
 * amount by inserting the decimal point from the right, cash-register/POS style: "1542" -> "15.42".
 * Empty input formats to "" (so the field falls back to its placeholder).
 */
private fun formatDigitsAsAmount(digits: String): String {
    if (digits.isEmpty()) return ""
    val padded = digits.trimStart('0').padStart(3, '0')
    val whole = padded.dropLast(2)
    val cents = padded.takeLast(2)
    return "$whole.$cents"
}

/**
 * A right-aligned, monospaced `$`-prefixed numeric field — the Subtotal/Tax input style from
 * the Paper Tape mockup. Digits typed auto-format as currency from the right (like a cash
 * register), and the reported [value]/[onValueChange] string is always the formatted amount, so
 * [TipCalculator][com.mwickes.gratuitinator.domain.TipCalculator] can keep parsing it as free-form
 * decimal text.
 *
 * Built on [BasicTextField] instead of Material3's `TextField` — the latter's built-in content
 * padding sits the text noticeably above the field's own bottom edge, which threw off alignment
 * against the row label's baseline. This draws just the text, bottom-aligned like the label.
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
    var fieldValue by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = "$",
            color = dim,
            fontSize = 14.sp,
        )
        Box(
            modifier = Modifier
                .width(130.dp)
                .let { if (testTag != null) it.testTag(testTag) else it },
            contentAlignment = Alignment.BottomEnd,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "0.00",
                    textAlign = TextAlign.End,
                    color = dim,
                    fontFamily = TapeMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BasicTextField(
                value = fieldValue,
                onValueChange = { new ->
                    val digits = new.text.filter { it.isDigit() }
                    val formatted = formatDigitsAsAmount(digits)
                    fieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                    onValueChange(formatted)
                },
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = TapeMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.End,
                    color = ink,
                ),
                cursorBrush = SolidColor(ink),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
