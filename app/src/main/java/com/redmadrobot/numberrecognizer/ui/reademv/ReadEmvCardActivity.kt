package com.redmadrobot.numberrecognizer.ui.reademv

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.github.devnied.emvnfccard.parser.EmvTemplate
import com.redmadrobot.numberrecognizer.R
import com.redmadrobot.numberrecognizer.databinding.ActivityReadEmvCardBinding
import com.redmadrobot.numberrecognizer.model.emvreader.EmvProvider
import timber.log.Timber


class ReadEmvCardActivity : AppCompatActivity() {

    private val emvProvider = EmvProvider()
    private val emvParser = initEmvParser(emvProvider)
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent

    private lateinit var binding: ActivityReadEmvCardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReadEmvCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.toolbar) {
            title = getString(R.string.read_emv)
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener { finish() }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //Create a PendingIntent object so the Android system can
        //populate it with the details of the tag when it is scanned.
        //PendingIntent.getActivity(Context,requestcode(identifier for
        //                           intent),intent,int)
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, this.javaClass)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()

        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveIntent(intent)
    }

    private fun resolveIntent(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action
        ) {
            val tag = (intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag?)
            Timber.tag("RTRT").d("Discovered tag:$tag")
            if (tag == null) return

            parseNfcTag(tag)
        }
    }

    private fun initEmvParser(emvProvider: EmvProvider): EmvTemplate {
        val config = EmvTemplate.Config()
            .setContactLess(true) // Enable contact less reading (default: true)
            .setReadAllAids(true) // Read all aids in card (default: true)
            .setReadTransactions(true) // Read all transactions (default: true)
            .setReadCplc(false) // Read and extract CPCLC data (default: false)
            .setRemoveDefaultParsers(false) // Remove default parsers for GeldKarte and EmvCard (default: false)
            .setReadAt(true) // Read and extract ATR/ATS and description

        return EmvTemplate.Builder() //
            .setProvider(emvProvider) // Define provider
            .setConfig(config) // Define config
            //.setTerminal(terminal) (optional) you can define a custom terminal implementation to create APDU
            .build()
    }

    private fun parseNfcTag(tag: Tag) {


        try {
            emvProvider.mTagCom = IsoDep.get(tag).apply { connect() }
            val card = emvParser.readEmvCard()
            card.cardNumber
            Timber.tag("RTRT").d("Parsed card number:${card.cardNumber}, expiration date:${card.expireDate}")
            binding.resultsTextView.text =
                String.format(
                    "Card number:%s\nExpiration date:%s\nCardholder name:%s %s",
                    card.cardNumber,
                    card.expireDate,
                    card.holderFirstname,
                    card.holderLastname
                )
        } catch (t: Throwable) {
            Timber.e(t)
        } finally {
            emvProvider.mTagCom?.close()
        }
    }
}