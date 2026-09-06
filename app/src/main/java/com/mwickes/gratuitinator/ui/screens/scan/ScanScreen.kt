package com.mwickes.gratuitinator.ui.screens.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwickes.gratuitinator.ui.theme.LocalTapeColors

/**
 * The "Scan" screen: on-device/offline disclosure + Capture/Gallery buttons. Intent-based capture
 * (system Camera app) is used instead of CameraX, so there's no live in-app preview to show —
 * Capture just launches the system Camera app and hands back the photo it takes.
 */
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onImageReady: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tape = LocalTapeColors.current
    val context = LocalContext.current
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCaptureUri
        if (success && uri != null) {
            viewModel.onImageCaptured(uri)
            onImageReady(uri)
        } else {
            viewModel.onCaptureCancelled()
        }
        pendingCaptureUri = null
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            viewModel.onImageCaptured(uri)
            onImageReady(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tape.pageBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "SCAN RECEIPT",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            color = tape.ink,
        )

        Text(
            text = "ON-DEVICE OCR · OFFLINE — nothing leaves this phone",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            letterSpacing = 0.3.sp,
            color = tape.dim,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            OutlinedButton(
                onClick = {
                    val uri = viewModel.createCaptureUri(context)
                    pendingCaptureUri = uri
                    takePictureLauncher.launch(uri)
                },
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 46.dp).testTag("captureButton"),
            ) {
                Text(text = "CAPTURE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 0.5.sp, color = tape.ink)
            }
            OutlinedButton(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 46.dp).testTag("galleryButton"),
            ) {
                Text(text = "GALLERY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 0.5.sp, color = tape.ink)
            }
        }
    }
}
