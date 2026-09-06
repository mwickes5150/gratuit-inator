package com.mwickes.gratuitinator.ui.screens.review

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.mwickes.gratuitinator.ocr.TextRecognitionClient
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [ReviewScreen]. Most state is driven through [ReviewViewModel]'s public
 * setters rather than running real ML Kit OCR — matching how
 * [com.mwickes.gratuitinator.ui.screens.split.SplitScreenTest] drives the shared MainViewModel
 * directly instead of simulating navigation. The "unreadable" test below does call
 * [ReviewViewModel.loadAndParse] with a Uri that can't be opened, which deterministically exercises
 * the real not-readable path (image-load failure -> empty OCR lines -> ReceiptParser finds
 * nothing) without depending on ML Kit actually recognizing text.
 */
class ReviewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun newViewModel(): ReviewViewModel {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return ReviewViewModel(TextRecognitionClient(context))
    }

    @Test
    fun prefilledFieldsRenderWithNoUnreadableHints() {
        val viewModel = newViewModel()

        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                ReviewScreen(viewModel = viewModel, onRetake = {}, onUseThese = { _, _ -> })
            }
        }

        composeTestRule.runOnIdle {
            viewModel.onSubtotalChanged("42.00")
            viewModel.onTaxChanged("3.50")
        }

        composeTestRule.onNodeWithTag("subtotalField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("taxField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("subtotalUnreadableHint").assertDoesNotExist()
        composeTestRule.onNodeWithTag("taxUnreadableHint").assertDoesNotExist()
    }

    @Test
    fun unreadableFieldShowsHintUntilEdited() {
        val viewModel = newViewModel()

        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                ReviewScreen(viewModel = viewModel, onRetake = {}, onUseThese = { _, _ -> })
            }
        }

        // An unopenable Uri fails image decoding, which resolves to zero OCR lines -> the parser
        // finds neither field -> both render as not-readable.
        composeTestRule.runOnIdle {
            viewModel.loadAndParse(Uri.parse("content://com.mwickes.gratuitinator.test/does-not-exist"))
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("subtotalUnreadableHint").assertIsDisplayed()
        composeTestRule.onNodeWithTag("taxUnreadableHint").assertIsDisplayed()

        // Editing a field marks it readable again (ReviewViewModel.onSubtotalChanged), so its
        // hint must disappear while the still-untouched Tax hint remains.
        composeTestRule.onNodeWithTag("subtotalField").performClick().performTextInput("1500")
        composeTestRule.onNodeWithTag("subtotalUnreadableHint").assertDoesNotExist()
        composeTestRule.onNodeWithTag("taxUnreadableHint").assertIsDisplayed()
    }

    @Test
    fun useTheseButtonReportsCurrentFieldValues() {
        val viewModel = newViewModel()
        var reportedSubtotal: String? = null
        var reportedTax: String? = null

        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                ReviewScreen(
                    viewModel = viewModel,
                    onRetake = {},
                    onUseThese = { subtotal, tax ->
                        reportedSubtotal = subtotal
                        reportedTax = tax
                    },
                )
            }
        }

        composeTestRule.runOnIdle {
            viewModel.onSubtotalChanged("100.00")
            viewModel.onTaxChanged("8.25")
        }

        composeTestRule.onNodeWithTag("useTheseButton").performClick()

        composeTestRule.runOnIdle {
            assertEquals("100.00", reportedSubtotal)
            assertEquals("8.25", reportedTax)
        }
    }

    @Test
    fun retakeButtonInvokesCallback() {
        val viewModel = newViewModel()
        var retakeCount = 0

        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                ReviewScreen(viewModel = viewModel, onRetake = { retakeCount++ }, onUseThese = { _, _ -> })
            }
        }

        composeTestRule.onNodeWithTag("retakeButton").performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, retakeCount)
        }
    }
}
