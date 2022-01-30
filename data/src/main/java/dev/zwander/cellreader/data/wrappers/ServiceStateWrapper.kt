package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.Annotation.NetworkType
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import android.telephony.TelephonyManager
import dev.zwander.cellreader.data.util.AccessNetworkUtils

data class ServiceStateWrapper(
    val operatorAlphaLong: String?,
    val operatorAlphaShort: String?,
    val operatorNumeric: String?,
    val manualNetworkSelection: Boolean,
    val emergencyOnly: Boolean,
    val cssIndicator: Int,
    val networkId: Int,
    val systemId: Int,
    val cdmaRoamingIndicator: Int,
    val cdmaDefaultRoamingIndicator: Int,
    val cdmaEriIconIndex: Int,
    val cdmaEriIconMode: Int,
    val nrFrequencyRange: Int,
    val channelNumber: Int,
    val cellBandwidths: ArrayList<Int>,
    val arfcnRsrpBoost: Int,
    val networkRegistrationInfos: ArrayList<NetworkRegistrationInfoWrapper>?,
    val operatorAlphaLongRaw: String?,
    val operatorAlphaShortRaw: String?,
    val dataRoamingFromRegistration: Boolean,
    val iWlanPreferred: Boolean,
    val dataRegState: Int,
    val voiceRegState: Int
) {
    companion object {
        fun isPsOnlyTech(radioTechnology: Int): Boolean {
            return radioTechnology == ServiceState.RIL_RADIO_TECHNOLOGY_LTE || radioTechnology == ServiceState.RIL_RADIO_TECHNOLOGY_LTE_CA || radioTechnology == ServiceState.RIL_RADIO_TECHNOLOGY_NR
        }

        /**
         * Transform network type [NetworkType] value to RIL radio technology
         * [RilRadioTechnology].
         *
         * @param networkType The network type [NetworkType].
         * @return The RIL radio technology [RilRadioTechnology].
         *
         * @hide
         */
        fun networkTypeToRilRadioTechnology(networkType: Int): Int {
            return when (networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS -> ServiceState.RIL_RADIO_TECHNOLOGY_GPRS
                TelephonyManager.NETWORK_TYPE_EDGE -> ServiceState.RIL_RADIO_TECHNOLOGY_EDGE
                TelephonyManager.NETWORK_TYPE_UMTS -> ServiceState.RIL_RADIO_TECHNOLOGY_UMTS
                TelephonyManager.NETWORK_TYPE_HSDPA -> ServiceState.RIL_RADIO_TECHNOLOGY_HSDPA
                TelephonyManager.NETWORK_TYPE_HSUPA -> ServiceState.RIL_RADIO_TECHNOLOGY_HSUPA
                TelephonyManager.NETWORK_TYPE_HSPA -> ServiceState.RIL_RADIO_TECHNOLOGY_HSPA
                TelephonyManager.NETWORK_TYPE_CDMA -> ServiceState.RIL_RADIO_TECHNOLOGY_IS95A
                TelephonyManager.NETWORK_TYPE_1xRTT -> ServiceState.RIL_RADIO_TECHNOLOGY_1xRTT
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_0
                TelephonyManager.NETWORK_TYPE_EVDO_A -> ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_A
                TelephonyManager.NETWORK_TYPE_EVDO_B -> ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_B
                TelephonyManager.NETWORK_TYPE_EHRPD -> ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD
                TelephonyManager.NETWORK_TYPE_LTE -> ServiceState.RIL_RADIO_TECHNOLOGY_LTE
                TelephonyManager.NETWORK_TYPE_HSPAP -> ServiceState.RIL_RADIO_TECHNOLOGY_HSPAP
                TelephonyManager.NETWORK_TYPE_GSM -> ServiceState.RIL_RADIO_TECHNOLOGY_GSM
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> ServiceState.RIL_RADIO_TECHNOLOGY_TD_SCDMA
                TelephonyManager.NETWORK_TYPE_IWLAN -> ServiceState.RIL_RADIO_TECHNOLOGY_IWLAN
                TelephonyManager.NETWORK_TYPE_LTE_CA -> ServiceState.RIL_RADIO_TECHNOLOGY_LTE_CA
                TelephonyManager.NETWORK_TYPE_NR -> ServiceState.RIL_RADIO_TECHNOLOGY_NR
                else -> ServiceState.RIL_RADIO_TECHNOLOGY_UNKNOWN
            }
        }
    }

    val voiceRoamingType: Int
        get() = getNetworkRegistrationInfo(
            NetworkRegistrationInfo.DOMAIN_CS,
            AccessNetworkConstants.TRANSPORT_TYPE_WWAN
        )?.roamingType ?: ServiceState.ROAMING_TYPE_NOT_ROAMING

    val dataRoamingType: Int
        get() = getNetworkRegistrationInfo(
            NetworkRegistrationInfo.DOMAIN_PS,
            AccessNetworkConstants.TRANSPORT_TYPE_WWAN
        )?.roamingType ?: ServiceState.ROAMING_TYPE_NOT_ROAMING

    val voiceNetworkType: Int
        get() = getNetworkRegistrationInfo(
            NetworkRegistrationInfo.DOMAIN_CS,
            AccessNetworkConstants.TRANSPORT_TYPE_WWAN
        )?.accessNetworkTechnology ?: TelephonyManager.NETWORK_TYPE_UNKNOWN

    val dataNetworkType: Int
        get() = getNetworkRegistrationInfo(
            NetworkRegistrationInfo.DOMAIN_PS,
            AccessNetworkConstants.TRANSPORT_TYPE_WWAN
        )?.accessNetworkTechnology ?: TelephonyManager.NETWORK_TYPE_UNKNOWN

    val roaming: Boolean
        get() = voiceRoamingType != ServiceState.ROAMING_TYPE_NOT_ROAMING
                || dataRoamingType != ServiceState.ROAMING_TYPE_NOT_ROAMING

    val duplexMode: Int
        get() {
            if (!isPsOnlyTech(rilDataRadioTechology)) {
                return ServiceState.DUPLEX_MODE_UNKNOWN
            }

            return AccessNetworkUtils.getOperatingBandForEarfcn(channelNumber).run {
                AccessNetworkUtils.getDuplexModeForEutranBand(this)
            }
        }

    val rilDataRadioTechology: Int
        get() = networkTypeToRilRadioTechnology(dataNetworkType)

    val isSearching: Boolean
        get() {
            getNetworkRegistrationInfo(
                NetworkRegistrationInfo.DOMAIN_PS,
                AccessNetworkConstants.TRANSPORT_TYPE_WWAN
            )?.apply {
                if (registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_NOT_REGISTERED_SEARCHING) {
                    return true
                }
            }

            getNetworkRegistrationInfo(
                NetworkRegistrationInfo.DOMAIN_CS,
                AccessNetworkConstants.TRANSPORT_TYPE_WWAN
            )?.apply {
                if (registrationState == NetworkRegistrationInfo.REGISTRATION_STATE_NOT_REGISTERED_SEARCHING) {
                    return true
                }
            }

            return false
        }

    val isUsingCarrierAggregation: Boolean
        get() {
            if (cellBandwidths.isNotEmpty()) return true

            return networkRegistrationInfos?.any {
                it.isUsingCarrierAggregation
            } ?: false
        }

    val nrState: Int
        get() = getNetworkRegistrationInfo(
            NetworkRegistrationInfo.DOMAIN_PS,
            AccessNetworkConstants.TRANSPORT_TYPE_WWAN
        )?.nrState ?: NetworkRegistrationInfo.NR_STATE_NONE

    fun getNetworkRegistrationInfo(domain: Int, transportType: Int): NetworkRegistrationInfoWrapper? {
        return networkRegistrationInfos?.first {
            it.transportType == transportType
                    && (it.domain and domain) != 0
        }
    }

    fun getNetworkRegistrationInfoListForTransportType(transportType: Int): List<NetworkRegistrationInfoWrapper> {
        return networkRegistrationInfos?.filter {
            it.transportType == transportType
        } ?: listOf()
    }

    constructor(state: ServiceState) : this(
        state.operatorAlphaLong,
        state.operatorAlphaShort,
        state.operatorNumeric,
        state.isManualSelection,
        state.isEmergencyOnly,
        state.cssIndicator,
        state.cdmaNetworkId,
        state.cdmaSystemId,
        state.cdmaRoamingIndicator,
        state.cdmaDefaultRoamingIndicator,
        state.cdmaEriIconIndex,
        state.cdmaEriIconMode,
        state.nrFrequencyRange,
        state.channelNumber,
        ArrayList(state.cellBandwidths.toList()),
        state.arfcnRsrpBoost,
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ArrayList(state.networkRegistrationInfoList.map { NetworkRegistrationInfoWrapper(it) })
        } else {
            null
        },
        state.operatorAlphaLongRaw,
        state.operatorAlphaShortRaw,
        state.dataRoamingFromRegistration,
        state.isIwlanPreferred,
        state.dataRegState,
        state.voiceRegState
    )
}