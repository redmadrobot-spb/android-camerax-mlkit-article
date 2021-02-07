package com.redmadrobot.numberrecognizer.model

import android.view.Surface

class HmsTextRecognition {

//    private val localAnalyzer = MLAnalyzerFactory.getInstance().localTextAnalyzer
//
//    fun processFrame(frame: Image, rotationDegrees: Int): Task<List<RecognizedLine>> {
//        val mlFrame = MLFrame.fromMediaImage(frame, getHmsQuadrant(rotationDegrees))
//    }
//
//    fun processFrame(bitmap: Bitmap, rotationDegrees: Int): Task<List<RecognizedLine>> {
//        val mlFrame = MLFrame.fromBitmap(bitmap)
//    }

//    private fun MLText.TextLine.toRecognizedLine(): RecognizedLine =
//        RecognizedLine(stringValue)

    private fun getHmsQuadrant(rotationDegrees: Int): Int =
        when (rotationDegrees) {
            Surface.ROTATION_0 -> PORTRAIT_QUADRANT
            Surface.ROTATION_90 -> LANDSCAPE_QUADRANT
            Surface.ROTATION_180 -> REVERSE_PORTRAIT_QUADRANT
            Surface.ROTATION_270 -> REVERSE_LANDSCAPE_QUADRANT
            else -> PORTRAIT_QUADRANT
        }

    companion object {
        private const val LANDSCAPE_QUADRANT = 0
        private const val PORTRAIT_QUADRANT = 1
        private const val REVERSE_LANDSCAPE_QUADRANT = 2
        private const val REVERSE_PORTRAIT_QUADRANT = 3
    }
}