package com.mwickes.gratuitinator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val PaperColorScheme = lightColorScheme(
    primary = TapeAccent,
    onPrimary = PaperBg,
    secondary = TapeAccent2,
    background = PaperBg,
    surface = PaperSurface,
    onBackground = PaperInk,
    onSurface = PaperInk,
    outline = PaperRule,
)

private val SteelColorScheme = darkColorScheme(
    primary = TapeAccent,
    onPrimary = SteelBg,
    secondary = TapeAccent2,
    background = SteelBg,
    surface = SteelSurface,
    onBackground = SteelInk,
    onSurface = SteelInk,
    outline = SteelRule,
)

/**
 * Paper/steel is an app feature the user toggles explicitly — not OS dark mode —
 * so it takes an explicit [darkSteel] flag rather than reading isSystemInDarkTheme().
 * Dynamic color is never used; Paper Tape has its own fixed palette.
 */
@Composable
fun GratuitinatorTheme(
    darkSteel: Boolean,
    content: @Composable () -> Unit,
) {
    val tapeColors = if (darkSteel) SteelTapeColors else PaperTapeColors
    val colorScheme = if (darkSteel) SteelColorScheme else PaperColorScheme

    CompositionLocalProvider(LocalTapeColors provides tapeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
