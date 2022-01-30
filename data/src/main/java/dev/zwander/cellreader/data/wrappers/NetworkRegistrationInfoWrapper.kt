package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.NetworkRegistrationInfo

data class NetworkRegistrationInfoWrapper(
    val domain: Int,
    val transportType: Int,
    val registrationState: Int,
    val roamingType: Int,
    val accessNetworkTechnology: Int,
    val nrState: Int,
    val rejectCause: Int,
    val emergencyOnly: Boolean,
    val availableServices: ArrayList<Int>,
    val cellIdentity: CellIdentityWrapper?,
    val voiceSpecificInfo: VoiceSpecificRegistrationInfoWrapper?,
    val dataSpecificInfo: DataSpecificRegistrationInfoWrapper?,
    val rplmn: String?,
    val isUsingCarrierAggregation: Boolean,
) {
    val isRegistered: Boolean
        get() = registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_HOME
                || registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_ROAMING

    val isInService: Boolean
        get() = registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_HOME
                || registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_ROAMING

    val isEmergencyEnabled: Boolean
        get() = emergencyOnly

    val isSearching: Boolean
        get() = registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_NOT_REGISTERED_SEARCHING

    constructor(info: NetworkRegistrationInfo) : this(
        info.domain,
        info.transportType,
        info.registrationState,
        info.roamingType,
        info.accessNetworkTechnology,
        info.nrState,
        info.rejectCause,
        info.isEmergencyEnabled,
        ArrayList(info.availableServices),
        CellIdentityWrapper.newInstance(info.cellIdentity),
        info.voiceSpecificInfo?.let { VoiceSpecificRegistrationInfoWrapper(it) },
        info.dataSpecificInfo?.let { DataSpecificRegistrationInfoWrapper(it) },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) info.registeredPlmn else null,
        info.isUsingCarrierAggregation
    )
}