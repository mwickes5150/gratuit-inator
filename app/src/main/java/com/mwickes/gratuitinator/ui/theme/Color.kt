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
// Darkened from the mockup's literal #7A7A7D to clear WCAG AA (4.5:1) at small text sizes
// against both PaperBg and PaperSurface — #7A7A7D only reaches ~3.5-3.8:1. See DEVELOPMENT_PLAN.md
// Phase 8 accessibility pass.
val PaperDim = Color(0xFF646467)
val PaperRule = Color(0xFFD7D7D8)

// Steel (dark) — inverted: charcoal background, off-white tape surface/ink.
val SteelBg = Color(0xFF1D1F20)
val SteelSurface = Color(0xFF2B2B2D)
val SteelInk = Color(0xFFF2F2F3)
val SteelDim = Color(0xFF98989B)
val SteelRule = Color(0xFF424244)

// Ink-tinted card shadow, from the mockup's per-theme box-shadow color (Gratuit-inator.dc.html,
// the 1c "Paper Tape" cardShadow token): a light, low-alpha ink tint on Paper; a stronger black
// tint on Steel since it's a dark surface.
val PaperCardShadow = Color(0x242B2B2D)
val SteelCardShadow = Color(0x73000000)
