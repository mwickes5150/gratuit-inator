package com.mwickes.gratuitinator.ui.screens.main

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [MainScreen], focused on the same critical state-machine behaviors
 * [com.mwickes.gratuitinator.domain.TipCalculatorTest] covers at the pure-function level —
 * here verified end-to-end through real input/click events on the composed screen.
 */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(viewModel: MainViewModel = MainViewModel()): MainViewModel {
        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                MainScreen(viewModel = viewModel)
            }
        }
        return viewModel
    }

    @Test
    fun typingSubtotalUpdatesTipAndTotal() {
        setContent()

        // Cash-register-style entry: digits fill in from the right, so "10000" -> $100.00.
        composeTestRule.onNodeWithTag("subtotalField").performClick().performTextInput("10000")

        // 20% default tip on a 100 subtotal -> Tip $20.00, Total $120.00.
        composeTestRule.onNodeWithTag("tipAmountText").assertTextEquals("$20.00")
        composeTestRule.onNodeWithTag("totalAmountText").assertTextEquals("$120.00")
    }

    @Test
    fun typingDigitsAutoInsertsDecimalPointFromTheRight() {
        setContent()

        composeTestRule.onNodeWithTag("subtotalField").performClick().performTextInput("1542")

        composeTestRule.onNodeWithTag("subtotalField").assertTextEquals("15.42")
    }

    @Test
    fun fullBillToggleDisablesTaxField() {
        setContent()

        composeTestRule.onNodeWithTag("taxField").assertIsEnabled()

        composeTestRule.onNodeWithText("USE FULL BILL AMOUNT").performClick()

        composeTestRule.onNodeWithTag("taxField").assertIsNotEnabled()
    }

    @Test
    fun fullBillToggleKeepsTaxAndRecalculatesTipLive() {
        val viewModel = setContent()

        composeTestRule.runOnIdle {
            viewModel.onSubtotalChanged("100")
            viewModel.onTaxChanged("8")
        }
        // Off: Tip = 100 * 20% = $20.00, Total = 100 + 8 + 20 = $128.00.
        composeTestRule.onNodeWithTag("tipAmountText").assertTextEquals("$20.00")

        composeTestRule.onNodeWithText("USE FULL BILL AMOUNT").performClick()

        // On: Tip = (100 + 8) * 20% = $21.60 — tax preserved and folded into the tip basis,
        // not cleared, and the tip recomputes live rather than staying frozen.
        composeTestRule.onNodeWithTag("tipAmountText").assertTextEquals("$21.60")
        composeTestRule.onNodeWithTag("totalAmountText").assertTextEquals("$129.60")

        composeTestRule.onNodeWithText("USE FULL BILL AMOUNT").performClick()

        // Off again: tax value is restored, tip basis back to subtotal only.
        composeTestRule.onNodeWithTag("tipAmountText").assertTextEquals("$20.00")
        composeTestRule.onNodeWithTag("totalAmountText").assertTextEquals("$128.00")
    }

    @Test
    fun roundUpShowsImpliedPercent() {
        val viewModel = setContent()

        composeTestRule.runOnIdle {
            viewModel.onSubtotalChanged("100")
            viewModel.onTaxChanged("8")
        }
        composeTestRule.onNodeWithText("ROUND UP").performClick()

        composeTestRule.onNodeWithTag("totalAmountText").assertTextEquals("$129.00")
        composeTestRule.onAllNodes(hasText("≈", substring = true)).onFirst().assertExists()
    }

    @Test
    fun movingSliderAfterRoundResetsTipToPercentMode() {
        val viewModel = setContent()

        composeTestRule.runOnIdle {
            viewModel.onSubtotalChanged("100")
        }
        composeTestRule.onNodeWithText("ROUND UP").performClick()

        // After rounding, tip is in Abs mode (not necessarily $20 anymore).
        composeTestRule.runOnIdle {
            viewModel.onSliderChanged(18.0)
        }

        // Moving the slider always resets to Pct mode: Tip = 18% of $100 = $18.00, not the
        // rounded amount.
        composeTestRule.onNodeWithTag("tipAmountText").assertTextEquals("$18.00")
    }
}
