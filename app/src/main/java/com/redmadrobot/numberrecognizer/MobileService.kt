package com.redmadrobot.numberrecognizer

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.HuaweiApiAvailability

enum class MobileService {
    GMS,
    HMS
}

internal typealias GMSConnectionResult = com.google.android.gms.common.ConnectionResult
internal typealias HMSConnectionResult = com.huawei.hms.api.ConnectionResult

fun Context.getMobileService(): MobileService =
    when {
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this) == GMSConnectionResult.SUCCESS -> MobileService.GMS
        HuaweiApiAvailability.getInstance()
            .isHuaweiMobileServicesAvailable(this) == HMSConnectionResult.SUCCESS -> MobileService.HMS
        else -> error("Unsupported mobile service type")
    }

