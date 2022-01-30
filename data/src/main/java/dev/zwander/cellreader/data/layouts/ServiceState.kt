package dev.zwander.cellreader.data.layouts

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
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.duplexModeToString
import dev.zwander.cellreader.data.util.onNegAvail
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper

@Composable
fun ServiceState(
    serviceState: ServiceStateWrapper
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
                operatorAlphaLong,
                operatorNumeric,
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
                HashSet(networkRegistrationInfos?.map { it.roamingType } ?: listOf())
                    .joinToString("/") { ServiceStateWrapper.roamingTypeToString(it) }
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
            setOf(dataRegState, voiceRegState).joinToString("/") { ServiceStateWrapper.rilServiceStateToString(it) }
        )
        FormatText(R.string.emergency_only_format, "$emergencyOnly")

        FormatText(
            R.string.network_type_format,
            setOf(dataNetworkType, voiceNetworkType).joinToString("/") {
                ServiceStateWrapper.rilRadioTechnologyToString(
                    ServiceStateWrapper.networkTypeToRilRadioTechnology(
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
        FormatText(R.string.manual_format, "$manualNetworkSelection")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            FormatText(R.string.iwlan_preferred_format, "$iWlanPreferred")
        }
        FormatText(R.string.cssi_format, "$cssIndicator")

        systemId.onNegAvail {
            FormatText(R.string.cdma_system_id_format, "$it")
        }
        networkId.onNegAvail {
            FormatText(R.string.cdma_network_id_format, "$it")
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
            networkRegistrationInfos?.let {
                NetworkRegInfo(networkRegistrationInfoList = networkRegistrationInfos)
            }
        }
    }
}