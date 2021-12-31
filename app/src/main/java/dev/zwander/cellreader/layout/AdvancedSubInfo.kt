package dev.zwander.cellreader.layout

import android.annotation.SuppressLint
import android.telephony.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.*

@SuppressLint("MissingPermission")
@Composable
fun AdvancedSubInfo(telephony: TelephonyManager, subs: SubscriptionManager) {
    ProvideTextStyle(value = LocalTextStyle.current.copy(textAlign = TextAlign.Center)) {
        FlowRow(
            mainAxisSpacing = 16.dp,
            mainAxisAlignment = MainAxisAlignment.Center,
            mainAxisSize = SizeMode.Expand
        ) {
            with(telephony.signalStrength) {
                Text(
                    text = stringResource(id = R.string.signal_strength),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                FormatText(R.string.level_format, "$level")
                FormatText(R.string.timestamp_format, "$timestampMillis")
            }

            PaddedDivider(modifier = Modifier.fillMaxWidth())

            telephony.serviceState?.apply {
                Text(
                    text = stringResource(id = R.string.service_state),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                FormatText(
                    R.string.operator_format,
                    setOf(
                        operatorAlphaShort,
                        operatorAlpha,
                        operatorAlphaLong,
                        operatorAlphaShortRaw,
                        operatorAlphaLongRaw,
                        operatorNumeric,
                        dataOperatorAlphaShort,
                        dataOperatorNumeric,
                        voiceOperatorAlphaLong,
                        voiceOperatorAlphaShort,
                        voiceOperatorNumeric
                    ).joinToString("/")
                )

                FormatText(R.string.roaming_format, "${roaming}/${dataRoaming}/${voiceRoaming}")
                FormatText(
                    R.string.roaming_type_format,
                    "${ServiceState.roamingTypeToString(dataRoamingType)}/${
                        ServiceState.roamingTypeToString(
                            voiceRoamingType
                        )
                    }"
                )
                FormatText(R.string.data_roaming_from_reg_format, "$dataRoamingFromRegistration")

                FormatText(
                    R.string.state_format,
                    "${ServiceState.rilServiceStateToString(dataRegState)}/${
                        ServiceState.rilServiceStateToString(
                            voiceRegState
                        )
                    }"
                )
                FormatText(R.string.emergency_only_format, "$isEmergencyOnly")

                FormatText(
                    R.string.network_type_format,
                    "${
                        ServiceState.rilRadioTechnologyToString(
                            ServiceState.networkTypeToRilRadioTechnology(
                                voiceNetworkType
                            )
                        )
                    }/" + ServiceState.rilRadioTechnologyToString(
                        ServiceState.networkTypeToRilRadioTechnology(
                            dataNetworkType
                        )
                    )
                )

                FormatText(R.string.bandwidths_format,"${cellBandwidths.joinToString(", ")}")
                FormatText(R.string.duplex_format, "${duplexModeToString(duplexMode)}")
                FormatText(R.string.channel_format, "$channelNumber")

                FormatText(R.string.searching_format, "$isSearching")
                FormatText(R.string.manual_format, "$isManualSelection")

                FormatText(R.string.iwlan_preferred_format, "$isIwlanPreferred")
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
                    FormatText(R.string.cdma_eri_icon_format, "${cdmaEriIconMode}/${cdmaEriIconIndex}")
                }
                FormatText(R.string.arfcn_rsrp_boost_format, "$arfcnRsrpBoost")

                PaddedDivider(modifier = Modifier.fillMaxWidth())

                Text(
                    text = stringResource(id = R.string.network_registrations),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Card(
                    elevation = 8.dp
                ) {
                    FlowRow(
                        mainAxisSize = SizeMode.Expand,
                        mainAxisAlignment = MainAxisAlignment.Center,
                        mainAxisSpacing = 16.dp,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        networkRegistrationInfoList.forEach { networkRegistrationInfo ->
                            with(networkRegistrationInfo) {
                                this.dataSpecificInfo?.apply {
                                    FormatText(R.string.endc_available_format, "$isEnDcAvailable")
                                    FormatText(R.string.nr_available_format, "$isNrAvailable")
                                    FormatText(R.string.dcnr_restricted_format, "$isDcNrRestricted")

                                    vopsSupportInfo?.apply {
                                        FormatText(R.string.vops_supported_format, "$isVopsSupported")
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

                                FormatText(R.string.registered_format, "$isRegistered")
                                FormatText(R.string.in_service_format, "$isInService")
                                FormatText(R.string.emergency_enabled_format, "$isEmergencyEnabled")
                                FormatText(R.string.searching_format, "$isSearching")
                                FormatText(
                                    R.string.roaming_format,
                                    "$isRoaming/${
                                        ServiceState.roamingTypeToString(
                                            roamingType
                                        )
                                    }"
                                )

                                FormatText(
                                    R.string.registration_state_format,
                                    NetworkRegistrationInfo.registrationStateToString(
                                        registrationState
                                    )
                                )
                                FormatText(R.string.reject_cause_format, "$rejectCause")

                                FormatText(R.string.rplmn_format, registeredPlmn)

                                FormatText(
                                    R.string.services_format,
                                    availableServices.joinToString(", ") {
                                        NetworkRegistrationInfo.serviceTypeToString(
                                            it
                                        )
                                    })
                                FormatText(R.string.domain_format, domainToString(domain))
                                FormatText(
                                    R.string.carrier_aggregation_format,
                                    isUsingCarrierAggregation.toString()
                                )
                                FormatText(
                                    R.string.nr_state_format,
                                    NetworkRegistrationInfo.nrStateToString(nrState).toString()
                                )

                                cellIdentity?.apply {
                                    channelNumber.onAvail {
                                        FormatText(R.string.channel_format, it.toString())
                                    }

                                    if (operatorAlphaLong != null || operatorAlphaShort != null) {
                                        FormatText(
                                            R.string.operator_format,
                                            setOf(
                                                operatorAlphaLong,
                                                operatorAlphaShort
                                            ).joinToString("/")
                                        )
                                    }

                                    globalCellId?.apply {
                                        FormatText(R.string.gci_format, this)
                                    }
                                    mccString?.apply {
                                        FormatText(R.string.mcc_mnc_format, "${mccString}-${mncString}")
                                    }
                                    plmn?.apply {
                                        FormatText(R.string.plmn_format, this)
                                    }
                                    FormatText(R.string.type_format, type.toString())

                                    cast<CellIdentityGsm>()?.apply {
                                        lac.onAvail {
                                            Text("LAC: $it")
                                        }
                                        cid.onAvail {
                                            Text("CID: $it")
                                        }
                                        bsic.onAvail {
                                            Text("BSIC: $bsic")
                                        }
                                        arfcn.onAvail {
                                            Text("ARFCN: $arfcn")
                                        }
                                        mobileNetworkOperator?.apply {
                                            FormatText(R.string.operator_format, this)
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            FormatText(
                                                R.string.additional_plmns_format,
                                                additionalPlmns.joinToString(", ")
                                            )
                                        }
                                    }

                                    cast<CellIdentityCdma>()?.apply {
                                        longitude.onAvail {
                                            FormatText(
                                                R.string.lat_lon_format,
                                                "${latitude}/${longitude}"
                                            )
                                        }
                                        networkId.onAvail {
                                            FormatText(R.string.cdma_network_id_format, it.toString())
                                        }
                                        basestationId.onAvail {
                                            FormatText(R.string.basestation_id_format, it.toString())
                                        }
                                        systemId.onAvail {
                                            FormatText(R.string.cdma_system_id_format, it.toString())
                                        }
                                    }

                                    cast<CellIdentityWcdma>()?.apply {
                                        lac.onAvail {
                                            FormatText(R.string.lac_format, it.toString())
                                        }
                                        cid.onAvail {
                                            FormatText(R.string.cid_format, it.toString())
                                        }
                                        uarfcn.onAvail {
                                            FormatText(R.string.uarfcn_format, it.toString())
                                        }
                                        mobileNetworkOperator?.apply {
                                            FormatText(R.string.operator_format, this)
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            FormatText(
                                                R.string.additional_plmns_format,
                                                additionalPlmns.joinToString(", ")
                                            )
                                        }

                                        this.closedSubscriberGroupInfo?.apply {
                                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                                            FormatText(
                                                R.string.csg_indicator_format,
                                                csgIndicator.toString()
                                            )
                                            FormatText(R.string.home_node_b_name_format, homeNodebName)
                                        }
                                    }

                                    cast<CellIdentityTdscdma>()?.apply {
                                        lac.onAvail {
                                            FormatText(R.string.lac_format, it.toString())
                                        }
                                        cid.onAvail {
                                            FormatText(R.string.cid_format, it.toString())
                                        }
                                        cpid.onAvail {
                                            FormatText(R.string.cpid_format, it.toString())
                                        }
                                        uarfcn.onAvail {
                                            FormatText(R.string.uarfcn_format, it.toString())
                                        }
                                        mobileNetworkOperator?.apply {
                                            FormatText(R.string.operator_format, this)
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            FormatText(
                                                R.string.additional_plmns_format,
                                                additionalPlmns.joinToString(", ")
                                            )
                                        }

                                        this.closedSubscriberGroupInfo?.apply {
                                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                                            FormatText(
                                                R.string.csg_indicator_format,
                                                csgIndicator.toString()
                                            )
                                            FormatText(R.string.home_node_b_name_format, homeNodebName)
                                        }
                                    }

                                    cast<CellIdentityLte>()?.apply {
                                        tac.onAvail {
                                            FormatText(R.string.tac_format, it.toString())
                                        }
                                        ci.onAvail {
                                            FormatText(R.string.ci_format, it.toString())
                                        }
                                        pci.onAvail {
                                            FormatText(R.string.pci_format, it.toString())
                                        }
                                        earfcn.onAvail {
                                            FormatText(R.string.earfcn_format, it.toString())
                                        }
                                        mobileNetworkOperator?.apply {
                                            FormatText(R.string.operator_format, this)
                                        }
                                        bandwidth.onAvail {
                                            FormatText(R.string.bandwidth_format, it.toString())
                                        }
                                        if (bands.isNotEmpty()) {
                                            FormatText(R.string.bands_format, bands.joinToString(", "))
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            FormatText(
                                                R.string.additional_plmns_format,
                                                additionalPlmns.joinToString(", ")
                                            )
                                        }

                                        this.closedSubscriberGroupInfo?.apply {
                                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                                            FormatText(
                                                R.string.csg_indicator_format,
                                                csgIndicator.toString()
                                            )
                                            FormatText(R.string.home_node_b_name_format, homeNodebName)
                                        }
                                    }

                                    cast<CellIdentityNr>()?.apply {
                                        tac.onAvail {
                                            FormatText(R.string.tac_format, it.toString())
                                        }
                                        nci.onAvail {
                                            FormatText(R.string.nci_format, it.toString())
                                        }
                                        pci.onAvail {
                                            FormatText(
                                                textId = R.string.pci_format,
                                                it.toString()
                                            )
                                        }
                                        nrarfcn.onAvail {
                                            FormatText(
                                                textId = R.string.nrarfcn_format,
                                                it.toString()
                                            )
                                        }
                                        if (bands.isNotEmpty()) {
                                            FormatText(
                                                textId = R.string.bands_format,
                                                bands.joinToString(", ")
                                            )
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            FormatText(
                                                textId = R.string.additional_plmns_format,
                                                additionalPlmns.joinToString(", ")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            subs.allSubscriptionInfoList.find { it.subscriptionId == telephony.subscriptionId }
                ?.apply {
                    PaddedDivider(modifier = Modifier.fillMaxWidth())

                    Text(
                        text = "Subscription Info",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = MainAxisAlignment.Center,
                        mainAxisSize = SizeMode.Expand,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("SIM Slot: $simSlotIndex")
                        Text("Number: $number")
                        Text("Display Name: $displayName")
                        Text("Carrier Name: $carrierName")
                        Text("Carrier ID: $carrierId")
                        Text("Type: ${subscriptionTypeToString(subscriptionType)}")
                        Text("Sub ID: $subscriptionId")
                        Text("Profile Class: ${profileClassToString(profileClass)}")
                        Text("Name Source: ${nameSourceToString(nameSource)}")
                        Text("Opportunistic: $isOpportunistic")
                        Text("Embedded: $isEmbedded")
                        if (iccId.isNotBlank()) {
                            Text("ICC ID: $iccId")
                        }
                        if (hplmns.isNotEmpty()) {
                            Text("HPLMNs: ${hplmns.joinToString(", ")}")
                        }
                        if (ehplmns.isNotEmpty()) {
                            Text("EHPLMNs: ${ehplmns.joinToString(", ")}")
                        }
                        Text("Group Disabled: $isGroupDisabled")
                        this.groupUuid?.apply {
                            Text("Group UUID: $this")
                        }
                        this.groupOwner?.apply {
                            Text("Group Owner: $this")
                        }
                        Text("Country ISO: $countryIso")
                        if (cardString.isNotBlank()) {
                            Text("Card String: $cardString")
                        }
                        Text("Card ID: $cardId")
                        Text("Data Roaming: $dataRoaming")
                        Text("Access Rules: $allAccessRules")
                        Text("MCC-MNC: $mccString-$mncString")
                        Text("UICC Apps: ${areUiccApplicationsEnabled()}")
                    }
                }
        }
    }
}