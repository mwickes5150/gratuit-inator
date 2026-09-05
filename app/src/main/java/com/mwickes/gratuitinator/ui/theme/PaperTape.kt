package com.mwickes.gratuitinator.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tape-specific color roles that don't map cleanly onto Material3's ColorScheme
 * (dashed-rule color, the promoted stepper block, distinct page-vs-card surfaces).
 * Paired with a matching Material3 ColorScheme (see Theme.kt) for standard widgets.
 */
data class TapeColors(
    val ink: Color,
    val dim: Color,
    val rule: Color,
    val pageBg: Color,
    val cardBg: Color,
    val accent: Color,
    val accent2: Color,
    val stepBg: Color,
    val stepFg: Color,
)

val PaperTapeColors = TapeColors(
    ink = PaperInk,
    dim = PaperDim,
    rule = PaperRule,
    pageBg = PaperBg,
    cardBg = PaperSurface,
    accent = TapeAccent,
    accent2 = TapeAccent2,
    stepBg = TapeAccent,
    stepFg = PaperBg,
)

val SteelTapeColors = TapeColors(
    ink = SteelInk,
    dim = SteelDim,
    rule = SteelRule,
    pageBg = SteelBg,
    cardBg = SteelSurface,
    accent = TapeAccent,
    accent2 = TapeAccent2,
    stepBg = TapeAccent,
    stepFg = SteelBg,
)

val LocalTapeColors = staticCompositionLocalOf { PaperTapeColors }
