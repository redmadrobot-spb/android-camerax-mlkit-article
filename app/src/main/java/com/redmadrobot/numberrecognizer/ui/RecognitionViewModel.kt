package com.redmadrobot.numberrecognizer.ui

import androidx.lifecycle.ViewModel
import com.redmadrobot.numberrecognizer.model.GmsTextRecognition

class RecognitionViewModel : ViewModel() {
    private val textRecognition = GmsTextRecognition()
}