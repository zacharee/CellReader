package dev.zwander.cellreader.data.layouts

import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.PaddedDivider
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.duplexModeToString
import dev.zwander.cellreader.data.util.onNegAvail
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper

@Composable
fun ServiceState(
    serviceState: ServiceStateWrapper
) {
    val context = LocalContext.current

    with(serviceState) {
        WearSafeText(
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
            }.filterNotNull().joinToString("/")
        )

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            FormatText(
                R.string.roaming_type_format,
                HashSet(networkRegistrationInfos?.map { it.roamingType } ?: listOf())
                    .joinToString("/") { ServiceStateWrapper.roamingTypeToString(context, it) }
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
            setOf(
                dataRegState,
                voiceRegState
            ).joinToString("/") { ServiceStateWrapper.rilServiceStateToString(context, it) }
        )
        FormatText(R.string.emergency_only_format, "$emergencyOnly")

        isUsingNonTerrestrialNetwork?.let {
            FormatText(R.string.is_non_terrestrial_network, it)
        }

        FormatText(
            R.string.network_type_format,
            setOf(dataNetworkType, voiceNetworkType).joinToString("/") {
                ServiceStateWrapper.rilRadioTechnologyToString(
                    context,
                    ServiceStateWrapper.networkTypeToRilRadioTechnology(
                        it
                    )
                )
            }
        )

        FormatText(R.string.duplex_format, duplexModeToString(context, duplexMode))
        FormatText(R.string.channel_format, "$channelNumber")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            FormatText(R.string.searching_format, "$isSearching")
        }
        FormatText(R.string.manual_format, "$manualNetworkSelection")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            PaddedDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
            )

            WearSafeText(
                text = stringResource(id = R.string.network_registrations),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            networkRegistrationInfos?.let {
                NetworkRegInfo(networkRegistrationInfoList = networkRegistrationInfos)
            }
        }
    }
}