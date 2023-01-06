package dev.zwander.cellreader.data.data

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellSignalInfo.Orderer.identityCdmaOrder
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.castGeneric
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.util.onCast
import dev.zwander.cellreader.data.wrappers.CellIdentityCdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityGsmWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityLteWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityNrWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityTdscdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityWcdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellIdentityWrapper
import dev.zwander.cellreader.data.wrappers.CellInfoLteWrapper
import dev.zwander.cellreader.data.wrappers.CellInfoWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthCdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthGsmWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthLteWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthNrWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthTdscdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWcdmaWrapper
import dev.zwander.cellreader.data.wrappers.CellSignalStrengthWrapper

object CellSignalInfo {
    sealed class Keys<T>(@StringRes val label: Int, val key: String, val defaultShown: Boolean, private val render: @Composable T.() -> Unit) {
        abstract fun cast(info: Any?): T?

        @Composable
        fun Render(info: Any?) {
            cast(info)?.let {
                render.invoke(it)
            }
        }

        object AdvancedSeparator : Keys<Unit>(R.string.advanced, "advanced", false, {}) {
            override fun cast(info: Any?): Unit? {
                return null
            }
        }

        sealed class IdentityKeys<T : CellIdentityWrapper>(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable T.() -> Unit) : Keys<T>(label, "identity-$key", defaultShown, render) {
            object Bands : IdentityKeys<CellIdentityWrapper>(R.string.bands_format, "bands", true, {
                if (bands.isNotEmpty()) {
                    FormatText(
                        R.string.bands_format,
                        bands.joinToString(", ")
                    )
                }
            }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }
            object Channel : IdentityKeys<CellIdentityWrapper>(R.string.channel_format, "channel", false, {
                channelNumber.onAvail {
                    FormatText(R.string.channel_format, it.toString())
                }
            }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }
            object GCI : IdentityKeys<CellIdentityWrapper>(R.string.gci_format, "gci", false, {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    globalCellId?.apply {
                        FormatText(R.string.gci_format, this)
                    }
                }
            }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }
            object Carrier : IdentityKeys<CellIdentityWrapper>(R.string.operator_format, "carrier", true, {
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
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }
            object PLMN : IdentityKeys<CellIdentityWrapper>(R.string.plmn_format, "plmn", true, {
                mcc?.apply {
                    FormatText(R.string.plmn_format, "${mcc}-${mnc}")
                }
            }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class CDMAKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellIdentityCdmaWrapper.() -> Unit) : IdentityKeys<CellIdentityCdmaWrapper>(label, "$key-cdma", defaultShown, render) {
                override fun cast(info: Any?): CellIdentityCdmaWrapper? {
                    return info?.castGeneric()
                }

                object LatLon : CDMAKeys(R.string.lat_lon_format, "lat-lon", false, {
                    longitude.onAvail {
                        FormatText(
                            R.string.lat_lon_format,
                            "${latitude}/${longitude}"
                        )
                    }
                })
                object CDMANetID : CDMAKeys(R.string.cdma_network_id_format, "net-id", false, {
                    networkId.onAvail {
                        FormatText(R.string.cdma_network_id_format, it.toString())
                    }
                })
                object BasestationID : CDMAKeys(R.string.basestation_id_format, "basestation-id", false, {
                    basestationId.onAvail {
                        FormatText(R.string.basestation_id_format, it.toString())
                    }
                })
                object CDMASysID : CDMAKeys(R.string.cdma_system_id_format, "sys-id", false, {
                    systemId.onAvail {
                        FormatText(R.string.cdma_system_id_format, it.toString())
                    }
                })
            }

            sealed class GSMKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellIdentityGsmWrapper.() -> Unit) : IdentityKeys<CellIdentityGsmWrapper>(label, "$key-gsm", defaultShown, render) {
                override fun cast(info: Any?): CellIdentityGsmWrapper? {
                    return info?.castGeneric()
                }

