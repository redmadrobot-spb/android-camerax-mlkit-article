package com.redmadrobot.numberrecognizer.model

import android.graphics.Bitmap
import android.media.Image
import android.view.Surface
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLText
import com.redmadrobot.numberrecognizer.entity.RecognizedLine

class HmsTextRecognition {

    private val localAnalyzer = MLAnalyzerFactory.getInstance().localTextAnalyzer

    fun processFrame(frame: Image, rotationDegrees: Int): Task<List<RecognizedLine>> {
        val mlFrame = MLFrame.fromMediaImage(frame, getHmsQuadrant(rotationDegrees))

        return localAnalyzer
            .asyncAnalyseFrame(mlFrame)
            .continueWith { task ->
                task.result
                    .blocks
                    .flatMap { block -> block.contents }
                    .map { line -> line.toRecognizedLine() }
            }
    }

    fun processFrame(bitmap: Bitmap, rotationDegrees: Int): Task<List<RecognizedLine>> {
        val mlFrame = MLFrame.fromBitmap(bitmap)

        return localAnalyzer
            .asyncAnalyseFrame(mlFrame)
            .continueWith { task ->
                task.result
                    .blocks
                    .flatMap { block -> block.contents }
                    .map { line -> line.toRecognizedLine() }
            }
    }

    private fun MLText.TextLine.toRecognizedLine(): RecognizedLine =
        RecognizedLine(stringValue)

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