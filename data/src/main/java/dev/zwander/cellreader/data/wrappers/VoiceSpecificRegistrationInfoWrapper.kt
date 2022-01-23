package dev.zwander.cellreader.data.wrappers

import android.telephony.VoiceSpecificRegistrationInfo

data class VoiceSpecificRegistrationInfoWrapper(
    val cssSupported: Boolean,
    val roamingIndicator: Int,
    val systemIsInPrl: Int,
    val defaultRoamingIndicator: Int
) {
    constructor(info: VoiceSpecificRegistrationInfo) : this(
        info.cssSupported,
        info.roamingIndicator,
        info.systemIsInPrl,
        info.defaultRoamingIndicator
    )
}
