package com.mwickes.gratuitinator.ui.screens.split

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.mwickes.gratuitinator.ui.screens.main.MainViewModel
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [SplitScreen] — floor-at-1 people count and live recalculation from the
 * shared [MainViewModel], matching the shared-state design DEVELOPMENT_PLAN.md Phase 3 calls for.
 */
class SplitScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(viewModel: MainViewModel = MainViewModel()): MainViewModel {
        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                SplitScreen(viewModel = viewModel)
            }
        }
        return viewModel
    }

    @Test
    fun peopleCountFlooredAtOne() {
        val viewModel = setContent()

        composeTestRule.onNodeWithTag("splitDivisionText").assertTextEquals("$0.00 ÷ 1")

        composeTestRule.onNodeWithTag("splitDownButton").performClick()

        // Already at 1 person; decrementing must not go below the floor.
        composeTestRule.onNodeWithTag("splitDivisionText").assertTextEquals("$0.00 ÷ 1")
    }

    @Test
    fun perPersonTotalRecalculatesLiveFromSharedState() {
        val viewModel = setContent()

        composeTestRule.runOnIdle {
            viewModel.onSubtotalChanged("100")
        }
        // Default 20% tip on $100 -> Grand Total $120.00, 1 person -> $120.00 each.
        composeTestRule.onNodeWithTag("perPersonAmountText").assertTextEquals("$120.00")

        composeTestRule.onNodeWithTag("splitUpButton").performClick()

        // 2 people -> $60.00 each, with a live division caption.
        composeTestRule.onNodeWithTag("perPersonAmountText").assertTextEquals("$60.00")
        composeTestRule.onNodeWithTag("splitDivisionText").assertTextEquals("$120.00 ÷ 2")
    }
}
