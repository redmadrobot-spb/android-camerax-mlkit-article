package com.redmadrobot.numberrecognizer.ui.recognition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.redmadrobot.numberrecognizer.databinding.DialogResultBinding

class ResultDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogResultBinding? = null
    private val binding
        get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val results = arguments?.getString(KEY_RESULTS) ?: ""
        binding.resultsTextView.text = results
    }

    companion object {
        private const val KEY_RESULTS = "KEY_RESULTS"

        fun create(results: String): ResultDialogFragment =
            ResultDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_RESULTS, results)
                }
            }
    }
}