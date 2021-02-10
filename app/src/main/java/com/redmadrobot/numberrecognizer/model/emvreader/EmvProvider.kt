package com.redmadrobot.numberrecognizer.model.emvreader

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.exception.CommunicationException
import com.github.devnied.emvnfccard.parser.IProvider
import java.io.IOException
import timber.log.Timber


class EmvProvider : IProvider {

    var mTagCom: IsoDep? = null

    override fun transceive(pCommand: ByteArray?): ByteArray {

        return try {
            // send command to emv card
            mTagCom!!.transceive(pCommand)
        } catch (e: IOException) {
            throw CommunicationException(e.message)
        }
    }

    override fun getAt(): ByteArray {
        // For NFC-A
        return try {
            mTagCom!!.historicalBytes
        } catch (t: Throwable) {
            Timber.e(t)
            throw CommunicationException(t.message)
        }
        // For NFC-B
        // return mTagCom.getHiLayerResponse()
    }
}