                object LAC : GSMKeys(R.string.lac_format, "lac", false, {
                    lac.onAvail {
                        FormatText(R.string.lac_format, "$it")
                    }
                })
                object CID : GSMKeys(R.string.cid_format, "cid", false, {
                    cid.onAvail {
                        FormatText(R.string.cid_format, "$it")
                    }
                })
                object BSIC : GSMKeys(R.string.bsic_format, "bsic", false, {
                    bsic.onAvail {
                        FormatText(R.string.bsic_format, "$bsic")
                    }
                })
                object ARFCN : GSMKeys(R.string.arfcn_format, "arfcn", false, {
                    arfcn.onAvail {
                        FormatText(R.string.arfcn_format, "$arfcn")
                    }
                })
                object DLFreqs : GSMKeys(R.string.dl_freqs_format, "dlfreqs", false, {
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
                object ULFreqs : GSMKeys(R.string.ul_freqs_format, "ulfreqs", false, {
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
                object AdditionalPLMNs : GSMKeys(R.string.additional_plmns_format, "additional-plmns", false, {
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

            sealed class LTEKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellIdentityLteWrapper.() -> Unit) : IdentityKeys<CellIdentityLteWrapper>(label, "$key-lte", defaultShown, render) {
                override fun cast(info: Any?): CellIdentityLteWrapper? {
                    return info?.castGeneric()
                }

                object Bandwidth : LTEKeys(R.string.bandwidth_format, "bandwidth", true, {
                    bandwidth.onAvail {
                        FormatText(R.string.bandwidth_format, it.toString())
                    }
                })
                object TAC : LTEKeys(R.string.tac_format, "tac", false, {
                    tac.onAvail {
                        FormatText(R.string.tac_format, it.toString())
                    }
                })
                object CI : LTEKeys(R.string.ci_format, "ci", false, {
                    ci.onAvail {
                        FormatText(R.string.ci_format, it.toString())
                    }
                })
                object PCI : LTEKeys(R.string.pci_format, "pci", false, {
                    pci.onAvail {
                        FormatText(R.string.pci_format, it.toString())
                    }
                })
                object EARFCN : LTEKeys(R.string.earfcn_format, "earfcn", false, {
                    earfcn.onAvail {
                        FormatText(R.string.earfcn_format, it.toString())
                    }
                })
                object DLFreqs : LTEKeys(R.string.dl_freqs_format, "dlfreqs", false, {
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
                object ULFreqs : LTEKeys(R.string.ul_freqs_format, "ulfreqs", false, {
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
                object AdditionalPLMNs : LTEKeys(R.string.additional_plmns_format, "additional-plmns", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
                object CSGID : LTEKeys(R.string.csg_id_format, "csg-id", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    }
                })
                object CSGIndicator : LTEKeys(R.string.csg_indicator_format, "csg-indicator", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    }
                })
                object HomeNodeBName : LTEKeys(R.string.home_node_b_name_format, "home-node-b-name", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    }
                })
            }

            sealed class NRKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellIdentityNrWrapper.() -> Unit) : IdentityKeys<CellIdentityNrWrapper>(label, "$key-nr", defaultShown, render) {
                override fun cast(info: Any?): CellIdentityNrWrapper? {
                    return info?.castGeneric()
                }

                object TAC : NRKeys(R.string.tac_format, "tac", false, {
                    tac.onAvail {
                        FormatText(R.string.tac_format, it.toString())
                    }
                })
                object NCI : NRKeys(R.string.nci_format, "nci", false, {
                    nci.onAvail {
                        FormatText(R.string.nci_format, it.toString())
                    }
                })
                object PCI : NRKeys(R.string.pci_format, "pci", false, {
                    pci.onAvail {
                        FormatText(
                            textId = R.string.pci_format,
                            it.toString()
                        )
                    }
                })
                object NRARFCN : NRKeys(R.string.nrarfcn_format, "nrarfcn", false, {
                    nrArfcn.onAvail {
                        FormatText(
                            textId = R.string.nrarfcn_format,
                            it.toString()
                        )
                    }
                })
                object DLFreqs : NRKeys(R.string.dl_freqs_format, "dlfreqs", false, {
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
                object ULFreqs : NRKeys(R.string.ul_freqs_format, "ulfreqs", false, {
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
                object AdditionalPLMNs : NRKeys(R.string.additional_plmns_format, "additional-plmns", false, {
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

            sealed class TDSCDMAKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellIdentityTdscdmaWrapper.() -> Unit) : IdentityKeys<CellIdentityTdscdmaWrapper>(label, "$key-tdscdma", defaultShown, render) {
                override fun cast(info: Any?): CellIdentityTdscdmaWrapper? {
                    return info?.castGeneric()
                }

                object LAC : TDSCDMAKeys(R.string.lac_format, "lac", false, {
                    lac.onAvail {
                        FormatText(R.string.lac_format, it.toString())
                    }
                })
                object CID : TDSCDMAKeys(R.string.cid_format, "cid", false, {
                    cid.onAvail {
                        FormatText(R.string.cid_format, it.toString())
                    }
                })
                object CPID : TDSCDMAKeys(R.string.cpid_format, "cpid", false, {
                    cpid.onAvail {
                        FormatText(R.string.cpid_format, it.toString())
                    }
                })
                object UARFCN : TDSCDMAKeys(R.string.uarfcn_format, "uarfcn", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        uarfcn.onAvail {
                            FormatText(R.string.uarfcn_format, it.toString())
                        }
                    }
                })
                object Freqs : TDSCDMAKeys(R.string.freqs_format, "freqs", false, {
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
                object AdditionalPLMNs : TDSCDMAKeys(R.string.additional_plmns_format, "additional-plmns", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
                object CSGID : TDSCDMAKeys(R.string.csg_id_format, "csg-id", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    }
                })
                object CSGIndicator : TDSCDMAKeys(R.string.csg_indicator_format, "csg-indicator", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    }
                })
                object HomeNodeBName : TDSCDMAKeys(R.string.home_node_b_name_format, "home-node-b-name", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    }
                })
            }

            sealed class WCDMAKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellIdentityWcdmaWrapper.() -> Unit) : IdentityKeys<CellIdentityWcdmaWrapper>(label, "$key-wcdma", defaultShown, render) {
                override fun cast(info: Any?): CellIdentityWcdmaWrapper? {
                    return info?.castGeneric()
                }

                object LAC : WCDMAKeys(R.string.lac_format, "lac", false, {
                    lac.onAvail {
                        FormatText(R.string.lac_format, it.toString())
                    }
                })
                object CID : WCDMAKeys(R.string.cid_format, "cid", false, {
                    cid.onAvail {
                        FormatText(R.string.cid_format, it.toString())
                    }
                })
                object UARFCN : WCDMAKeys(R.string.uarfcn_format, "uarfcn", false, {
                    uarfcn.onAvail {
                        FormatText(R.string.uarfcn_format, it.toString())
                    }
                })
                object DLFreqs : WCDMAKeys(R.string.dl_freqs_format, "dlfreqs", false, {
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
                object ULFreqs : WCDMAKeys(R.string.ul_freqs_format, "ulfreqs", false, {
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
                object AdditionalPLMNs : WCDMAKeys(R.string.additional_plmns_format, "additional-plmns", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!additionalPlmns.isNullOrEmpty()) {
                            FormatText(
                                R.string.additional_plmns_format,
                                additionalPlmns.joinToString(", ") { it.asMccMnc }
                            )
                        }
                    }
                })
                object CSGID : WCDMAKeys(R.string.csg_id_format, "csg-id", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    }
                })
                object CSGIndicator : WCDMAKeys(R.string.csg_indicator_format, "csg-indicator", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    }
                })
                object HomeNodeBName : WCDMAKeys(R.string.home_node_b_name_format, "home-node-b-name", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    }
                })
            }
        }

        sealed class StrengthKeys<T>(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable T.() -> Unit) : Keys<T>(label, "$key-strength", defaultShown, render) {
            object ASU : StrengthKeys<CellSignalStrengthWrapper>(R.string.asu_format, "asu", false, {
                FormatText(R.string.asu_format, "$asuLevel")
            }) {
                override fun cast(info: Any?): CellSignalStrengthWrapper? {
                    return info?.castGeneric()
                }
            }
            object Valid : StrengthKeys<CellSignalStrengthWrapper>(R.string.valid_format, "valid", false, {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FormatText(R.string.valid_format, "$valid")
                }
            }) {
                override fun cast(info: Any?): CellSignalStrengthWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class CDMAKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellSignalStrengthCdmaWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthCdmaWrapper>(label, "$key-cdma", defaultShown, render) {
                override fun cast(info: Any?): CellSignalStrengthCdmaWrapper? {
                    return info?.castGeneric()
                }

                object CDMAdBm : CDMAKeys(R.string.cdma_dbm_format, "cdma-dbm", false, {
                    cdmaDbm.onAvail {
                        FormatText(R.string.cdma_dbm_format, "$cdmaDbm")
                    }
                })
                object EvDOdBm : CDMAKeys(R.string.evdo_dbm_format, "evdo-dbm", false, {
                    evdoDbm.onAvail {
                        FormatText(R.string.evdo_dbm_format, "$evdoDbm")
                    }
                })
                object CDMAEcIo : CDMAKeys(R.string.cdma_ecio_format, "cdma-ecio", false, {
                    cdmaEcio.onAvail {
                        FormatText(R.string.cdma_ecio_format, "$cdmaEcio")
                    }
                })
                object EvDOEcIo : CDMAKeys(R.string.evdo_ecio_format, "evdo-ecio", false, {
                    evdoEcio.onAvail {
                        FormatText(R.string.evdo_ecio_format, "$evdoEcio")
                    }
                })
                object SnR : CDMAKeys(R.string.snr_format, "snr", false, {
                    evdoSnr.onAvail {
                        FormatText(R.string.snr_format, "$evdoSnr")
                    }
                })
                object EvDOASU : CDMAKeys(R.string.evdo_asu_format, "evdo-asu", false, {
                    evdoAsuLevel.onAvail {
                        FormatText(R.string.evdo_asu_format, "$it")
                    }
                })
            }

            sealed class GSMKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellSignalStrengthGsmWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthGsmWrapper>(label, "$key-gsm", defaultShown, render) {
                override fun cast(info: Any?): CellSignalStrengthGsmWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : GSMKeys(R.string.rssi_format, "rssi", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi.onAvail {
                            FormatText(R.string.rssi_format, "$rssi")
                        }
                    }
                })
                object BitErrorRate : GSMKeys(R.string.bit_error_rate_format, "ber", false, {
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$it")
                    }
                })
                object TimingAdvance : GSMKeys(R.string.timing_advance_format, "timing-advance", false, {
                    timingAdvance.onAvail {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    }
                })
            }

            sealed class LTEKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellSignalStrengthLteWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthLteWrapper>(label, "$key-lte", defaultShown, render) {
                override fun cast(info: Any?): CellSignalStrengthLteWrapper? {
                    return info?.castGeneric()
                }

                object RSRQ : LTEKeys(R.string.rsrq_format, "rsrq", true, {
                    FormatText(R.string.rsrq_format, "$rsrq")
                })
                object RSSI : LTEKeys(R.string.rssi_format, "rssi", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi.onAvail {
                            FormatText(R.string.rssi_format, "$rssi")
                        }
                    }
                })
                object CQI : LTEKeys(R.string.cqi_format, "cqi", false, {
                    cqi.onAvail {
                        FormatText(R.string.cqi_format, "$cqi")
                    }
                })
                object CQIIndex : LTEKeys(R.string.cqi_table_index_format, "cqi-index", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        cqiTableIndex.onAvail {
                            FormatText(R.string.cqi_table_index_format, "$cqiTableIndex")
                        }
                    }
                })
                object RSSnR : LTEKeys(R.string.rssnr_format, "rssnr", false, {
                    rssnr.onAvail {
                        FormatText(R.string.rssnr_format, "$rssnr")
                    }
                })
                object TimingAdvance : LTEKeys(R.string.timing_advance_format, "timing-advance", false, {
                    timingAdvance.onAvail {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    }
                })
            }

            sealed class NRKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellSignalStrengthNrWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthNrWrapper>(label, "$key-nr", defaultShown, render) {
                override fun cast(info: Any?): CellSignalStrengthNrWrapper? {
                    return info?.castGeneric()
                }

                object SSRSRQ : NRKeys(R.string.ss_rsrq_format, "ss-rsrq", true, {
                    ssRsrq.onAvail {
                        FormatText(R.string.ss_rsrq_format, it.toString())
                    }
                })
                object CSIRSRQ : NRKeys(R.string.csi_rsrq_format, "csi-rsrq", true, {
                    csiRsrq.onAvail {
                        FormatText(R.string.csi_rsrq_format, it.toString())
                    }
                })
                object CSICQIReport : NRKeys(R.string.csi_cqi_report_format, "csi-cqi-report", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!csiCqiReport.isNullOrEmpty()) {
                            FormatText(R.string.csi_cqi_report_format, csiCqiReport.joinToString(", "))
                        }
                    }
                })
                object CSICQIIndex : NRKeys(R.string.csi_cqi_table_index_format, "csi-cqi-index", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        csiCqiTableIndex.onAvail {
                            FormatText(R.string.csi_cqi_table_index_format, "$csiCqiTableIndex")
                        }
                    }
                })
                object SSSinR : NRKeys(R.string.ss_sinr_format, "ss-sinr", false, {
                    ssSinr.onAvail {
                        FormatText(R.string.ss_sinr_format, "$ssSinr")
                    }
                })
            }

            sealed class TDSCDMAKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellSignalStrengthTdscdmaWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthTdscdmaWrapper>(label, "$key-tdscdma", defaultShown, render) {
                override fun cast(info: Any?): CellSignalStrengthTdscdmaWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : TDSCDMAKeys(R.string.rssi_format, "rssi", false, {
                    rssi.onAvail {
                        FormatText(R.string.rssi_format, "$rssi")
                    }
                })
                object BitErrorRate : TDSCDMAKeys(R.string.bit_error_rate_format, "ber", false, {
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                })
                object RSCP : TDSCDMAKeys(R.string.rscp_format, "rscp", false, {
                    rscp.onAvail {
                        FormatText(R.string.rscp_format, "$rscp")
                    }
                })
            }

            sealed class WCDMAKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellSignalStrengthWcdmaWrapper.() -> Unit) : StrengthKeys<CellSignalStrengthWcdmaWrapper>(label, "$key-wcdma", defaultShown, render) {
                override fun cast(info: Any?): CellSignalStrengthWcdmaWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : WCDMAKeys(R.string.rssi_format, "rssi", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rssi.onAvail {
                            FormatText(R.string.rssi_format, "$rssi")
                        }
                    }
                })
                object BitErrorRate : WCDMAKeys(R.string.bit_error_rate_format, "ber", false, {
                    bitErrorRate.onAvail {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    }
                })
                object RSCP : WCDMAKeys(R.string.rscp_format, "rscp", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rscp.onAvail {
                            FormatText(R.string.rscp_format, "$rscp")
                        }
                    }
                })
                object EcNo : WCDMAKeys(R.string.ecno_format, "ecno", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ecNo.onAvail {
                            FormatText(R.string.ecno_format, "$ecNo")
                        }
                    }
                })
            }
        }

        sealed class InfoKeys<T>(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable T.() -> Unit) : Keys<T>(label, "$key-info", defaultShown, render) {
            object Registered : InfoKeys<CellInfoWrapper>(R.string.registered_format, "registered", false, {
                FormatText(R.string.registered_format, isRegistered.toString())
            }) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            object Status : InfoKeys<CellInfoWrapper>(R.string.cell_connection_status_format, "status", false, {
                val context = LocalContext.current

                FormatText(
                    R.string.cell_connection_status_format,
                    dev.zwander.cellreader.data.util.CellUtils.connectionStatusToString(
                        context,
                        connectionStatus
                    )
                )
            }) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            object Timestamp : InfoKeys<CellInfoWrapper>(R.string.timestamp_format, "timestamp", false, {
                FormatText(R.string.timestamp_format, timeStamp)
            }) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class LTEKeys(@StringRes label: Int, key: String, defaultShown: Boolean, render: @Composable CellInfoLteWrapper.() -> Unit) : InfoKeys<CellInfoLteWrapper>(label, "$key-lte", defaultShown, render) {
                override fun cast(info: Any?): CellInfoLteWrapper? {
                    return info?.castGeneric()
                }

                object ENDC : LTEKeys(R.string.endc_available_format, "endc", false, {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        onCast<CellInfoLteWrapper> {
                            FormatText(R.string.endc_available_format, "${cellConfig?.endcAvailable}")
                        }
                    }
                })
            }
        }
    }

    object Renderer {
        @Composable
        fun RenderIdentity(
            identity: CellIdentityWrapper,
            strength: CellSignalStrengthWrapper? = null,
            cellInfo: CellInfoWrapper? = null,
            simple: Boolean,
            advanced: Boolean,
        ) {
            val context = LocalContext.current

            with (Orderer) {
                val order = remember(identity) {
                    identity.orderOf(context)
                }
                
                val (simpleOrder, advancedOrder) = remember(order) {
                    order.splitInfo
                }
                
                if (simple) {
                    simpleOrder.forEach {
                        if (it is Keys.IdentityKeys) {
                            it.Render(info = identity)
                        }

                        if (it is Keys.StrengthKeys) {
                            it.Render(info = strength)
                        }

                        if (it is Keys.InfoKeys) {
                            it.Render(info = cellInfo)
                        }
                    }
                }
                
                if (advanced) {
                    advancedOrder.forEach {
                        if (it is Keys.IdentityKeys) {
                            it.Render(info = identity)
                        }

                        if (it is Keys.StrengthKeys) {
                            it.Render(info = strength)
                        }

                        if (it is Keys.InfoKeys) {
                            it.Render(info = cellInfo)
                        }
                    }
                }
            }
        }
        
        @Composable
        fun RenderStrength(
            strength: CellSignalStrengthWrapper,
            simple: Boolean,
            advanced: Boolean
        ) {
            val context = LocalContext.current

            with (Orderer) {
                val order = remember(strength) {
                    strength.orderOf(context)
                }

                val (simpleOrder, advancedOrder) = remember(order) {
                    order.splitInfo
                }

                if (simple) {
                    simpleOrder.forEach {
                        it.Render(info = strength)
                    }
                }

                if (advanced) {
                    advancedOrder.forEach {
                        it.Render(info = strength)
                    }
                }
            }
        }
    }

    object Orderer {
        private val defaultIdentityCdmaOrder = listOf(
            Keys.IdentityKeys.Carrier,
            Keys.AdvancedSeparator,
            Keys.InfoKeys.Registered,
            Keys.InfoKeys.Status,
            Keys.InfoKeys.Timestamp,
            Keys.IdentityKeys.CDMAKeys.LatLon,
            Keys.IdentityKeys.CDMAKeys.CDMANetID,
            Keys.IdentityKeys.CDMAKeys.BasestationID,
            Keys.IdentityKeys.CDMAKeys.CDMASysID,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.StrengthKeys.CDMAKeys.CDMAdBm,
            Keys.StrengthKeys.CDMAKeys.EvDOdBm,
            Keys.StrengthKeys.CDMAKeys.CDMAEcIo,
            Keys.StrengthKeys.CDMAKeys.EvDOEcIo,
            Keys.StrengthKeys.CDMAKeys.SnR,
            Keys.StrengthKeys.CDMAKeys.EvDOASU
        )

        private val defaultStrengthCdmaOrder = listOf(
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.StrengthKeys.CDMAKeys.CDMAdBm,
            Keys.StrengthKeys.CDMAKeys.EvDOdBm,
            Keys.StrengthKeys.CDMAKeys.CDMAEcIo,
            Keys.StrengthKeys.CDMAKeys.EvDOEcIo,
            Keys.StrengthKeys.CDMAKeys.SnR,
            Keys.StrengthKeys.CDMAKeys.EvDOASU,
            Keys.AdvancedSeparator,
        )

        private val defaultIdentityGsmOrder = listOf(
            Keys.IdentityKeys.Bands,
            Keys.IdentityKeys.Carrier,
            Keys.IdentityKeys.PLMN,
            Keys.AdvancedSeparator,
            Keys.IdentityKeys.Channel,
            Keys.IdentityKeys.GCI,
            Keys.IdentityKeys.GSMKeys.LAC,
            Keys.IdentityKeys.GSMKeys.CID,
            Keys.IdentityKeys.GSMKeys.BSIC,
            Keys.IdentityKeys.GSMKeys.ARFCN,
            Keys.IdentityKeys.GSMKeys.DLFreqs,
            Keys.IdentityKeys.GSMKeys.ULFreqs,
            Keys.IdentityKeys.GSMKeys.AdditionalPLMNs,
            Keys.StrengthKeys.GSMKeys.RSSI,
            Keys.StrengthKeys.GSMKeys.BitErrorRate,
            Keys.StrengthKeys.GSMKeys.TimingAdvance,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid
        )

        private val defaultStrengthGsmOrder = listOf(
            Keys.StrengthKeys.GSMKeys.RSSI,
            Keys.StrengthKeys.GSMKeys.BitErrorRate,
            Keys.StrengthKeys.GSMKeys.TimingAdvance,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.AdvancedSeparator
        )

        private val defaultIdentityLteOrder = listOf(
            Keys.IdentityKeys.Bands,
            Keys.IdentityKeys.LTEKeys.Bandwidth,
            Keys.IdentityKeys.Carrier,
            Keys.IdentityKeys.PLMN,
            Keys.StrengthKeys.LTEKeys.RSRQ,
            Keys.AdvancedSeparator,
            Keys.InfoKeys.Registered,
            Keys.InfoKeys.Status,
            Keys.InfoKeys.Timestamp,
            Keys.InfoKeys.LTEKeys.ENDC,
            Keys.IdentityKeys.Channel,
            Keys.IdentityKeys.GCI,
            Keys.IdentityKeys.LTEKeys.TAC,
            Keys.IdentityKeys.LTEKeys.CI,
            Keys.IdentityKeys.LTEKeys.PCI,
            Keys.IdentityKeys.LTEKeys.EARFCN,
            Keys.IdentityKeys.LTEKeys.DLFreqs,
            Keys.IdentityKeys.LTEKeys.ULFreqs,
            Keys.IdentityKeys.LTEKeys.AdditionalPLMNs,
            Keys.IdentityKeys.LTEKeys.CSGID,
            Keys.IdentityKeys.LTEKeys.CSGIndicator,
            Keys.IdentityKeys.LTEKeys.HomeNodeBName,
            Keys.StrengthKeys.LTEKeys.RSSI,
            Keys.StrengthKeys.LTEKeys.CQI,
            Keys.StrengthKeys.LTEKeys.CQIIndex,
            Keys.StrengthKeys.LTEKeys.RSSnR,
            Keys.StrengthKeys.LTEKeys.TimingAdvance,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid
        )

        private val defaultStrengthLteOrder = listOf(
            Keys.StrengthKeys.LTEKeys.RSRQ,
            Keys.StrengthKeys.LTEKeys.CQI,
            Keys.StrengthKeys.LTEKeys.CQIIndex,
            Keys.StrengthKeys.LTEKeys.RSSnR,
            Keys.StrengthKeys.LTEKeys.TimingAdvance,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.AdvancedSeparator
        )

        private val defaultIdentityNrOrder = listOf(
            Keys.IdentityKeys.Bands,
            Keys.StrengthKeys.NRKeys.SSRSRQ,
            Keys.StrengthKeys.NRKeys.CSIRSRQ,
            Keys.AdvancedSeparator,
            Keys.InfoKeys.Registered,
            Keys.InfoKeys.Status,
            Keys.InfoKeys.Timestamp,
            Keys.IdentityKeys.Channel,
            Keys.IdentityKeys.GCI,
            Keys.IdentityKeys.NRKeys.TAC,
            Keys.IdentityKeys.NRKeys.NCI,
            Keys.IdentityKeys.NRKeys.PCI,
            Keys.IdentityKeys.NRKeys.NRARFCN,
            Keys.IdentityKeys.NRKeys.DLFreqs,
            Keys.IdentityKeys.NRKeys.ULFreqs,
            Keys.IdentityKeys.NRKeys.AdditionalPLMNs,
            Keys.IdentityKeys.Carrier,
            Keys.IdentityKeys.PLMN,
            Keys.StrengthKeys.NRKeys.CSICQIReport,
            Keys.StrengthKeys.NRKeys.CSICQIIndex,
            Keys.StrengthKeys.NRKeys.SSSinR,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid
        )

        private val defaultStrengthNrOrder = listOf(
            Keys.StrengthKeys.NRKeys.SSRSRQ,
            Keys.StrengthKeys.NRKeys.CSIRSRQ,
            Keys.StrengthKeys.NRKeys.CSICQIReport,
            Keys.StrengthKeys.NRKeys.CSICQIIndex,
            Keys.StrengthKeys.NRKeys.SSSinR,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.AdvancedSeparator
        )

        private val defaultIdentityTdscdmaOrder = listOf(
            Keys.IdentityKeys.Bands,
            Keys.IdentityKeys.Carrier,
            Keys.IdentityKeys.PLMN,
            Keys.AdvancedSeparator,
            Keys.InfoKeys.Registered,
            Keys.InfoKeys.Status,
            Keys.InfoKeys.Timestamp,
            Keys.IdentityKeys.Channel,
            Keys.IdentityKeys.GCI,
            Keys.IdentityKeys.TDSCDMAKeys.LAC,
            Keys.IdentityKeys.TDSCDMAKeys.CID,
            Keys.IdentityKeys.TDSCDMAKeys.CPID,
            Keys.IdentityKeys.TDSCDMAKeys.UARFCN,
            Keys.IdentityKeys.TDSCDMAKeys.Freqs,
            Keys.IdentityKeys.TDSCDMAKeys.AdditionalPLMNs,
            Keys.IdentityKeys.TDSCDMAKeys.CSGID,
            Keys.IdentityKeys.TDSCDMAKeys.CSGIndicator,
            Keys.IdentityKeys.TDSCDMAKeys.HomeNodeBName,
            Keys.StrengthKeys.TDSCDMAKeys.RSSI,
            Keys.StrengthKeys.TDSCDMAKeys.BitErrorRate,
            Keys.StrengthKeys.TDSCDMAKeys.RSCP,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid
        )

        private val defaultStrengthTdscdmaOrder = listOf(
            Keys.StrengthKeys.TDSCDMAKeys.RSSI,
            Keys.StrengthKeys.TDSCDMAKeys.BitErrorRate,
            Keys.StrengthKeys.TDSCDMAKeys.RSCP,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.AdvancedSeparator
        )

        private val defaultIdentityWcdmaOrder = listOf(
            Keys.IdentityKeys.Bands,
            Keys.IdentityKeys.Carrier,
            Keys.IdentityKeys.PLMN,
            Keys.AdvancedSeparator,
            Keys.InfoKeys.Registered,
            Keys.InfoKeys.Status,
            Keys.InfoKeys.Timestamp,
            Keys.IdentityKeys.Channel,
            Keys.IdentityKeys.GCI,
            Keys.IdentityKeys.WCDMAKeys.LAC,
            Keys.IdentityKeys.WCDMAKeys.CID,
            Keys.IdentityKeys.WCDMAKeys.UARFCN,
            Keys.IdentityKeys.WCDMAKeys.DLFreqs,
            Keys.IdentityKeys.WCDMAKeys.ULFreqs,
            Keys.IdentityKeys.WCDMAKeys.AdditionalPLMNs,
            Keys.IdentityKeys.WCDMAKeys.CSGID,
            Keys.IdentityKeys.WCDMAKeys.CSGIndicator,
            Keys.IdentityKeys.WCDMAKeys.HomeNodeBName,
            Keys.StrengthKeys.WCDMAKeys.RSSI,
            Keys.StrengthKeys.WCDMAKeys.BitErrorRate,
            Keys.StrengthKeys.WCDMAKeys.RSCP,
            Keys.StrengthKeys.WCDMAKeys.EcNo,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid
        )

        private val defaultStrengthWcdmaOrder = listOf(
            Keys.StrengthKeys.WCDMAKeys.RSSI,
            Keys.StrengthKeys.WCDMAKeys.BitErrorRate,
            Keys.StrengthKeys.WCDMAKeys.RSCP,
            Keys.StrengthKeys.WCDMAKeys.EcNo,
            Keys.StrengthKeys.ASU,
            Keys.StrengthKeys.Valid,
            Keys.AdvancedSeparator
        )

        val Context.identityCdmaOrder: List<Keys<*>>
            get() = defaultIdentityCdmaOrder

        val Context.strengthCdmaOrder: List<Keys<*>>
            get() = defaultStrengthCdmaOrder

        val Context.identityGsmOrder: List<Keys<*>>
            get() = defaultIdentityGsmOrder

        val Context.strengthGsmOrder: List<Keys<*>>
            get() = defaultStrengthGsmOrder

        val Context.identityLteOrder: List<Keys<*>>
            get() = defaultIdentityLteOrder

        val Context.strengthLteOrder: List<Keys<*>>
            get() = defaultStrengthLteOrder

        val Context.identityNrOrder: List<Keys<*>>
            get() = defaultIdentityNrOrder

        val Context.strengthNrOrder: List<Keys<*>>
            get() = defaultStrengthNrOrder

        val Context.identityTdscdmaOrder: List<Keys<*>>
            get() = defaultIdentityTdscdmaOrder

        val Context.strengthTdscdmaOrder: List<Keys<*>>
            get() = defaultStrengthTdscdmaOrder

        val Context.identityWcdmaOrder: List<Keys<*>>
            get() = defaultIdentityWcdmaOrder
        
        val Context.strengthWcdmaOrder: List<Keys<*>>
            get() = defaultStrengthWcdmaOrder

        val List<Keys<*>>.advancedIndex: Int
            get() = indexOf(Keys.AdvancedSeparator)

        val List<Keys<*>>.hasAdvancedItems: Boolean
            get() = advancedIndex < lastIndex

        val List<Keys<*>>.splitInfo: Pair<List<Keys<*>>, List<Keys<*>>>
            get() {
                val advancedIndex = advancedIndex

                if (advancedIndex >= lastIndex) {
                    return this to listOf()
                }

                val simple = slice(0 until advancedIndex)
                val advanced = slice(advancedIndex + 1..lastIndex)

                return simple to advanced
            }

        fun CellIdentityWrapper.orderOf(context: Context): List<Keys<*>> =
            when (this) {
                is CellIdentityCdmaWrapper -> context.identityCdmaOrder
                is CellIdentityGsmWrapper -> context.identityGsmOrder
                is CellIdentityLteWrapper -> context.identityLteOrder
                is CellIdentityNrWrapper -> context.identityNrOrder
                is CellIdentityTdscdmaWrapper -> context.identityTdscdmaOrder
                is CellIdentityWcdmaWrapper -> context.identityWcdmaOrder
            }

        fun CellSignalStrengthWrapper.orderOf(context: Context): List<Keys<*>> =
            when (this) {
                is CellSignalStrengthCdmaWrapper -> context.strengthCdmaOrder
                is CellSignalStrengthGsmWrapper -> context.strengthGsmOrder
                is CellSignalStrengthLteWrapper -> context.strengthLteOrder
                is CellSignalStrengthNrWrapper -> context.strengthNrOrder
                is CellSignalStrengthTdscdmaWrapper -> context.strengthTdscdmaOrder
                is CellSignalStrengthWcdmaWrapper -> context.strengthWcdmaOrder
            }
    }
}