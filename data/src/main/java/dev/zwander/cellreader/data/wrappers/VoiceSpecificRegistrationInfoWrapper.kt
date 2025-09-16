package dev.zwander.cellreader.data.wrappers

import android.telephony.VoiceSpecificRegistrationInfo

data class VoiceSpecificRegistrationInfoWrapper(
    val cssSupported: Boolean,
    val roamingIndicator: Int,
    val systemIsInPrl: Int,
    val defaultRoamingIndicator: Int,
) {
    constructor(info: VoiceSpecificRegistrationInfo) : this(
        cssSupported = info.cssSupported,
        roamingIndicator = info.roamingIndicator,
        systemIsInPrl = info.systemIsInPrl,
        defaultRoamingIndicator = info.defaultRoamingIndicator,
    )
}
