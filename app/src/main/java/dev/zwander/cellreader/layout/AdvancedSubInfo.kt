package dev.zwander.cellreader.layout

import android.annotation.SuppressLint
import android.telephony.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    text = "Signal Strength",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text("Level: $level")
                Text("Timestamp: $timestampMillis")
            }

            Spacer(Modifier.size(4.dp))
            Divider()
            Spacer(Modifier.size(4.dp))

            telephony.serviceState?.apply {
                Text(
                    text = "Service State",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    "Operator: " +
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

                Text("Roaming: ${roaming}/${dataRoaming}/${voiceRoaming}")
                Text(
                    "Roaming Type: ${ServiceState.roamingTypeToString(dataRoamingType)}/${
                        ServiceState.roamingTypeToString(
                            voiceRoamingType
                        )
                    }"
                )
                Text("Data Roaming From Reg: $dataRoamingFromRegistration")

                Text(
                    "State: ${ServiceState.rilServiceStateToString(dataRegState)}/${
                        ServiceState.rilServiceStateToString(
                            voiceRegState
                        )
                    }"
                )
                Text("Emergency Only: $isEmergencyOnly")

                Text(
                    "Network Type: ${
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

                Text("Bandwidths: ${cellBandwidths.joinToString(", ")}")
                Text("Duplex: ${duplexModeToString(duplexMode)}")
                Text("Channel: $channelNumber")

                Text("Searching: $isSearching")
                Text("Manual: $isManualSelection")

                Text("IWLAN Preferred: $isIwlanPreferred")
                Text("CSSI: $cssIndicator")

                cdmaSystemId.onNegAvail {
                    Text("CDMA Sys ID: $cdmaSystemId")
                }
                cdmaNetworkId.onNegAvail {
                    Text("CDMA Net ID: $cdmaNetworkId")
                }
                cdmaRoamingIndicator.onNegAvail {
                    Text("CDMA Roaming Indicator: ${cdmaRoamingIndicator}/${cdmaDefaultRoamingIndicator}")
                }
                cdmaEriIconMode.onNegAvail {
                    Text("CDMA ERI Icon: ${cdmaEriIconMode}/${cdmaEriIconIndex}")
                }
                Text("ARFCN RSRP Boost: $arfcnRsrpBoost")

                Spacer(Modifier.size(4.dp))
                Divider(modifier = Modifier.padding(start = 32.dp, end = 32.dp))
                Spacer(Modifier.size(4.dp))

                Text(
                    text = "Network Registrations",
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
                                    Text("ENDC Available: $isEnDcAvailable")
                                    Text("NR Available: $isNrAvailable")
                                    Text("DCNR Restricted: $isDcNrRestricted")

                                    vopsSupportInfo?.apply {
                                        Text("VoPS: $isVopsSupported")
                                        Text("VoPS Emergency: $isEmergencyServiceSupported")
                                        Text("VoPS Fallback: $isEmergencyServiceFallbackSupported")
                                    }
                                }

                                this.voiceSpecificInfo?.apply {
                                    Text("PRL: $systemIsInPrl")

                                    roamingIndicator.onNegAvail {
                                        Text("Roaming Indicator: $roamingIndicator/$defaultRoamingIndicator")
                                    }

                                    Text("CSS: $cssSupported")
                                }

                                Text(
                                    "Transport: ${
                                        AccessNetworkConstants.transportTypeToString(
                                            transportType
                                        )
                                    }"
                                )
                                Text(
                                    "Access: ${
                                        ServiceState.rilRadioTechnologyToString(
                                            ServiceState.networkTypeToRilRadioTechnology(
                                                accessNetworkTechnology
                                            )
                                        )
                                    }"
                                )

                                Text("Registered: $isRegistered")
                                Text("In Service: $isInService")
                                Text("Emergency Enabled: $isEmergencyEnabled")
                                Text("Searching: $isSearching")
                                Text(
                                    "Roaming: $isRoaming/${
                                        ServiceState.roamingTypeToString(
                                            roamingType
                                        )
                                    }"
                                )

                                Text(
                                    "Registration State: ${
                                        NetworkRegistrationInfo.registrationStateToString(
                                            registrationState
                                        )
                                    }"
                                )
                                Text("Reject Cause: $rejectCause")

                                Text("R-PLMN: $registeredPlmn")

                                Text("Services: ${
                                    availableServices.joinToString(", ") {
                                        NetworkRegistrationInfo.serviceTypeToString(
                                            it
                                        )
                                    }
                                }")
                                Text("Domain: ${domainToString(domain)}")
                                Text("CA: $isUsingCarrierAggregation")
                                Text("NR: ${NetworkRegistrationInfo.nrStateToString(nrState)}")

                                cellIdentity?.apply {
                                    channelNumber.onAvail {
                                        Text("Channel: $channelNumber")
                                    }

                                    if (operatorAlphaLong != null || operatorAlphaShort != null) {
                                        Text(
                                            "Operator: ${
                                                setOf(
                                                    operatorAlphaLong,
                                                    operatorAlphaShort
                                                ).joinToString("/")
                                            }"
                                        )
                                    }

                                    globalCellId?.apply {
                                        Text("GCI: $globalCellId")
                                    }
                                    mccString?.apply {
                                        Text("MCC-MNC: ${mccString}-${mncString}")
                                    }
                                    plmn?.apply {
                                        Text("PLMN: $plmn")
                                    }
                                    Text("Type: ${typeToString(type)}")

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
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                    }

                                    cast<CellIdentityCdma>()?.apply {
                                        longitude.onAvail {
                                            Text("Lat/Lon: ${latitude}/${longitude}")
                                        }
                                        networkId.onAvail {
                                            Text("Net ID: $it")
                                        }
                                        basestationId.onAvail {
                                            Text("Basestation ID: $it")
                                        }
                                        systemId.onAvail {
                                            Text("System ID: $it")
                                        }
                                    }

                                    cast<CellIdentityWcdma>()?.apply {
                                        lac.onAvail {
                                            Text("LAC: $it")
                                        }
                                        cid.onAvail {
                                            Text("CID: $it")
                                        }
                                        uarfcn.onAvail {
                                            Text("UARFCN: $it")
                                        }
                                        mobileNetworkOperator?.apply {
                                            Text("Operator: $this")
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }

                                        this.closedSubscriberGroupInfo?.apply {
                                            Text("CSG ID: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                    }

                                    cast<CellIdentityTdscdma>()?.apply {
                                        lac.onAvail {
                                            Text("LAC: $it")
                                        }
                                        cid.onAvail {
                                            Text("CID: $it")
                                        }
                                        cpid.onAvail {
                                            Text("CPID: $it")
                                        }
                                        uarfcn.onAvail {
                                            Text("UARFCN: $it")
                                        }
                                        mobileNetworkOperator?.apply {
                                            Text("Operator: $this")
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }

                                        this.closedSubscriberGroupInfo?.apply {
                                            Text("CSG ID: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                    }

                                    cast<CellIdentityLte>()?.apply {
                                        tac.onAvail {
                                            Text("TAC: $it")
                                        }
                                        ci.onAvail {
                                            Text("CI: $it")
                                        }
                                        pci.onAvail {
                                            Text("PCI: $it")
                                        }
                                        earfcn.onAvail {
                                            Text("EARFCN: $it")
                                        }
                                        mobileNetworkOperator?.apply {
                                            Text("Operator: $this")
                                        }
                                        bandwidth.onAvail {
                                            Text("Bandwidth: $it")
                                        }
                                        if (bands.isNotEmpty()) {
                                            Text("Bands: ${bands.joinToString(", ")}")
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }

                                        this.closedSubscriberGroupInfo?.apply {
                                            Text("CSG ID: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                    }

                                    cast<CellIdentityNr>()?.apply {
                                        tac.onAvail {
                                            Text("TAC: $it")
                                        }
                                        nci.onAvail {
                                            Text("NCI: $it")
                                        }
                                        pci.onAvail {
                                            Text("PCI: $it")
                                        }
                                        nrarfcn.onAvail {
                                            Text("NRARFCN: $it")
                                        }
                                        if (bands.isNotEmpty()) {
                                            Text("Bands: ${bands.joinToString(", ")}")
                                        }
                                        if (!additionalPlmns.isNullOrEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
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
                    Spacer(Modifier.size(4.dp))
                    Divider()
                    Spacer(Modifier.size(4.dp))

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