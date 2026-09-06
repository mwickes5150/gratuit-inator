package com.mwickes.gratuitinator.ui.screens.review

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwickes.gratuitinator.data.ReceiptParser
import com.mwickes.gratuitinator.ocr.TextRecognitionClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * State for the Review screen: editable Subtotal/Tax fields pre-filled from OCR parsing, plus
 * per-field readability flags so an unreadable field can render distinctly instead of guessing.
 */
data class ReviewUiState(
    val subtotalInput: String = "",
    val taxInput: String = "",
    val subtotalReadable: Boolean = true,
    val taxReadable: Boolean = true,
    val isLoading: Boolean = false,
)

/**
 * Runs OCR + [ReceiptParser] on a captured receipt image, exposing editable fields for the user
 * to confirm. Results only reach the shared bill state via an explicit "Use These" tap — never
 * auto-committed.
 */
class ReviewViewModel(private val ocrClient: TextRecognitionClient) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState

    fun loadAndParse(uri: Uri) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val lines = ocrClient.recognize(uri)
                .getOrNull()
                ?.textBlocks
                ?.flatMap { it.lines }
                ?.map { it.text }
                ?: emptyList()
            val parsed = ReceiptParser.parseReceipt(lines)
            _uiState.value = ReviewUiState(
                subtotalInput = parsed.subtotal.orEmpty(),
                taxInput = parsed.tax.orEmpty(),
                subtotalReadable = parsed.subtotal != null,
                taxReadable = parsed.tax != null,
                isLoading = false,
            )
        }
    }

    fun onSubtotalChanged(value: String) {
        _uiState.value = _uiState.value.copy(subtotalInput = value, subtotalReadable = true)
    }

    fun onTaxChanged(value: String) {
        _uiState.value = _uiState.value.copy(taxInput = value, taxReadable = true)
    }
}
