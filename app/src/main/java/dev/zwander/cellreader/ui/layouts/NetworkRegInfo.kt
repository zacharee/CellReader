package dev.zwander.cellreader.ui.layouts

import android.os.Build
import android.telephony.AccessNetworkConstants
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@Composable
fun NetworkRegInfo(
    networkRegistrationInfoList: List<NetworkRegistrationInfo>
) {
    networkRegistrationInfoList.forEach { networkRegistrationInfo ->
        Card(
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            border = BorderStroke(1.dp, Color.White),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        ) {
            FlowRow(
                mainAxisSize = SizeMode.Expand,
                mainAxisAlignment = MainAxisAlignment.SpaceBetween,
                mainAxisSpacing = 16.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                with(networkRegistrationInfo) {
                    this.dataSpecificInfo?.apply {
                        FormatText(R.string.endc_available_format, "$isEnDcAvailable")
                        FormatText(R.string.nr_available_format, "$isNrAvailable")
                        FormatText(R.string.dcnr_restricted_format, "$isDcNrRestricted")

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            vopsSupportInfo?.apply {
                                FormatText(
                                    R.string.vops_supported_format,
                                    "$isVopsSupported"
                                )
                                FormatText(
                                    R.string.vops_emergency_service_supported_format,
                                    "$isEmergencyServiceSupported"
                                )
                                FormatText(
                                    R.string.vops_emergency_service_fallback_supported_format,
                                    "$isEmergencyServiceFallbackSupported"
                                )
                            }
                        }
                    }

                    this.voiceSpecificInfo?.apply {
                        FormatText(R.string.prl_format, "$systemIsInPrl")

                        roamingIndicator.onNegAvail {
                            FormatText(
                                R.string.roaming_indicator_format,
                                "$roamingIndicator/$defaultRoamingIndicator"
                            )
                        }

                        FormatText(R.string.css_format, "$cssSupported")
                    }

                    FormatText(
                        R.string.transport_format,
                        AccessNetworkConstants.transportTypeToString(
                            transportType
                        )
                    )
                    FormatText(
                        R.string.access_format,
                        ServiceState.rilRadioTechnologyToString(
                            ServiceState.networkTypeToRilRadioTechnology(
                                accessNetworkTechnology
                            )
                        )
                    )

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
                        ServiceState.roamingTypeToString(
                            roamingType
                        )
                    )

                    FormatText(
                        R.string.registration_state_format,
                        NetworkRegistrationInfo.registrationStateToString(
                            registrationState
                        )
                    )
                    FormatText(R.string.reject_cause_format, "$rejectCause")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        FormatText(R.string.rplmn_format, registeredPlmn.asMccMnc)
                    }

                    FormatText(
                        R.string.services_format,
                        availableServices.joinToString(", ") {
                            NetworkRegistrationInfo.serviceTypeToString(
                                it
                            )
                        })
                    FormatText(R.string.domain_format, domainToString(domain))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        FormatText(
                            R.string.carrier_aggregation_format,
                            isUsingCarrierAggregation.toString()
                        )
                    }
                    FormatText(
                        R.string.nr_state_format,
                        CellUtils.nrStateToString(nrState)
                    )

                    cellIdentity?.apply {
                        CellIdentity(
                            cellIdentity = this,
                            simple = true,
                            advanced = true
                        )
                    }
                }
            }
        }
    }
}