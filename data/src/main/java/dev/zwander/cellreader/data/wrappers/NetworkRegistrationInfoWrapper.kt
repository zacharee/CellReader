package dev.zwander.cellreader.data.wrappers

import android.content.Context
import android.os.Build
import android.telephony.NetworkRegistrationInfo
import android.telephony.NetworkRegistrationInfo.RegistrationState
import dev.zwander.cellreader.data.R

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
    companion object {
        fun registrationStateToString(context: Context, @RegistrationState registrationState: Int): String {
            return context.resources.getString(
                when (registrationState) {
                    NetworkRegistrationInfo.REGISTRATION_STATE_NOT_REGISTERED_OR_SEARCHING -> R.string.not_registered
                    NetworkRegistrationInfo.REGISTRATION_STATE_HOME -> R.string.home
                    NetworkRegistrationInfo.REGISTRATION_STATE_NOT_REGISTERED_SEARCHING -> R.string.not_reg_searching
                    NetworkRegistrationInfo.REGISTRATION_STATE_DENIED -> R.string.denied
                    NetworkRegistrationInfo.REGISTRATION_STATE_ROAMING -> R.string.unknown
                    else -> R.string.unknown
                }
            )
        }

        fun serviceTypeToString(context: Context, @NetworkRegistrationInfo.ServiceType serviceType: Int): String {
            return context.resources.getString(
                when (serviceType) {
                    NetworkRegistrationInfo.SERVICE_TYPE_VOICE -> R.string.voice
                    NetworkRegistrationInfo.SERVICE_TYPE_DATA -> R.string.data
                    NetworkRegistrationInfo.SERVICE_TYPE_SMS -> R.string.sms
                    NetworkRegistrationInfo.SERVICE_TYPE_VIDEO -> R.string.video
                    NetworkRegistrationInfo.SERVICE_TYPE_EMERGENCY -> R.string.emergency
                    else -> R.string.unknown
                }
            )
        }
    }

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
        try {
            info.isUsingCarrierAggregation
        } catch (e: Throwable) {
            false
        }
    )
}