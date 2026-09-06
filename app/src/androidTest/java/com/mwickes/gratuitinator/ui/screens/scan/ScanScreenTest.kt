package com.mwickes.gratuitinator.ui.screens.scan

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.mwickes.gratuitinator.ui.theme.GratuitinatorTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [ScanScreen]. Camera/gallery intent launches aren't exercised here (that's
 * Android intent plumbing, not app state) — these confirm the screen renders its offline
 * disclosure and both entry points correctly, matching Phase 8's test-coverage goal.
 */
class ScanScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent() {
        composeTestRule.setContent {
            GratuitinatorTheme(darkSteel = false) {
                ScanScreen(viewModel = ScanViewModel(), onImageReady = {})
            }
        }
    }

    @Test
    fun rendersOfflineDisclosure() {
        setContent()

        composeTestRule.onNodeWithText("ON-DEVICE OCR · OFFLINE — nothing leaves this phone").assertIsDisplayed()
    }

    @Test
    fun captureAndGalleryButtonsAreDisplayedAndEnabled() {
        setContent()

        composeTestRule.onNodeWithTag("captureButton").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("CAPTURE").assertIsDisplayed()

        composeTestRule.onNodeWithTag("galleryButton").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("GALLERY").assertIsDisplayed()
    }
}
