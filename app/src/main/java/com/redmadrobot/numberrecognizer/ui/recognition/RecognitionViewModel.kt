package com.redmadrobot.numberrecognizer.ui.recognition

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.redmadrobot.numberrecognizer.entity.RecognizedLine
import com.redmadrobot.numberrecognizer.model.GmsTextRecognition
import timber.log.Timber

class RecognitionViewModel : ViewModel() {

    private val textRecognition = GmsTextRecognition()
    private var lastTimestampFrameReceived = 0L

    val recognitionResultLiveData = MutableLiveData<List<RecognizedLine>>()

    @androidx.camera.core.ExperimentalGetImage
    fun onFrameReceived(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        val diff = currentTimestamp - lastTimestampFrameReceived
        if (diff < 1000) {
            imageProxy.close()
            return
        }
        lastTimestampFrameReceived = currentTimestamp

        val frame = imageProxy.image
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        if (frame == null) {
            imageProxy.close()
            return
        }
        textRecognition
            .processFrame(frame, rotationDegrees)
            .addOnCompleteListener { imageProxy.close() }
            .addOnSuccessListener {
                Timber.tag("RTRT").d("Local raw result:$it")
                recognitionResultLiveData.postValue(it)
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