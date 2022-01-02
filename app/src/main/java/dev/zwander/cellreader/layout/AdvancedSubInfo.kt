package dev.zwander.cellreader.layout

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.serviceStates
import dev.zwander.cellreader.signalStrengths
import dev.zwander.cellreader.subInfos
import dev.zwander.cellreader.utils.*

@SuppressLint("MissingPermission")
@Composable
fun AdvancedSubInfo(
    subId: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
            FlowRow(
                mainAxisSpacing = 16.dp,
                mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                mainAxisSize = SizeMode.Expand
            ) {
                signalStrengths[subId]?.apply {
                    Text(
                        text = stringResource(id = R.string.signal_strength),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    FormatText(R.string.level_format, "$level")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        FormatText(R.string.timestamp_format, "$timestampMillis")
                    }
                }

                PaddedDivider(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                )

                serviceStates[subId]?.apply {
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
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.network_registrations),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
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
                }

                subInfos[subId]?.apply {
                    PaddedDivider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.subscription_info),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                        mainAxisSize = SizeMode.Expand,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        FormatText(R.string.sim_slot_format, "$simSlotIndex")
                        FormatText(R.string.number_format, number)
                        FormatText(R.string.display_name_format, "$displayName")
                        FormatText(R.string.carrier_name_format, "$carrierName")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FormatText(R.string.carrier_id_format, "$carrierId")
                            FormatText(
                                R.string.subscription_type_format,
                                subscriptionTypeToString(subscriptionType)
                            )
                        }
                        FormatText(R.string.subscription_id_format, "$subscriptionId")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FormatText(
                                R.string.profile_class_format,
                                profileClassToString(profileClass)
                            )
                        }
                        FormatText(R.string.name_source_format, nameSourceToString(nameSource))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FormatText(R.string.opportunistic_format, "$isOpportunistic")
                        }
                        FormatText(R.string.embedded_format, "$isEmbedded")
                        if (iccId.isNotBlank()) {
                            FormatText(R.string.icc_id_format, iccId)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (hplmns.isNotEmpty()) {
                                FormatText(R.string.hplmns_format, hplmns.joinToString(", ") { it.asMccMnc })
                            }
                            if (ehplmns.isNotEmpty()) {
                                FormatText(R.string.ehplmns_format, ehplmns.joinToString(", ") { it.asMccMnc })
                            }
                            FormatText(R.string.group_disabled_format, "$isGroupDisabled")
                            this.groupUuid?.apply {
                                FormatText(R.string.group_uuid_format, "$this")
                            }
                            this.groupOwner?.apply {
                                FormatText(R.string.group_owner_format, this)
                            }
                        }
                        FormatText(R.string.country_iso_format, countryIso)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (cardString.isNotBlank()) {
                                FormatText(R.string.card_string_format, cardString)
                            }
                        }
                        FormatText(R.string.card_id_format, cardIdCompat)
                        FormatText(R.string.data_roaming_format, "$dataRoaming")
                        FormatText(R.string.access_rules_format, "$allAccessRulesCompat")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FormatText(R.string.mcc_mnc_format, "$mccString-$mncString")
                        } else {
                            FormatText(R.string.mcc_mnc_format, "$mcc-$mnc")
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            FormatText(R.string.uicc_apps_format, "${areUiccApplicationsEnabled()}")
                        }
                    }
                }
            }
        }
    }
}