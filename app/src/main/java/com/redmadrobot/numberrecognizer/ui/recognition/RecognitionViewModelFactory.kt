package com.redmadrobot.numberrecognizer.ui.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.redmadrobot.numberrecognizer.MobileService

class RecognitionViewModelFactory(private val mobileService: MobileService) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecognitionViewModel(mobileService) as T
    }
}