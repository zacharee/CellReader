package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.ClosedSubscriberGroupInfo
import androidx.annotation.RequiresApi

data class ClosedSubscriberGroupInfoWrapper(
    val csgIndicator: Boolean,
    val homeNodebName: String?,
    val csgIdentity: Int
) {
    @RequiresApi(Build.VERSION_CODES.R)
    constructor(info: ClosedSubscriberGroupInfo) : this(
        csgIndicator = info.csgIndicator,
        homeNodebName = info.homeNodebName,
        csgIdentity = info.csgIdentity,
    )
}