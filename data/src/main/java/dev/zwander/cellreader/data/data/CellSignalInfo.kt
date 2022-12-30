package dev.zwander.cellreader.data.data

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.castGeneric
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellIdentityCdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityGsmWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityLteWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityNrWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityTdscdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityWcdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthCdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthGsmWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthLteWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthNrWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthTdscdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWcdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper

object CellSignalInfo {
    sealed class Keys<T>(@StringRes val label: Int, val defaultShown: Boolean, val render: @Composable T.() -> Unit) {
        abstract fun cast(info: Any): T?

        sealed class IdentityKeys<T : CellIdentityWrapper>(@StringRes label: Int, defaultShown: Boolean, render: @Composable T.() -> Unit) : Keys<T>(label, defaultShown, render) {
            object Bands : IdentityKeys<CellIdentityWrapper>(R.string.bands_format, true, {
                if (bands.isNotEmpty()) {
                    FormatText(
                        R.string.bands_format,
                        bands.joinToString(", ")
                    )
                }
            }) {
                override fun cast(info: Any): CellIdentityWrapper? {
                    return info.castGeneric()
                }
            }
            object Channel : IdentityKeys<CellIdentityWrapper>(R.string.channel_format, false, {
                channelNumber.onAvail {
                    FormatText(R.string.channel_format, it.toString())
                }
            }) {
                override fun cast(info: Any): CellIdentityWrapper? {
                    return info.castGeneric()
                }
            }
            object GCI : IdentityKeys<CellIdentityWrapper>(R.string.gci_format, false, {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    globalCellId?.apply {
                        FormatText(R.string.gci_format, this)
                    }
                }
            }) {
                override fun cast(info: Any): CellIdentityWrapper? {
                    return info.castGeneric()
                }
            }
            object Carrier : IdentityKeys<CellIdentityWrapper>(R.string.operator_format, true, {
                if (!alphaLong.isNullOrBlank() || !alphaShort.isNullOrBlank()) {
                    FormatText(
                        R.string.operator_format,
                        setOf(
                            alphaLong,
                            alphaShort
                        ).joinToString("/")
                    )
                }
            }) {
                override fun cast(info: Any): CellIdentityWrapper? {
                    return info.castGeneric()
                }
            }
            object PLMN : IdentityKeys<CellIdentityWrapper>(R.string.plmn_format, true, {
                mcc?.apply {
                    FormatText(R.string.plmn_format, "${mcc}-${mnc}")
                }
            }) {
                override fun cast(info: Any): CellIdentityWrapper? {
                    return info.castGeneric()
                }
            }

            sealed class CDMAKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellIdentityCdmaWrapper.() -> Unit) : IdentityKeys<CellIdentityCdmaWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellIdentityCdmaWrapper? {
                    return info.castGeneric()
                }

