package dev.zwander.cellreader.data.wrappers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.NetworkRegistrationInfo
import android.telephony.NetworkRegistrationInfo.RegistrationState
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.withMinApi
import dev.zwander.cellreader.data.util.withTryCatch

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
    val isNonTerrestrialNetwork: Boolean?,
) {
    companion object {
        fun registrationStateToString(
            context: Context,
            @RegistrationState registrationState: Int,
        ): String {
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

        fun serviceTypeToString(
            context: Context,
            @NetworkRegistrationInfo.ServiceType serviceType: Int,
        ): String {
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
        domain = info.domain,
        transportType = info.transportType,
        registrationState = @Suppress("Deprecation")
            withTryCatch(info.registrationState) {
                info.networkRegistrationState
            },
        roamingType = info.roamingType,
        accessNetworkTechnology = info.accessNetworkTechnology,
        nrState = info.nrState,
        rejectCause = info.rejectCause,
        emergencyOnly = info.isEmergencyEnabled,
        availableServices = ArrayList(info.availableServices),
        cellIdentity = info.cellIdentity?.let { CellIdentityWrapper.newInstance(it) },
        voiceSpecificInfo = info.voiceSpecificInfo?.let { VoiceSpecificRegistrationInfoWrapper(it) },
        dataSpecificInfo = info.dataSpecificInfo?.let { DataSpecificRegistrationInfoWrapper(it) },
        rplmn = withMinApi(Build.VERSION_CODES.R) {
            info.registeredPlmn
        },
        isUsingCarrierAggregation = withTryCatch(false) {
            info.isUsingCarrierAggregation
        },
        isNonTerrestrialNetwork = withMinApi(Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            info.isNonTerrestrialNetwork
        },
    )
}