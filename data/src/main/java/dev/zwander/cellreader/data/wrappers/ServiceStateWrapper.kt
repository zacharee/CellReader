package dev.zwander.cellreader.data.wrappers

import android.os.Build
import android.telephony.ServiceState

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
) {
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
        state.isIwlanPreferred
    )
}