                object LatLon : CDMAKeys(R.string.lat_lon_format, false, {
                    longitude.onAvail {
                        FormatText(
                            R.string.lat_lon_format,
                            "${latitude}/${longitude}"
                        )
                    }
                })
                object CDMANetID : CDMAKeys(R.string.cdma_network_id_format, false, {
                    networkId.onAvail {
                        FormatText(R.string.cdma_network_id_format, it.toString())
                    }
                })
                object BasestationID : CDMAKeys(R.string.basestation_id_format, false, {
                    basestationId.onAvail {
                        FormatText(R.string.basestation_id_format, it.toString())
                    }
                })
                object CDMASysID : CDMAKeys(R.string.cdma_system_id_format, false, {
                    systemId.onAvail {
                        FormatText(R.string.cdma_system_id_format, it.toString())
                    }
                })
            }

            sealed class GSMKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellIdentityGsmWrapper.() -> Unit) : IdentityKeys<CellIdentityGsmWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellIdentityGsmWrapper? {
                    return info.castGeneric()
                }

                object LAC : GSMKeys(R.string.lac_format, false, {
                    lac.onAvail {
                        FormatText(R.string.lac_format, "$it")
                    }
                })
                object CID : GSMKeys(R.string.cid_format, false, {
                    cid.onAvail {
                        FormatText(R.string.cid_format, "$it")
                    }
                })
                object BSIC : GSMKeys(R.string.bsic_format, false, {
                    bsic.onAvail {
                        FormatText(R.string.bsic_format, "$bsic")
                    }
                })
                object ARFCN : GSMKeys(R.string.arfcn_format, false, {
                    arfcn.onAvail {
                        FormatText(R.string.arfcn_format, "$arfcn")
                    }
                })
                object DLFreqs : GSMKeys(R.string.dl_freqs_format, false, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(arfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    if (dlFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.dl_freqs_format,
                            dlFreqs.joinToString(", ")
                        )
                    }
                })
                object ULFreqs : GSMKeys(R.string.ul_freqs_format, false, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(arfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    if (ulFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.ul_freqs_format,
                            ulFreqs.joinToString(", ")
                        )
                    }
                })
                object AdditionalPLMNs : GSMKeys(R.string.additional_plmns_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
            }

            sealed class LTEKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellIdentityLteWrapper.() -> Unit) : IdentityKeys<CellIdentityLteWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellIdentityLteWrapper? {
                    return info.castGeneric()
                }

                object Bandwidth : LTEKeys(R.string.bandwidth_format, true, {
                    bandwidth.onAvail {
                        FormatText(R.string.bandwidth_format, it.toString())
                    }
                })
                object TAC : LTEKeys(R.string.tac_format, false, {
                    tac.onAvail {
                        FormatText(R.string.tac_format, it.toString())
                    }
                })
                object CI : LTEKeys(R.string.ci_format, false, {
                    ci.onAvail {
                        FormatText(R.string.ci_format, it.toString())
                    }
                })
                object PCI : LTEKeys(R.string.pci_format, false, {
                    pci.onAvail {
                        FormatText(R.string.pci_format, it.toString())
                    }
                })
                object EARFCN : LTEKeys(R.string.earfcn_format, false, {
                    earfcn.onAvail {
                        FormatText(R.string.earfcn_format, it.toString())
                    }
                })
                object DLFreqs : LTEKeys(R.string.dl_freqs_format, false, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(earfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    if (dlFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.dl_freqs_format,
                            dlFreqs.joinToString(", ")
                        )
                    }
                })
                object ULFreqs : LTEKeys(R.string.ul_freqs_format, false, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(earfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    if (ulFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.ul_freqs_format,
                            ulFreqs.joinToString(", ")
                        )
                    }
                })
                object AdditionalPLMNs : LTEKeys(R.string.additional_plmns_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
                object CSGID : LTEKeys(R.string.csg_id_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    }
                })
                object CSGIndicator : LTEKeys(R.string.csg_indicator_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    }
                })
                object HomeNodeBName : LTEKeys(R.string.home_node_b_name_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    }
                })
            }

            sealed class NRKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellIdentityNrWrapper.() -> Unit) : IdentityKeys<CellIdentityNrWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellIdentityNrWrapper? {
                    return info.castGeneric()
                }

                object TAC : NRKeys(R.string.tac_format, false, {
                    tac.onAvail {
                        FormatText(R.string.tac_format, it.toString())
                    }
                })
                object NCI : NRKeys(R.string.nci_format, false, {
                    nci.onAvail {
                        FormatText(R.string.nci_format, it.toString())
                    }
                })
                object PCI : NRKeys(R.string.pci_format, false, {
                    pci.onAvail {
                        FormatText(
                            textId = R.string.pci_format,
                            it.toString()
                        )
                    }
                })
                object NRARFCN : NRKeys(R.string.nrarfcn_format, false, {
                    nrArfcn.onAvail {
                        FormatText(
                            textId = R.string.nrarfcn_format,
                            it.toString()
                        )
                    }
                })
                object DLFreqs : NRKeys(R.string.dl_freqs_format, false, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(nrArfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    if (dlFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.dl_freqs_format,
                            dlFreqs.joinToString(", ")
                        )
                    }
                })
                object ULFreqs : NRKeys(R.string.ul_freqs_format, false, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(nrArfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    if (ulFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.ul_freqs_format,
                            ulFreqs.joinToString(", ")
                        )
                    }
                })
                object AdditionalPLMNs : NRKeys(R.string.additional_plmns_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                textId = R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
            }

            sealed class TSCDMAKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellIdentityTdscdmaWrapper.() -> Unit) : IdentityKeys<CellIdentityTdscdmaWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellIdentityTdscdmaWrapper? {
                    return info.castGeneric()
                }

                object LAC : TSCDMAKeys(R.string.lac_format, false, {
                    lac.onAvail {
                        FormatText(R.string.lac_format, it.toString())
                    }
                })
                object CID : TSCDMAKeys(R.string.cid_format, false, {
                    cid.onAvail {
                        FormatText(R.string.cid_format, it.toString())
                    }
                })
                object CPID : TSCDMAKeys(R.string.cpid_format, false, {
                    cpid.onAvail {
                        FormatText(R.string.cpid_format, it.toString())
                    }
                })
                object UARFCN : TSCDMAKeys(R.string.uarfcn_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        uarfcn.onAvail {
                            FormatText(R.string.uarfcn_format, it.toString())
                        }
                    }
                })
                object Freqs : TSCDMAKeys(R.string.freqs_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val freqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                            arfcnInfo.map { it.dlFreq }
                        }

                        if (freqs.isNotEmpty()) {
                            FormatText(
                                textId = R.string.freqs_format,
                                freqs.joinToString(", ")
                            )
                        }
                    }
                })
                object AdditionalPLMNs : TSCDMAKeys(R.string.additional_plmns_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
                object CSGID : TSCDMAKeys(R.string.csg_id_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    }
                })
                object CSGIndicator : TSCDMAKeys(R.string.csg_indicator_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    }
                })
                object HomeNodeBName : TSCDMAKeys(R.string.home_node_b_name_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    }
                })
            }

            sealed class WCDMAKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellIdentityWcdmaWrapper.() -> Unit) : IdentityKeys<CellIdentityWcdmaWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellIdentityWcdmaWrapper? {
                    return info.castGeneric()
                }

                object LAC : WCDMAKeys(R.string.lac_format, false, {
                    lac.onAvail {
                        FormatText(R.string.lac_format, it.toString())
                    }
                })
                object CID : WCDMAKeys(R.string.cid_format, false, {
                    cid.onAvail {
                        FormatText(R.string.cid_format, it.toString())
                    }
                })
                object UARFCN : WCDMAKeys(R.string.uarfcn_format, false, {
                    uarfcn.onAvail {
                        FormatText(R.string.uarfcn_format, it.toString())
                    }
                })
                object DLFreqs : WCDMAKeys(R.string.dl_freqs_format, false, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    if (dlFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.dl_freqs_format,
                            dlFreqs.joinToString(", ")
                        )
                    }
                })
                object ULFreqs : WCDMAKeys(R.string.ul_freqs_format, false, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    if (ulFreqs.isNotEmpty()) {
                        FormatText(
                            textId = R.string.ul_freqs_format,
                            ulFreqs.joinToString(", ")
                        )
                    }
                })
                object AdditionalPLMNs : WCDMAKeys(R.string.additional_plmns_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
                object CSGID : WCDMAKeys(R.string.csg_id_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    }
                })
                object CSGIndicator : WCDMAKeys(R.string.csg_indicator_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    }
                })
                object HomeNodeBName : WCDMAKeys(R.string.home_node_b_name_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    }
                })
            }
        }

        sealed class StrengthKeys<T>(@StringRes label: Int, defaultShown: Boolean, render: @Composable T.() -> Unit) : Keys<T>(label, defaultShown, render) {
            object ASU : StrengthKeys<CellSignalStrengthWrapper>(R.string.asu_format, false, {
                FormatText(R.string.asu_format, "$asuLevel")
            }) {
                override fun cast(info: Any): CellSignalStrengthWrapper? {
                    return info.castGeneric()
                }
            }
            object Valid : StrengthKeys<CellSignalStrengthWrapper>(R.string.valid_format, false, {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FormatText(R.string.valid_format, "$valid")
                }
            }) {
                override fun cast(info: Any): CellSignalStrengthWrapper? {
                    return info.castGeneric()
                }
            }

            sealed class CDMAKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellSignalStrengthCdmaWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthCdmaWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellSignalStrengthCdmaWrapper? {
                    return info.castGeneric()
                }

                object CDMAdBm : CDMAKeys(R.string.cdma_dbm_format, false, {
                    cdmaDbm.onAvail {
                        FormatText(R.string.cdma_dbm_format, "$cdmaDbm")
                    }
                })
                object EvDOdBm : CDMAKeys(R.string.evdo_dbm_format, false, {
                    evdoDbm.onAvail {
                        FormatText(R.string.evdo_dbm_format, "$evdoDbm")
                    }
                })
                object CDMAEcIo : CDMAKeys(R.string.cdma_ecio_format, false, {
                    cdmaEcio.onAvail {
                        FormatText(R.string.cdma_ecio_format, "$cdmaEcio")
                    }
                })
                object EvDOEcIo : CDMAKeys(R.string.evdo_ecio_format, false, {
                    evdoEcio.onAvail {
                        FormatText(R.string.evdo_ecio_format, "$evdoEcio")
                    }
                })
                object SnR : CDMAKeys(R.string.snr_format, false, {
                    evdoSnr.onAvail {
                        FormatText(R.string.snr_format, "$evdoSnr")
                    }
                })
                object EvDOASU : CDMAKeys(R.string.evdo_asu_format, false, {
                    evdoAsuLevel.onAvail {
                        FormatText(R.string.evdo_asu_format, "$it")
                    }
                })
            }

            sealed class GSMKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellSignalStrengthGsmWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthGsmWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellSignalStrengthGsmWrapper? {
                    return info.castGeneric()
                }

                object RSSI : GSMKeys(R.string.rssi_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi.onAvail {
                            FormatText(R.string.rssi_format, "$rssi")
                        }
                    }
                })
                object BitErrorRate : GSMKeys(R.string.bit_error_rate_format, false, {
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$it")
                    }
                })
                object TimingAdvance : GSMKeys(R.string.timing_advance_format, false, {
                    timingAdvance.onAvail {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    }
                })
            }

            sealed class LTEKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellSignalStrengthLteWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthLteWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellSignalStrengthLteWrapper? {
                    return info.castGeneric()
                }

                object RSRQ : LTEKeys(R.string.rsrq_format, true, {
                    FormatText(R.string.rsrq_format, "$rsrq")
                })
                object RSSI : LTEKeys(R.string.rssi_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi.onAvail {
                            FormatText(R.string.rssi_format, "$rssi")
                        }
                    }
                })
                object CQI : LTEKeys(R.string.cqi_format, false, {
                    cqi.onAvail {
                        FormatText(R.string.cqi_format, "$cqi")
                    }
                })
                object CQIIndex : LTEKeys(R.string.cqi_table_index_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        cqiTableIndex.onAvail {
                            FormatText(R.string.cqi_table_index_format, "$cqiTableIndex")
                        }
                    }
                })
                object RSSnR : LTEKeys(R.string.rssnr_format, false, {
                    rssnr.onAvail {
                        FormatText(R.string.rssnr_format, "$rssnr")
                    }
                })
                object TimingAdvance : LTEKeys(R.string.timing_advance_format, false, {
                    timingAdvance.onAvail {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    }
                })
            }

            sealed class NRKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellSignalStrengthNrWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthNrWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellSignalStrengthNrWrapper? {
                    return info.castGeneric()
                }

                object SSRSRQ : NRKeys(R.string.ss_rsrq_format, true, {
                    ssRsrq.onAvail {
                        FormatText(R.string.ss_rsrq_format, it.toString())
                    }
                })
                object CSIRSRQ : NRKeys(R.string.csi_rsrq_format, true, {
                    csiRsrq.onAvail {
                        FormatText(R.string.csi_rsrq_format, it.toString())
                    }
                })
                object CSICQIReport : NRKeys(R.string.csi_cqi_report_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!csiCqiReport.isNullOrEmpty()) {
                            FormatText(R.string.csi_cqi_report_format, csiCqiReport.joinToString(", "))
                        }
                    }
                })
                object CSICQIIndex : NRKeys(R.string.csi_cqi_table_index_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        csiCqiTableIndex.onAvail {
                            FormatText(R.string.csi_cqi_table_index_format, "$csiCqiTableIndex")
                        }
                    }
                })
                object SSSinR : NRKeys(R.string.ss_sinr_format, false, {
                    ssSinr.onAvail {
                        FormatText(R.string.ss_sinr_format, "$ssSinr")
                    }
                })
            }

            sealed class TSCDMAKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellSignalStrengthTdscdmaWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthTdscdmaWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellSignalStrengthTdscdmaWrapper? {
                    return info.castGeneric()
                }

                object RSSI : TSCDMAKeys(R.string.rssi_format, false, {
                    rssi.onAvail {
                        FormatText(R.string.rssi_format, "$rssi")
                    }
                })
                object BitErrorRate : TSCDMAKeys(R.string.bit_error_rate_format, false, {
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                })
                object RSCP : TSCDMAKeys(R.string.rscp_format, false, {
                    rscp.onAvail {
                        FormatText(R.string.rscp_format, "$rscp")
                    }
                })
            }

            sealed class WCDMAKeys(@StringRes label: Int, defaultShown: Boolean, render: @Composable CellSignalStrengthWcdmaWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthWcdmaWrapper>(label, defaultShown, render) {
                override fun cast(info: Any): CellSignalStrengthWcdmaWrapper? {
                    return info.castGeneric()
                }

                object RSSI : WCDMAKeys(R.string.rssi_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi.onAvail {
                            FormatText(R.string.rssi_format, "$rssi")
                        }
                    }
                })
                object BitErrorRate : WCDMAKeys(R.string.bit_error_rate_format, false, {
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                })
                object RSCP : WCDMAKeys(R.string.rscp_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rscp.onAvail {
                            FormatText(R.string.rscp_format, "$rscp")
                        }
                    }
                })
                object EcNo : WCDMAKeys(R.string.ecno_format, false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ecNo.onAvail {
                            FormatText(R.string.ecno_format, "$ecNo")
                        }
                    }
                })
            }
        }
    }
}