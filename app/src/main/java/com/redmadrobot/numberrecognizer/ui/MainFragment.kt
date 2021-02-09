package com.redmadrobot.numberrecognizer.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.redmadrobot.numberrecognizer.R
import com.redmadrobot.numberrecognizer.databinding.FragmentMainBinding
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import timber.log.Timber


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.app_name)

        binding.mlkitButton.setOnClickListener {
            findNavController().navigate(R.id.recognitionFragment)
        }

        binding.cardioButton.setOnClickListener { startCardIO() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startCardIO() {
        val scanIntent = Intent(requireContext(), CardIOActivity::class.java).apply {
            putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
            putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false)
            putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
        }
        startActivityForResult(scanIntent, CARD_IO_SCAN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != CARD_IO_SCAN_REQUEST_CODE) return

        if (data == null || !data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            Snackbar.make(binding.root, "Scan was cancelled", Snackbar.LENGTH_SHORT).show()
            return
        }

        val scanResult: CreditCard? = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT)
        scanResult?.let {
            Timber.tag("RTRT").d("Card IO card:$it")
        }
    }

    companion object {
        private const val CARD_IO_SCAN_REQUEST_CODE = 13
    }
}