package com.redmadrobot.numberrecognizer.ui

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.redmadrobot.numberrecognizer.model.GmsTextRecognition
import timber.log.Timber

class RecognitionViewModel : ViewModel() {
    private val textRecognition = GmsTextRecognition()

    @androidx.camera.core.ExperimentalGetImage
    fun onFrameReceived(imageProxy: ImageProxy) {
        val frame = imageProxy.image
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        Timber.tag("RTRT").d("Image width:${imageProxy.width}, height:${imageProxy.height}, rotation:$rotationDegrees")

        if (frame == null) {
            imageProxy.close()
            return
        }

        textRecognition
            .processFrame(frame, rotationDegrees)
            .addOnCompleteListener { imageProxy.close() }
            .addOnSuccessListener {
                Timber.tag("RTRT").d("Local raw result:$it")
            }
            .addOnFailureListener { Timber.e(it) }
    }

    fun onFrameCaptured(bitmap: Bitmap, rotationDegrees: Int) {

        textRecognition
            .processFrame(bitmap, rotationDegrees)
            .addOnCompleteListener { bitmap.recycle() }
            .addOnSuccessListener {
                Timber.tag("RTRT").d("Remote raw result:$it")
            }
            .addOnFailureListener { Timber.e(it) }
    }
}