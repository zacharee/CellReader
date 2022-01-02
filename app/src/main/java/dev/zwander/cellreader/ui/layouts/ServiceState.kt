package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.ServiceState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.R
import dev.zwander.cellreader.ui.components.PaddedDivider
import dev.zwander.cellreader.utils.FormatText
import dev.zwander.cellreader.utils.duplexModeToString
import dev.zwander.cellreader.utils.onNegAvail

@Composable
fun ServiceState(
    serviceState: ServiceState
) {
    with (serviceState) {
        Text(
            text = stringResource(id = R.string.service_state),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        FormatText(
            R.string.operator_format,
            mutableSetOf(
                operatorAlphaShort,
                operatorAlpha,
                operatorAlphaLong,
                operatorNumeric,
                dataOperatorAlphaShort,
                dataOperatorNumeric,
                voiceOperatorAlphaLong,
                voiceOperatorAlphaShort,
                voiceOperatorNumeric
            ).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(operatorAlphaShortRaw)
                    add(operatorAlphaLongRaw)
                }
            }.joinToString("/")
        )

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            FormatText(
                R.string.roaming_type_format,
                HashSet(networkRegistrationInfoList.map { it.roamingType })
                    .joinToString("/") { ServiceState.roamingTypeToString(it) }
            )
        } else {
            FormatText(
                R.string.roaming_format,
                roaming.toString()
            )
        }
        FormatText(R.string.data_roaming_from_reg_format, "$dataRoamingFromRegistration")

        FormatText(
            R.string.state_format,
            setOf(dataRegState, voiceRegState).joinToString("/") { ServiceState.rilServiceStateToString(it) }
        )
        FormatText(R.string.emergency_only_format, "$isEmergencyOnly")

        FormatText(
            R.string.network_type_format,
            setOf(dataNetworkType, voiceNetworkType).joinToString("/") {
                ServiceState.rilRadioTechnologyToString(
                    ServiceState.networkTypeToRilRadioTechnology(
                        it
                    )
                )
            }
        )

        FormatText(R.string.bandwidths_format, cellBandwidths.joinToString(", "))
        FormatText(R.string.duplex_format, duplexModeToString(duplexMode))
        FormatText(R.string.channel_format, "$channelNumber")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            FormatText(R.string.searching_format, "$isSearching")
        }
        FormatText(R.string.manual_format, "$isManualSelection")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            FormatText(R.string.iwlan_preferred_format, "$isIwlanPreferred")
        }
        FormatText(R.string.cssi_format, "$cssIndicator")

        cdmaSystemId.onNegAvail {
            FormatText(R.string.cdma_system_id_format, "$cdmaSystemId")
        }
        cdmaNetworkId.onNegAvail {
            FormatText(R.string.cdma_network_id_format, "$cdmaNetworkId")
        }

        cdmaRoamingIndicator.onNegAvail {
            FormatText(
                R.string.cdma_roaming_indicator_format,
                "${cdmaRoamingIndicator}/${cdmaDefaultRoamingIndicator}"
            )
        }
        cdmaEriIconMode.onNegAvail {
            FormatText(
                R.string.cdma_eri_icon_format,
                "${cdmaEriIconMode}/${cdmaEriIconIndex}"
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            FormatText(R.string.arfcn_rsrp_boost_format, "$arfcnRsrpBoost")
        }

        PaddedDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.network_registrations),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            NetworkRegInfo(networkRegistrationInfoList = networkRegistrationInfoList)
        }
    }
}