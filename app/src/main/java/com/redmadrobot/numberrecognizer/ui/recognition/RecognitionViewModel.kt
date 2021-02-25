package com.redmadrobot.numberrecognizer.ui.recognition

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.redmadrobot.numberrecognizer.MobileService
import com.redmadrobot.numberrecognizer.entity.RecognizedLine
import com.redmadrobot.numberrecognizer.model.GmsTextRecognition
import com.redmadrobot.numberrecognizer.model.HmsTextRecognition
import timber.log.Timber

class RecognitionViewModel(private val mobileService: MobileService) : ViewModel() {

    private val gmsTextRecognition = GmsTextRecognition()
    private val hmsTextRecognition = HmsTextRecognition()

    private var startCycleComputingFpsTimestamp = 0L
    private var framesCounter = 0

    val realtimeResultsLiveData = MutableLiveData<List<RecognizedLine>>()
    val captureResultsLiveData = MutableLiveData<List<RecognizedLine>>()
    val fpsLiveData = MutableLiveData<Int>()

    @androidx.camera.core.ExperimentalGetImage
    fun onFrameReceived(imageProxy: ImageProxy) {

        recomputeFps()

        val frame = imageProxy.image
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        if (frame == null) {
            imageProxy.close()
            return
        }

        when (mobileService) {
            MobileService.GMS -> {
                gmsTextRecognition
                    .processFrame(frame, rotationDegrees)
                    .addOnCompleteListener { imageProxy.close() }
                    .addOnSuccessListener {
                        Timber.tag("RTRT").d("On-device realtime result:$it")
                        realtimeResultsLiveData.postValue(it)
                    }
                    .addOnFailureListener { Timber.e(it) }
            }
            MobileService.HMS -> {
                hmsTextRecognition
                    .processFrame(frame, rotationDegrees)
                    .addOnCompleteListener { imageProxy.close() }
                    .addOnSuccessListener {
                        Timber.tag("RTRT").d("On-device realtime result:$it")
                        realtimeResultsLiveData.postValue(it)
                    }
                    .addOnFailureListener { Timber.e(it) }
            }
        }

    }

    fun onFrameCaptured(bitmap: Bitmap, rotationDegrees: Int) {
        when (mobileService) {
            MobileService.GMS -> {
                gmsTextRecognition
                    .processFrame(bitmap, rotationDegrees)
                    .addOnCompleteListener { bitmap.recycle() }
                    .addOnSuccessListener {
                        Timber.tag("RTRT").d("On-device from capture result:$it")
                        captureResultsLiveData.postValue(it)
                    }
                    .addOnFailureListener { Timber.e(it) }
            }
            MobileService.HMS -> {
                hmsTextRecognition
                    .processFrame(bitmap, rotationDegrees)
                    .addOnCompleteListener { bitmap.recycle() }
                    .addOnSuccessListener {
                        Timber.tag("RTRT").d("On-cloud from capture result:$it")
                        captureResultsLiveData.postValue(it)
                    }
                    .addOnFailureListener { Timber.e(it) }
            }
        }
    }

    private fun recomputeFps() {
        val currentTimestamp = System.currentTimeMillis()
        if (startCycleComputingFpsTimestamp == 0L) {
            startCycleComputingFpsTimestamp = currentTimestamp
            return
        }

        val diff = currentTimestamp - startCycleComputingFpsTimestamp
        if (diff < 1000) {
            framesCounter++
        } else {
            fpsLiveData.postValue(framesCounter)
            framesCounter = 0
            startCycleComputingFpsTimestamp = currentTimestamp
        }
    }
}