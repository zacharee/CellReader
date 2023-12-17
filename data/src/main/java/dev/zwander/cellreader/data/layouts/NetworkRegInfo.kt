package dev.zwander.cellreader.data.layouts

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.data.CellSignalInfo
import dev.zwander.cellreader.data.util.*
import dev.zwander.cellreader.data.wrappers.NetworkRegistrationInfoWrapper
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NetworkRegInfo(
    networkRegistrationInfoList: List<NetworkRegistrationInfoWrapper>
) {
    val context = LocalContext.current

    networkRegistrationInfoList.forEach { networkRegistrationInfo ->
        Card(
            elevation = CardDefaults.outlinedCardElevation(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp).fillMaxWidth(),
        ) {
            FlowRow(
                horizontalArrangement = SpacedArrangement(
                    spacing = 16.dp,
                    arrangement = Arrangement.SpaceBetween,
                ),
                modifier = Modifier.padding(8.dp)
            ) {
                with(networkRegistrationInfo) {
                    FlowRow(
                        horizontalArrangement = SpacedArrangement(
                            spacing = 16.dp,
                            arrangement = Arrangement.SpaceAround,
                        ),
                    ) {
                        FormatText(
                            textId = R.string.domain_format,
                            textFormat = domainToString(context, domain),
                        )

                        FormatText(
                            R.string.access_format,
                            ServiceStateWrapper.rilRadioTechnologyToString(
                                context,
                                ServiceStateWrapper.networkTypeToRilRadioTechnology(
                                    accessNetworkTechnology
                                )
                            )
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            FormatText(R.string.rplmn_format, rplmn.asMccMnc)
                        }
                    }

                    FormatText(
                        R.string.transport_format,
                        AccessNetworkConstants.transportTypeToString(
                            context,
                            transportType
                        )
                    )
                    FormatText(
                        R.string.nr_state_format,
                        CellUtils.nrStateToString(context, nrState)
                    )

                    cellIdentity?.apply {
                        CellSignalInfo.Renderer.RenderIdentity(
                            identity = cellIdentity,
                            simple = true,
                            advanced = true,
                            defaultOrder = true
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        FormatText(R.string.registered_format, "$isRegistered")
                    }
                    FormatText(R.string.in_service_format, "$isInService")
                    FormatText(R.string.emergency_enabled_format, "$isEmergencyEnabled")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        FormatText(R.string.searching_format, "$isSearching")
                    }
                    FormatText(
                        R.string.roaming_format,
                        ServiceStateWrapper.roamingTypeToString(
                            context,
                            roamingType
                        )
                    )

                    FormatText(
                        R.string.registration_state_format,
                        NetworkRegistrationInfoWrapper.registrationStateToString(
                            context,
                            registrationState
                        )
                    )
                    FormatText(R.string.reject_cause_format, "$rejectCause")

                    FormatText(
                        R.string.services_format,
                        availableServices.joinToString(", ") {
                            NetworkRegistrationInfoWrapper.serviceTypeToString(
                                context,
                                it
                            )
                        })
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        FormatText(
                            R.string.carrier_aggregation_format,
                            isUsingCarrierAggregation.toString()
                        )
                    }

                    this.dataSpecificInfo?.apply {
                        WearSafeText(
                            text = stringResource(id = R.string.data_specific_info),
                            modifier = Modifier.fillMaxWidth()
                        )

                        FormatText(R.string.endc_available_format, "$isEnDcAvailable")
                        FormatText(R.string.nr_available_format, "$isNrAvailable")
                        FormatText(R.string.dcnr_restricted_format, "$isDcNrRestricted")

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            vopsSupportInfo?.apply {
                                FormatText(
                                    R.string.vops_supported_format,
                                    "$vopsSupported"
                                )
                                FormatText(
                                    R.string.vops_emergency_service_supported_format,
                                    "$emergencyServiceSupported"
                                )
                                FormatText(
                                    R.string.vops_emergency_service_fallback_supported_format,
                                    "$emergencyFallbackServiceSupported"
                                )
                            }
                        }
                    }

                    this.voiceSpecificInfo?.apply {
                        WearSafeText(
                            text = stringResource(id = R.string.voice_specific_info),
                            modifier = Modifier.fillMaxWidth()
                        )

                        FormatText(R.string.prl_format, "$systemIsInPrl")

                        roamingIndicator.onNegAvail {
                            FormatText(
                                R.string.roaming_indicator_format,
                                "$roamingIndicator/$defaultRoamingIndicator"
                            )
                        }

                        FormatText(R.string.css_format, "$cssSupported")
                    }
                }
            }
        }
    }
}