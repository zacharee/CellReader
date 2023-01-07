package dev.zwander.cellreader.data.data

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.asMccMnc
import dev.zwander.cellreader.data.util.avail
import dev.zwander.cellreader.data.util.castGeneric
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

object CellSignalInfo {
    sealed class Keys<T>(
        @StringRes val label: Int,
        val key: String,
        private val canRender: T.() -> Boolean,
        private val render: @Composable T.() -> Unit
    ) {
        companion object {
            private val objectsCache = mutableListOf<Keys<*>>()

            fun getAllObjects(): List<Keys<*>> {
                return if (objectsCache.isNotEmpty()) {
                    objectsCache
                } else {
                    findSealedObjects(Keys::class).apply {
                        objectsCache.addAll(this)
                    }
                }
            }

            private fun findSealedObjects(parent: KClass<out Keys<*>>): List<Keys<*>> {
                val subclasses = parent.sealedSubclasses
                val objects = mutableListOf<Keys<*>>()

                subclasses.forEach { subclass ->
                    val objectInstance = subclass.objectInstance

                    if (objectInstance != null) {
                        objects.add(objectInstance)
                    } else {
                        objects.addAll(findSealedObjects(subclass))
                    }
                }

                return objects
            }
        }

        abstract fun cast(info: Any?): T?

        @Composable
        fun Render(info: Any?) {
            if (canRender(info)) {
                cast(info)?.let {
                    render.invoke(it)
                }
            }
        }

        fun canRender(info: Any?) = cast(info)?.let { it.canRender() } ?: false

        object AdvancedSeparator : Keys<Unit>(R.string.advanced, "advanced", { false }, {}) {
            override fun cast(info: Any?): Unit? {
                return null
            }
        }

        sealed class IdentityKeys<T : CellIdentityWrapper>(
            @StringRes label: Int,
            key: String,
            canRender: T.() -> Boolean,
            render: @Composable T.() -> Unit
        ) : Keys<T>(label, "identity-$key", canRender, render) {
            object Bands : IdentityKeys<CellIdentityWrapper>(
                R.string.bands_format,
                "bands",
                { bands.isNotEmpty() },
                {
                    FormatText(
                        R.string.bands_format,
                        bands.joinToString(", ")
                    )
                }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object Channel : IdentityKeys<CellIdentityWrapper>(
                R.string.channel_format,
                "channel",
                { channelNumber.avail() },
                {
                    FormatText(R.string.channel_format, channelNumber.toString())
                }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object GCI : IdentityKeys<CellIdentityWrapper>(
                R.string.gci_format,
                "gci",
                { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && globalCellId != null },
                {
                    globalCellId?.apply {
                        FormatText(R.string.gci_format, this)
                    }
                }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object Carrier : IdentityKeys<CellIdentityWrapper>(
                R.string.operator_format,
                "carrier",
                { !alphaLong.isNullOrBlank() || !alphaShort.isNullOrBlank() },
                {
                    FormatText(
                        R.string.operator_format,
                        setOf(
                            alphaLong,
                            alphaShort
                        ).joinToString("/")
                    )
                }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object PLMN : IdentityKeys<CellIdentityWrapper>(
                R.string.plmn_format,
                "plmn",
                { mcc != null && mnc != null },
                {
                    mcc?.apply {
                        FormatText(R.string.plmn_format, "${mcc}-${mnc}")
                    }
                }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class CDMAKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellIdentityCdmaWrapper.() -> Boolean,
                render: @Composable CellIdentityCdmaWrapper.() -> Unit
            ) : IdentityKeys<CellIdentityCdmaWrapper>(label, "$key-cdma", canRender, render) {
                override fun cast(info: Any?): CellIdentityCdmaWrapper? {
                    return info?.castGeneric()
                }

                object LatLon : CDMAKeys(
                    R.string.lat_lon_format,
                    "lat-lon",
                    { latitude.avail() && longitude.avail() },
                    {
                        FormatText(
                            R.string.lat_lon_format,
                            "${latitude}/${longitude}"
                        )
                    })

                object CDMANetID :
                    CDMAKeys(R.string.cdma_network_id_format, "net-id", { networkId.avail() }, {
                        FormatText(R.string.cdma_network_id_format, networkId.toString())
                    })

                object BasestationID : CDMAKeys(
                    R.string.basestation_id_format,
                    "basestation-id",
                    { basestationId.avail() },
                    {
                        FormatText(R.string.basestation_id_format, basestationId.toString())
                    })

                object CDMASysID :
                    CDMAKeys(R.string.cdma_system_id_format, "sys-id", { systemId.avail() }, {
                        FormatText(R.string.cdma_system_id_format, systemId.toString())
                    })
            }

            sealed class GSMKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellIdentityGsmWrapper.() -> Boolean,
                render: @Composable CellIdentityGsmWrapper.() -> Unit
            ) : IdentityKeys<CellIdentityGsmWrapper>(label, "$key-gsm", canRender, render) {
                override fun cast(info: Any?): CellIdentityGsmWrapper? {
                    return info?.castGeneric()
                }

                object LAC : GSMKeys(R.string.lac_format, "lac", { lac.avail() }, {
                    FormatText(R.string.lac_format, "$lac")
                })

                object CID : GSMKeys(R.string.cid_format, "cid", { cid.avail() }, {
                    FormatText(R.string.cid_format, "$cid")
                })

                object BSIC : GSMKeys(R.string.bsic_format, "bsic", { bsic.avail() }, {
                    FormatText(R.string.bsic_format, "$bsic")
                })

                object ARFCN : GSMKeys(R.string.arfcn_format, "arfcn", { arfcn.avail() }, {
                    FormatText(R.string.arfcn_format, "$arfcn")
                })

                object DLFreqs : GSMKeys(R.string.dl_freqs_format, "dlfreqs", { arfcn.avail() }, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(arfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    FormatText(
                        textId = R.string.dl_freqs_format,
                        dlFreqs.joinToString(", ")
                    )
                })

                object ULFreqs : GSMKeys(R.string.ul_freqs_format, "ulfreqs", { arfcn.avail() }, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(arfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    FormatText(
                        textId = R.string.ul_freqs_format,
                        ulFreqs.joinToString(", ")
                    )
                })

                object AdditionalPLMNs : GSMKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns?.joinToString(", ") { it.asMccMnc }
                        )
                    })
            }

            sealed class LTEKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellIdentityLteWrapper.() -> Boolean,
                render: @Composable CellIdentityLteWrapper.() -> Unit
            ) : IdentityKeys<CellIdentityLteWrapper>(label, "$key-lte", canRender, render) {
                override fun cast(info: Any?): CellIdentityLteWrapper? {
                    return info?.castGeneric()
                }

                object Bandwidth :
                    LTEKeys(R.string.bandwidth_format, "bandwidth", { bandwidth.avail() }, {
                        FormatText(R.string.bandwidth_format, bandwidth.toString())
                    })

                object TAC : LTEKeys(R.string.tac_format, "tac", { tac.avail() }, {
                    FormatText(R.string.tac_format, tac.toString())
                })

                object CI : LTEKeys(R.string.ci_format, "ci", { ci.avail() }, {
                    FormatText(R.string.ci_format, ci.toString())
                })

                object PCI : LTEKeys(R.string.pci_format, "pci", { pci.avail() }, {
                    FormatText(R.string.pci_format, pci.toString())
                })

                object EARFCN : LTEKeys(R.string.earfcn_format, "earfcn", { earfcn.avail() }, {
                    FormatText(R.string.earfcn_format, earfcn.toString())
                })

                object DLFreqs : LTEKeys(R.string.dl_freqs_format, "dlfreqs", { earfcn.avail() }, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(earfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    FormatText(
                        textId = R.string.dl_freqs_format,
                        dlFreqs.joinToString(", ")
                    )
                })

                object ULFreqs : LTEKeys(R.string.ul_freqs_format, "ulfreqs", { earfcn.avail() }, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(earfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    FormatText(
                        textId = R.string.ul_freqs_format,
                        ulFreqs.joinToString(", ")
                    )
                })

                object AdditionalPLMNs : LTEKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns?.joinToString(", ") { it.asMccMnc }
                        )
                    })

                object CSGID : LTEKeys(
                    R.string.csg_id_format,
                    "csg-id",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    })

                object CSGIndicator : LTEKeys(
                    R.string.csg_indicator_format,
                    "csg-indicator",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    })

                object HomeNodeBName : LTEKeys(
                    R.string.home_node_b_name_format,
                    "home-node-b-name",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    })
            }

            sealed class NRKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellIdentityNrWrapper.() -> Boolean,
                render: @Composable CellIdentityNrWrapper.() -> Unit
            ) : IdentityKeys<CellIdentityNrWrapper>(label, "$key-nr", canRender, render) {
                override fun cast(info: Any?): CellIdentityNrWrapper? {
                    return info?.castGeneric()
                }

                object TAC : NRKeys(R.string.tac_format, "tac", { tac.avail() }, {
                    FormatText(R.string.tac_format, tac.toString())
                })

                object NCI : NRKeys(R.string.nci_format, "nci", { nci.avail() }, {
                    FormatText(R.string.nci_format, nci.toString())
                })

                object PCI : NRKeys(R.string.pci_format, "pci", { pci.avail() }, {
                    FormatText(
                        textId = R.string.pci_format,
                        pci.toString()
                    )
                })

                object NRARFCN : NRKeys(R.string.nrarfcn_format, "nrarfcn", { nrArfcn.avail() }, {
                    FormatText(
                        textId = R.string.nrarfcn_format,
                        nrArfcn.toString()
                    )
                })

                object DLFreqs : NRKeys(R.string.dl_freqs_format, "dlfreqs", { nrArfcn.avail() }, {
                    val dlFreqs = rememberSaveable(inputs = arrayOf(nrArfcn)) {
                        arfcnInfo.map { it.dlFreq }
                    }

                    FormatText(
                        textId = R.string.dl_freqs_format,
                        dlFreqs.joinToString(", ")
                    )
                })

                object ULFreqs : NRKeys(R.string.ul_freqs_format, "ulfreqs", { nrArfcn.avail() }, {
                    val ulFreqs = rememberSaveable(inputs = arrayOf(nrArfcn)) {
                        arfcnInfo.map { it.ulFreq }
                    }

                    FormatText(
                        textId = R.string.ul_freqs_format,
                        ulFreqs.joinToString(", ")
                    )
                })

                object AdditionalPLMNs : NRKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    {
                        FormatText(
                            textId = R.string.additional_plmns_format,
                            additionalPlmns?.joinToString(", ") { it.asMccMnc }
                        )
                    })
            }

            sealed class TDSCDMAKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellIdentityTdscdmaWrapper.() -> Boolean,
                render: @Composable CellIdentityTdscdmaWrapper.() -> Unit
            ) : IdentityKeys<CellIdentityTdscdmaWrapper>(label, "$key-tdscdma", canRender, render) {
                override fun cast(info: Any?): CellIdentityTdscdmaWrapper? {
                    return info?.castGeneric()
                }

                object LAC : TDSCDMAKeys(R.string.lac_format, "lac", { lac.avail() }, {
                    FormatText(R.string.lac_format, lac.toString())
                })

                object CID : TDSCDMAKeys(R.string.cid_format, "cid", { cid.avail() }, {
                    FormatText(R.string.cid_format, cid.toString())
                })

                object CPID : TDSCDMAKeys(R.string.cpid_format, "cpid", { cpid.avail() }, {
                    FormatText(R.string.cpid_format, cpid.toString())
                })

                object UARFCN : TDSCDMAKeys(
                    R.string.uarfcn_format,
                    "uarfcn",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uarfcn.avail() },
                    {
                        FormatText(R.string.uarfcn_format, uarfcn.toString())
                    })

                object Freqs : TDSCDMAKeys(
                    R.string.freqs_format,
                    "freqs",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uarfcn.avail() },
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val freqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                                arfcnInfo.map { it.dlFreq }
                            }

                            FormatText(
                                textId = R.string.freqs_format,
                                freqs.joinToString(", ")
                            )
                        }
                    })

                object AdditionalPLMNs : TDSCDMAKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns?.joinToString(", ") { it.asMccMnc }
                        )
                    })

                object CSGID : TDSCDMAKeys(
                    R.string.csg_id_format,
                    "csg-id",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    })

                object CSGIndicator : TDSCDMAKeys(
                    R.string.csg_indicator_format,
                    "csg-indicator",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    })

                object HomeNodeBName : TDSCDMAKeys(
                    R.string.home_node_b_name_format,
                    "home-node-b-name",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    })
            }

            sealed class WCDMAKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellIdentityWcdmaWrapper.() -> Boolean,
                render: @Composable CellIdentityWcdmaWrapper.() -> Unit
            ) : IdentityKeys<CellIdentityWcdmaWrapper>(label, "$key-wcdma", canRender, render) {
                override fun cast(info: Any?): CellIdentityWcdmaWrapper? {
                    return info?.castGeneric()
                }

                object LAC : WCDMAKeys(R.string.lac_format, "lac", { lac.avail() }, {
                    FormatText(R.string.lac_format, lac.toString())
                })

                object CID : WCDMAKeys(R.string.cid_format, "cid", { cid.avail() }, {
                    FormatText(R.string.cid_format, cid.toString())
                })

                object UARFCN : WCDMAKeys(R.string.uarfcn_format, "uarfcn", { uarfcn.avail() }, {
                    FormatText(R.string.uarfcn_format, uarfcn.toString())
                })

                object DLFreqs :
                    WCDMAKeys(R.string.dl_freqs_format, "dlfreqs", { uarfcn.avail() }, {
                        val dlFreqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                            arfcnInfo.map { it.dlFreq }
                        }

                        FormatText(
                            textId = R.string.dl_freqs_format,
                            dlFreqs.joinToString(", ")
                        )
                    })

                object ULFreqs :
                    WCDMAKeys(R.string.ul_freqs_format, "ulfreqs", { uarfcn.avail() }, {
                        val ulFreqs = rememberSaveable(inputs = arrayOf(uarfcn)) {
                            arfcnInfo.map { it.ulFreq }
                        }

                        FormatText(
                            textId = R.string.ul_freqs_format,
                            ulFreqs.joinToString(", ")
                        )
                    })

                object AdditionalPLMNs : WCDMAKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    {
                        FormatText(
                            R.string.additional_plmns_format,
                            additionalPlmns?.joinToString(", ") { it.asMccMnc }
                        )
                    })

                object CSGID : WCDMAKeys(
                    R.string.csg_id_format,
                    "csg-id",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(R.string.csg_id_format, csgIdentity.toString())
                        }
                    })

                object CSGIndicator : WCDMAKeys(
                    R.string.csg_indicator_format,
                    "csg-indicator",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(
                                R.string.csg_indicator_format,
                                csgIndicator.toString()
                            )
                        }
                    })

                object HomeNodeBName : WCDMAKeys(
                    R.string.home_node_b_name_format,
                    "home-node-b-name",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    {
                        csgInfo?.apply {
                            FormatText(R.string.home_node_b_name_format, homeNodebName ?: "")
                        }
                    })
            }
        }

        sealed class StrengthKeys<T>(
            @StringRes label: Int,
            key: String,
            canRender: T.() -> Boolean,
            render: @Composable T.() -> Unit
        ) : Keys<T>(label, "$key-strength", canRender, render) {
            object ASU :
                StrengthKeys<CellSignalStrengthWrapper>(R.string.asu_format, "asu", { true }, {
                    FormatText(R.string.asu_format, "$asuLevel")
                }) {
                override fun cast(info: Any?): CellSignalStrengthWrapper? {
                    return info?.castGeneric()
                }
            }

            object Valid : StrengthKeys<CellSignalStrengthWrapper>(
                R.string.valid_format,
                "valid",
                { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q },
                {
                    FormatText(R.string.valid_format, "$valid")
                }) {
                override fun cast(info: Any?): CellSignalStrengthWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class CDMAKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellSignalStrengthCdmaWrapper.() -> Boolean,
                render: @Composable CellSignalStrengthCdmaWrapper.() -> Unit
            ) : StrengthKeys<CellSignalStrengthCdmaWrapper>(label, "$key-cdma", canRender, render) {
                override fun cast(info: Any?): CellSignalStrengthCdmaWrapper? {
                    return info?.castGeneric()
                }

                object CDMAdBm :
                    CDMAKeys(R.string.cdma_dbm_format, "cdma-dbm", { cdmaDbm.avail() }, {
                        FormatText(R.string.cdma_dbm_format, "$cdmaDbm")
                    })

                object EvDOdBm :
                    CDMAKeys(R.string.evdo_dbm_format, "evdo-dbm", { evdoDbm.avail() }, {
                        FormatText(R.string.evdo_dbm_format, "$evdoDbm")
                    })

                object CDMAEcIo :
                    CDMAKeys(R.string.cdma_ecio_format, "cdma-ecio", { cdmaEcio.avail() }, {
                        FormatText(R.string.cdma_ecio_format, "$cdmaEcio")
                    })

                object EvDOEcIo :
                    CDMAKeys(R.string.evdo_ecio_format, "evdo-ecio", { evdoEcio.avail() }, {
                        FormatText(R.string.evdo_ecio_format, "$evdoEcio")
                    })

                object SnR : CDMAKeys(R.string.snr_format, "snr", { evdoSnr.avail() }, {
                    FormatText(R.string.snr_format, "$evdoSnr")
                })

                object EvDOASU : CDMAKeys(R.string.evdo_asu_format, "evdo-asu", { true }, {
                    FormatText(R.string.evdo_asu_format, "$evdoAsuLevel")
                })
            }

            sealed class GSMKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellSignalStrengthGsmWrapper.() -> Boolean,
                render: @Composable CellSignalStrengthGsmWrapper.() -> Unit
            ) : StrengthKeys<CellSignalStrengthGsmWrapper>(label, "$key-gsm", canRender, render) {
                override fun cast(info: Any?): CellSignalStrengthGsmWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : GSMKeys(
                    R.string.rssi_format,
                    "rssi",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rssi.avail() },
                    {
                        FormatText(R.string.rssi_format, "$rssi")
                    })

                object BitErrorRate :
                    GSMKeys(R.string.bit_error_rate_format, "ber", { bitErrorRate.avail() }, {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    })

                object TimingAdvance : GSMKeys(R.string.timing_advance_format, "timing-advance",
                    { timingAdvance.avail() }, {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    })
            }

            sealed class LTEKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellSignalStrengthLteWrapper.() -> Boolean,
                render: @Composable CellSignalStrengthLteWrapper.() -> Unit
            ) : StrengthKeys<CellSignalStrengthLteWrapper>(label, "$key-lte", canRender, render) {
                override fun cast(info: Any?): CellSignalStrengthLteWrapper? {
                    return info?.castGeneric()
                }

                object RSRQ : LTEKeys(R.string.rsrq_format, "rsrq", { true }, {
                    FormatText(R.string.rsrq_format, "$rsrq")
                })

                object RSSI : LTEKeys(
                    R.string.rssi_format,
                    "rssi",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rssi.avail() },
                    {
                        FormatText(R.string.rssi_format, "$rssi")
                    })

                object CQI : LTEKeys(R.string.cqi_format, "cqi", { cqi.avail() }, {
                    FormatText(R.string.cqi_format, "$cqi")
                })

                object CQIIndex : LTEKeys(
                    R.string.cqi_table_index_format,
                    "cqi-index",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && cqiTableIndex.avail() },
                    {
                        FormatText(R.string.cqi_table_index_format, "$cqiTableIndex")
                    })

                object RSSnR : LTEKeys(R.string.rssnr_format, "rssnr", { rssnr.avail() }, {
                    FormatText(R.string.rssnr_format, "$rssnr")
                })

                object TimingAdvance : LTEKeys(
                    R.string.timing_advance_format,
                    "timing-advance",
                    { timingAdvance.avail() },
                    {
                        FormatText(R.string.timing_advance_format, "$timingAdvance")
                    })
            }

            sealed class NRKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellSignalStrengthNrWrapper.() -> Boolean,
                render: @Composable CellSignalStrengthNrWrapper.() -> Unit
            ) : StrengthKeys<CellSignalStrengthNrWrapper>(label, "$key-nr", canRender, render) {
                override fun cast(info: Any?): CellSignalStrengthNrWrapper? {
                    return info?.castGeneric()
                }

                object SSRSRQ : NRKeys(R.string.ss_rsrq_format, "ss-rsrq", { ssRsrq.avail() }, {
                    FormatText(R.string.ss_rsrq_format, ssRsrq.toString())
                })

                object CSIRSRQ : NRKeys(R.string.csi_rsrq_format, "csi-rsrq", { csiRsrq.avail() }, {
                    FormatText(R.string.csi_rsrq_format, csiRsrq.toString())
                })

                object CSICQIReport : NRKeys(R.string.csi_cqi_report_format,
                    "csi-cqi-report",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !csiCqiReport.isNullOrEmpty() },
                    {
                        FormatText(R.string.csi_cqi_report_format, csiCqiReport?.joinToString(", "))
                    })

                object CSICQIIndex : NRKeys(R.string.csi_cqi_table_index_format,
                    "csi-cqi-index",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && csiCqiTableIndex.avail() },
                    {
                        FormatText(R.string.csi_cqi_table_index_format, "$csiCqiTableIndex")
                    })

                object SSSinR : NRKeys(R.string.ss_sinr_format, "ss-sinr", { ssSinr.avail() }, {
                    FormatText(R.string.ss_sinr_format, "$ssSinr")
                })
            }

            sealed class TDSCDMAKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellSignalStrengthTdscdmaWrapper.() -> Boolean,
                render: @Composable CellSignalStrengthTdscdmaWrapper.() -> Unit
            ) : StrengthKeys<CellSignalStrengthTdscdmaWrapper>(
                label,
                "$key-tdscdma",
                canRender,
                render
            ) {
                override fun cast(info: Any?): CellSignalStrengthTdscdmaWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : TDSCDMAKeys(R.string.rssi_format, "rssi", { rssi.avail() }, {
                    FormatText(R.string.rssi_format, "$rssi")
                })

                object BitErrorRate :
                    TDSCDMAKeys(R.string.bit_error_rate_format, "ber", { bitErrorRate.avail() }, {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    })

                object RSCP : TDSCDMAKeys(R.string.rscp_format, "rscp", { rscp.avail() }, {
                    FormatText(R.string.rscp_format, "$rscp")
                })
            }

            sealed class WCDMAKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellSignalStrengthWcdmaWrapper.() -> Boolean,
                render: @Composable CellSignalStrengthWcdmaWrapper.() -> Unit
            ) : StrengthKeys<CellSignalStrengthWcdmaWrapper>(
                label,
                "$key-wcdma",
                canRender,
                render
            ) {
                override fun cast(info: Any?): CellSignalStrengthWcdmaWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : WCDMAKeys(
                    R.string.rssi_format,
                    "rssi",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rssi.avail() },
                    {
                        FormatText(R.string.rssi_format, "$rssi")
                    })

                object BitErrorRate :
                    WCDMAKeys(R.string.bit_error_rate_format, "ber", { bitErrorRate.avail() }, {
                        FormatText(R.string.bit_error_rate_format, "$bitErrorRate")
                    })

                object RSCP : WCDMAKeys(
                    R.string.rscp_format,
                    "rscp",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rscp.avail() },
                    {
                        FormatText(R.string.rscp_format, "$rscp")
                    })

                object EcNo : WCDMAKeys(
                    R.string.ecno_format,
                    "ecno",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ecNo.avail() },
                    {
                        FormatText(R.string.ecno_format, "$ecNo")
                    })
            }
        }

        sealed class InfoKeys<T>(
            @StringRes label: Int,
            key: String,
            canRender: T.() -> Boolean,
            render: @Composable T.() -> Unit
        ) : Keys<T>(label, "$key-info", canRender, render) {
            object Registered : InfoKeys<CellInfoWrapper>(R.string.registered_format, "registered",
                { true }, {
                    FormatText(R.string.registered_format, isRegistered.toString())
                }) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            object Status :
                InfoKeys<CellInfoWrapper>(R.string.cell_connection_status_format, "status",
                    { true }, {
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

            object Timestamp : InfoKeys<CellInfoWrapper>(R.string.timestamp_format, "timestamp",
                { true }, {
                    FormatText(R.string.timestamp_format, timeStamp)
                }) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class LTEKeys(
                @StringRes label: Int,
                key: String,
                canRender: CellInfoLteWrapper.() -> Boolean,
                render: @Composable CellInfoLteWrapper.() -> Unit
            ) : InfoKeys<CellInfoLteWrapper>(label, "$key-lte", canRender, render) {
                override fun cast(info: Any?): CellInfoLteWrapper? {
                    return info?.castGeneric()
                }

                object ENDC : LTEKeys(
                    R.string.endc_available_format,
                    "endc",
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellConfig != null },
                    {
                        FormatText(R.string.endc_available_format, "${cellConfig?.endcAvailable}")
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
            defaultOrder: Boolean = false
        ) {
            val context = LocalContext.current

            with(Orderer) {
                val order = remember(identity) {
                    identity.orderOf()
                }

                val (simpleOrder, advancedOrder) = remember(order, defaultOrder) {
                    with(order) {
                        if (defaultOrder) MutableStateFlow(order.defaultSplitOrder) else context.splitOrder
                    }
                }.collectAsState(initial = order.defaultSplitOrder).value

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

            with(Orderer) {
                val order = remember(strength) {
                    strength.orderOf()
                }

                val (simpleOrder, advancedOrder) = remember(order) {
                    with(order) {
                        context.splitOrder
                    }
                }.collectAsState(initial = order.defaultSplitOrder).value

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
        sealed class PreferenceKey(keyString: String) {
            val key: Preferences.Key<Set<String>> = stringSetPreferencesKey(keyString)

            sealed class Identity(keyString: String) : PreferenceKey("$keyString-identity") {
                object CDMA : Identity("cdma")
                object GSM : Identity("gsm")
                object LTE : Identity("lte")
                object NR : Identity("nr")
                object TDSCDMA : Identity("tdscdma")
                object WCDMA : Identity("wcdma")
            }

            sealed class Strength(keyString: String) : PreferenceKey("$keyString-strength") {
                object CDMA : Strength("cdma")
                object GSM : Strength("gsm")
                object LTE : Strength("lte")
                object NR : Strength("nr")
                object TDSCDMA : Strength("tdscdma")
                object WCDMA : Strength("wcdma")
            }
        }

        sealed class Order(
            private val key: PreferenceKey,
            private val defaultOrder: List<Keys<*>>
        ) {
            val Context.order: Flow<List<Keys<*>>>
                get() = store.data.map {
                    it[key.key]?.toKeys()?.ifEmpty { defaultOrder } ?: defaultOrder
                }

            val Context.splitOrder: Flow<Pair<List<Keys<*>>, List<Keys<*>>>>
                get() = order.map { it.splitInfo }

            val Context.hasAdvancedItems: Flow<Boolean>
                get() = splitOrder.map { it.second.isNotEmpty() }

            val defaultSplitOrder = defaultOrder.splitInfo

            suspend fun Context.updateOrder(newOrder: List<Keys<*>>) {
                // Separate remove and set operations needed here in order
                // for the store to actually update.
                store.edit {
                    it.remove(key.key)
                }
                store.edit {
                    it[key.key] = newOrder.map { k -> k.key }.toSet()
                }
            }

            sealed class Identity(key: PreferenceKey, defaultOrder: List<Keys<*>>) :
                Order(key, defaultOrder) {
                object CDMA : Identity(PreferenceKey.Identity.CDMA, defaultIdentityCdmaOrder)
                object GSM : Identity(PreferenceKey.Identity.GSM, defaultIdentityGsmOrder)
                object LTE : Identity(PreferenceKey.Identity.LTE, defaultIdentityLteOrder)
                object NR : Identity(PreferenceKey.Identity.NR, defaultIdentityNrOrder)
                object TDSCDMA :
                    Identity(PreferenceKey.Identity.TDSCDMA, defaultIdentityTdscdmaOrder)

                object WCDMA : Identity(PreferenceKey.Identity.WCDMA, defaultIdentityWcdmaOrder)
            }

            sealed class Strength(key: PreferenceKey, defaultOrder: List<Keys<*>>) :
                Order(key, defaultOrder) {
                object CDMA : Strength(PreferenceKey.Strength.CDMA, defaultStrengthCdmaOrder)
                object GSM : Strength(PreferenceKey.Strength.GSM, defaultStrengthGsmOrder)
                object LTE : Strength(PreferenceKey.Strength.LTE, defaultStrengthLteOrder)
                object NR : Strength(PreferenceKey.Strength.NR, defaultStrengthNrOrder)
                object TDSCDMA :
                    Strength(PreferenceKey.Strength.TDSCDMA, defaultStrengthTdscdmaOrder)

                object WCDMA : Strength(PreferenceKey.Strength.WCDMA, defaultStrengthWcdmaOrder)
            }
        }

        private val Context.store by preferencesDataStore(
            "cell_item_order"
        )

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

        val List<Keys<*>>.advancedIndex: Int
            get() = indexOf(Keys.AdvancedSeparator)

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

        fun CellIdentityWrapper.orderOf(): Order.Identity =
            when (this) {
                is CellIdentityCdmaWrapper -> Order.Identity.CDMA
                is CellIdentityGsmWrapper -> Order.Identity.GSM
                is CellIdentityLteWrapper -> Order.Identity.LTE
                is CellIdentityNrWrapper -> Order.Identity.NR
                is CellIdentityTdscdmaWrapper -> Order.Identity.TDSCDMA
                is CellIdentityWcdmaWrapper -> Order.Identity.WCDMA
            }

        fun CellSignalStrengthWrapper.orderOf(): Order.Strength =
            when (this) {
                is CellSignalStrengthCdmaWrapper -> Order.Strength.CDMA
                is CellSignalStrengthGsmWrapper -> Order.Strength.GSM
                is CellSignalStrengthLteWrapper -> Order.Strength.LTE
                is CellSignalStrengthNrWrapper -> Order.Strength.NR
                is CellSignalStrengthTdscdmaWrapper -> Order.Strength.TDSCDMA
                is CellSignalStrengthWcdmaWrapper -> Order.Strength.WCDMA
            }

        private fun String.keyStringToKeys(): Keys<*> {
            return Keys.getAllObjects().first { it.key == this }
        }

        private fun Set<String>.toKeys(): List<Keys<*>> {
            return this.map { it.keyStringToKeys() }
        }
    }
}