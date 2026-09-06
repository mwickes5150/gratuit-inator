package com.mwickes.gratuitinator.ui.screens.scan

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Holds transient capture state for the Scan screen. No calculation/business logic lives here —
 * just enough state to get a photo Uri from the system Camera app or gallery into the app.
 */
class ScanViewModel : ViewModel() {

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri

    fun onImageCaptured(uri: Uri) {
        _capturedImageUri.value = uri
    }

    fun onCaptureCancelled() {
        _capturedImageUri.value = null
    }

    /**
     * Creates a fresh cache file under `cacheDir/receipts/` and returns a FileProvider Uri for it,
     * to be passed to [androidx.activity.result.contract.ActivityResultContracts.TakePicture].
     */
    fun createCaptureUri(context: Context): Uri {
        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File.createTempFile("receipt_", ".jpg", receiptsDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
