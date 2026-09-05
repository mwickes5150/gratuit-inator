package com.mwickes.gratuitinator.ui.theme

import androidx.compose.ui.graphics.Color

// Paper Tape design tokens, derived from app-design-files/project/_ds/industry-*/styles.css.
// "Paper" is the light receipt-tape theme; "Steel" is its dark counterpart (inverted
// dark-charcoal background with an off-white tape surface). Accent is shared across both.

// Shared accent (from --color-accent / --color-accent-2)
val TapeAccent = Color(0xFF5980A6)
val TapeAccent2 = Color(0xFF728FAB)

// Paper (light)
val PaperBg = Color(0xFFF2F2F3)
val PaperSurface = Color(0xFFE9E9EA)
val PaperInk = Color(0xFF1D1F20)
val PaperDim = Color(0xFF7A7A7D)
val PaperRule = Color(0xFFD7D7D8)

// Steel (dark) — inverted: charcoal background, off-white tape surface/ink.
val SteelBg = Color(0xFF1D1F20)
val SteelSurface = Color(0xFF2B2B2D)
val SteelInk = Color(0xFFF2F2F3)
val SteelDim = Color(0xFF98989B)
val SteelRule = Color(0xFF424244)
