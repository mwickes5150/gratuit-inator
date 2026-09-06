package com.mwickes.gratuitinator.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Thin wrapper around ML Kit's on-device text recognizer. No parsing logic here — that's
 * [com.mwickes.gratuitinator.data.ReceiptParser]'s job. Fully on-device: the bundled
 * `com.google.mlkit:text-recognition` artifact does not require network access.
 */
class TextRecognitionClient(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(uri: Uri): Result<Text> = suspendCancellableCoroutine { cont ->
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: Exception) {
            cont.resume(Result.failure(e))
            return@suspendCancellableCoroutine
        }
        recognizer.process(image)
            .addOnSuccessListener { text -> cont.resume(Result.success(text)) }
            .addOnFailureListener { e -> cont.resume(Result.failure(e)) }
    }
}
