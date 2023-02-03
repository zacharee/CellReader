package dev.zwander.cellreader.data.data

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
        private val helpText: @Composable T?.() -> Any?,
        private val canRender: T.() -> Boolean,
        private val constructValue: @Composable T.() -> String?,
    ) {
        companion object {
            private val objectsCache = mutableListOf<Keys<*>>()

            @Synchronized
            fun getAllObjects(): List<Keys<*>> {
                return objectsCache.ifEmpty {
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
                    val value = it.constructValue()

                    FormatText(
                        textId = label,
                        textFormat = value,
                        helpText = retrieveHelpText(info = info)
                    )
                }
            }
        }

        @Composable
        fun retrieveHelpText(info: Any?): String? {
            return cast(info).let {
                it.helpText()?.let { help ->
                    if (help is Int) {
                        stringResource(id = help)
                    } else {
                        help.toString()
                    }
                }
            }
        }

        fun canRender(info: Any?) = cast(info)?.let { it.canRender() } ?: false

        object AdvancedSeparator : Keys<Unit>(
            R.string.advanced,
            "advanced",
            { null },
            { false },
            { null }
        ) {
            override fun cast(info: Any?): Unit? {
                return null
            }
        }

        sealed class IdentityKeys<T : CellIdentityWrapper>(
            @StringRes label: Int,
            key: String,
            helpText: @Composable T?.() -> Any?,
            canRender: T.() -> Boolean,
            constructValue: @Composable T.() -> String?
        ) : Keys<T>(label, "identity-$key", helpText, canRender, constructValue) {
            object Bands : IdentityKeys<CellIdentityWrapper>(
                R.string.bands_format,
                "bands",
                { stringResource(id = R.string.bands_helper_text, this?.inferringBands.toString()) },
                { bands.isNotEmpty() },
                { bands.joinToString(", ") }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object Channel : IdentityKeys<CellIdentityWrapper>(
                R.string.channel_format,
                "channel",
                { R.string.channel_helper_text },
                { channelNumber.avail() },
                { channelNumber.toString() }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object GCI : IdentityKeys<CellIdentityWrapper>(
                R.string.gci_format,
                "gci",
                { R.string.gci_helper_text },
                { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && globalCellId != null },
                { globalCellId }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object Carrier : IdentityKeys<CellIdentityWrapper>(
                R.string.operator_format,
                "carrier",
                { R.string.carrier_helper_text },
                { !alphaLong.isNullOrBlank() || !alphaShort.isNullOrBlank() },
                {
                    setOf(
                        alphaLong,
                        alphaShort
                    ).joinToString("/")
                }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            object PLMN : IdentityKeys<CellIdentityWrapper>(
                R.string.plmn_format,
                "plmn",
                { R.string.plmn_helper_text },
                { mcc != null && mnc != null },
                { "${mcc}-${mnc}" }) {
                override fun cast(info: Any?): CellIdentityWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class CDMAKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellIdentityCdmaWrapper?.() -> Any?,
                canRender: CellIdentityCdmaWrapper.() -> Boolean,
                constructValue: @Composable CellIdentityCdmaWrapper.() -> String?
            ) : IdentityKeys<CellIdentityCdmaWrapper>(
                label,
                "$key-cdma",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellIdentityCdmaWrapper? {
                    return info?.castGeneric()
                }

                object LatLon : CDMAKeys(
                    R.string.lat_lon_format,
                    "lat-lon",
                    { R.string.lat_lon_helper_text },
                    { latitude.avail() && longitude.avail() },
                    { "${latitude}/${longitude}" })

                object CDMANetID : CDMAKeys(
                    R.string.cdma_network_id_format,
                    "net-id",
                    { R.string.net_id_helper_text },
                    { networkId.avail() },
                    { networkId.toString() })

                object BasestationID : CDMAKeys(
                    R.string.basestation_id_format,
                    "basestation-id",
                    { R.string.basestation_id_helper_text },
                    { basestationId.avail() },
                    { basestationId.toString() })

                object CDMASysID : CDMAKeys(
                    R.string.cdma_system_id_format,
                    "sys-id",
                    { R.string.sys_id_helper_text },
                    { systemId.avail() },
                    { systemId.toString() })
            }

            sealed class GSMKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellIdentityGsmWrapper?.() -> Any?,
                canRender: CellIdentityGsmWrapper.() -> Boolean,
                constructValue: @Composable CellIdentityGsmWrapper.() -> String?
            ) : IdentityKeys<CellIdentityGsmWrapper>(
                label,
                "$key-gsm",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellIdentityGsmWrapper? {
                    return info?.castGeneric()
                }

                object LAC : GSMKeys(
                    R.string.lac_format,
                    "lac",
                    { R.string.lac_helper_text },
                    { lac.avail() },
                    { lac.toString() }
                )

                object CID : GSMKeys(
                    R.string.cid_format,
                    "cid",
                    { R.string.cid_helper_text },
                    { cid.avail() },
                    { cid.toString() }
                )

                object BSIC : GSMKeys(
                    R.string.bsic_format,
                    "bsic",
                    { R.string.bsic_helper_text },
                    { bsic.avail() },
                    { bsic.toString() }
                )

                object ARFCN : GSMKeys(
                    R.string.arfcn_format,
                    "arfcn",
                    { R.string.arfcn_helper_text },
                    { arfcn.avail() },
                    { arfcn.toString() })

                object DLFreqs : GSMKeys(
                    R.string.dl_freqs_format,
                    "dlfreqs",
                    { R.string.dl_freqs_helper_text },
                    { arfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(arfcn)) {
                            arfcnInfo.map { it.dlFreq }.joinToString(", ")
                        }
                    }
                )

                object ULFreqs : GSMKeys(
                    R.string.ul_freqs_format,
                    "ulfreqs",
                    { R.string.ul_freqs_helper_text },
                    { arfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(arfcn)) {
                            arfcnInfo.map { it.ulFreq }.joinToString(", ")
                        }
                    }
                )

                object AdditionalPLMNs : GSMKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { R.string.additional_plmns_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    {
                        additionalPlmns?.joinToString(", ") { it.asMccMnc }
                    }
                )
            }

            sealed class LTEKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellIdentityLteWrapper?.() -> Any?,
                canRender: CellIdentityLteWrapper.() -> Boolean,
                constructValue: @Composable CellIdentityLteWrapper.() -> String?
            ) : IdentityKeys<CellIdentityLteWrapper>(
                label,
                "$key-lte",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellIdentityLteWrapper? {
                    return info?.castGeneric()
                }

                object Bandwidth :
                    LTEKeys(
                        R.string.bandwidth_format,
                        "bandwidth",
                        { R.string.bandwidth_helper_text },
                        { bandwidth.avail() },
                        { bandwidth.toString() }
                    )

                object TAC : LTEKeys(
                    R.string.tac_format,
                    "tac",
                    { R.string.tac_helper_text },
                    { tac.avail() },
                    { tac.toString() }
                )

                object CI : LTEKeys(
                    R.string.ci_format,
                    "ci",
                    { R.string.ci_helper_text },
                    { ci.avail() },
                    { ci.toString() }
                )

                object PCI : LTEKeys(
                    R.string.pci_format,
                    "pci",
                    { R.string.pci_helper_text },
                    { pci.avail() },
                    { pci.toString() }
                )

                object EARFCN : LTEKeys(
                    R.string.earfcn_format,
                    "earfcn",
                    { R.string.arfcn_helper_text },
                    { earfcn.avail() },
                    { earfcn.toString() }
                )

                object DLFreqs : LTEKeys(
                    R.string.dl_freqs_format,
                    "dlfreqs",
                    { R.string.dl_freqs_helper_text },
                    { earfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(earfcn)) {
                            arfcnInfo.map { it.dlFreq }.joinToString(", ")
                        }
                    }
                )

                object ULFreqs : LTEKeys(
                    R.string.ul_freqs_format,
                    "ulfreqs",
                    { R.string.ul_freqs_helper_text },
                    { earfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(earfcn)) {
                            arfcnInfo.map { it.ulFreq }.joinToString(", ")
                        }
                    }
                )

                object AdditionalPLMNs : LTEKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { R.string.additional_plmns_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    { additionalPlmns?.joinToString(", ") { it.asMccMnc } }
                )

                object CSGID : LTEKeys(
                    R.string.csg_id_format,
                    "csg-id",
                    { R.string.csg_id_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.csgIdentity.toString() }
                )

                object CSGIndicator : LTEKeys(
                    R.string.csg_indicator_format,
                    "csg-indicator",
                    { R.string.csg_indicator_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.csgIndicator?.toString() }
                )

                object HomeNodeBName : LTEKeys(
                    R.string.home_node_b_name_format,
                    "home-node-b-name",
                    { R.string.home_node_b_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.homeNodebName }
                )
            }

            sealed class NRKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellIdentityNrWrapper?.() -> Any?,
                canRender: CellIdentityNrWrapper.() -> Boolean,
                constructValue: @Composable CellIdentityNrWrapper.() -> String?
            ) : IdentityKeys<CellIdentityNrWrapper>(
                label,
                "$key-nr",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellIdentityNrWrapper? {
                    return info?.castGeneric()
                }

                object TAC : NRKeys(
                    R.string.tac_format,
                    "tac",
                    { R.string.tac_helper_text },
                    { tac.avail() },
                    { tac.toString() }
                )

                object NCI : NRKeys(
                    R.string.nci_format,
                    "nci",
                    { R.string.nci_helper_text },
                    { nci.avail() },
                    { nci.toString() }
                )

                object PCI : NRKeys(
                    R.string.pci_format,
                    "pci",
                    { R.string.pci_helper_text },
                    { pci.avail() },
                    { pci.toString() }
                )

                object NRARFCN : NRKeys(
                    R.string.nrarfcn_format,
                    "nrarfcn",
                    { R.string.arfcn_helper_text },
                    { nrArfcn.avail() },
                    { nrArfcn.toString() }
                )

                object DLFreqs : NRKeys(
                    R.string.dl_freqs_format,
                    "dlfreqs",
                    { R.string.dl_freqs_helper_text },
                    { nrArfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(nrArfcn)) {
                            arfcnInfo.map { it.dlFreq }.joinToString(", ")
                        }
                    }
                )

                object ULFreqs : NRKeys(
                    R.string.ul_freqs_format,
                    "ulfreqs",
                    { R.string.ul_freqs_helper_text },
                    { nrArfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(nrArfcn)) {
                            arfcnInfo.map { it.ulFreq }.joinToString(", ")
                        }
                    }
                )

                object AdditionalPLMNs : NRKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { R.string.additional_plmns_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    { additionalPlmns?.joinToString(", ") { it.asMccMnc } }
                )
            }

            sealed class TDSCDMAKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellIdentityTdscdmaWrapper?.() -> Any?,
                canRender: CellIdentityTdscdmaWrapper.() -> Boolean,
                constructValue: @Composable CellIdentityTdscdmaWrapper.() -> String?
            ) : IdentityKeys<CellIdentityTdscdmaWrapper>(
                label,
                "$key-tdscdma",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellIdentityTdscdmaWrapper? {
                    return info?.castGeneric()
                }

                object LAC : TDSCDMAKeys(
                    R.string.lac_format,
                    "lac",
                    { R.string.lac_helper_text },
                    { lac.avail() },
                    { lac.toString() }
                )

                object CID : TDSCDMAKeys(
                    R.string.cid_format,
                    "cid",
                    { R.string.cid_helper_text },
                    { cid.avail() },
                    { cid.toString() }
                )

                object CPID : TDSCDMAKeys(
                    R.string.cpid_format,
                    "cpid",
                    { R.string.cpid_helper_text },
                    { cpid.avail() },
                    { cpid.toString() }
                )

                object UARFCN : TDSCDMAKeys(
                    R.string.uarfcn_format,
                    "uarfcn",
                    { R.string.arfcn_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uarfcn.avail() },
                    { uarfcn.toString() }
                )

                object Freqs : TDSCDMAKeys(
                    R.string.freqs_format,
                    "freqs",
                    { R.string.freqs_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uarfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(uarfcn)) {
                            arfcnInfo.map { it.dlFreq }.joinToString(", ")
                        }
                    }
                )

                object AdditionalPLMNs : TDSCDMAKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { R.string.additional_plmns_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    { additionalPlmns?.joinToString(", ") { it.asMccMnc } }
                )

                object CSGID : TDSCDMAKeys(
                    R.string.csg_id_format,
                    "csg-id",
                    { R.string.csg_id_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.csgIdentity?.toString() }
                )

                object CSGIndicator : TDSCDMAKeys(
                    R.string.csg_indicator_format,
                    "csg-indicator",
                    { R.string.csg_indicator_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.csgIndicator?.toString() }
                )

                object HomeNodeBName : TDSCDMAKeys(
                    R.string.home_node_b_name_format,
                    "home-node-b-name",
                    { R.string.home_node_b_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.homeNodebName }
                )
            }

            sealed class WCDMAKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellIdentityWcdmaWrapper?.() -> Any?,
                canRender: CellIdentityWcdmaWrapper.() -> Boolean,
                constructValue: @Composable CellIdentityWcdmaWrapper.() -> String?
            ) : IdentityKeys<CellIdentityWcdmaWrapper>(
                label,
                "$key-wcdma",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellIdentityWcdmaWrapper? {
                    return info?.castGeneric()
                }

                object LAC : WCDMAKeys(
                    R.string.lac_format,
                    "lac",
                    { R.string.lac_helper_text },
                    { lac.avail() },
                    { lac.toString() }
                )

                object CID : WCDMAKeys(
                    R.string.cid_format,
                    "cid",
                    { R.string.cid_helper_text },
                    { cid.avail() },
                    { cid.toString() }
                )

                object UARFCN : WCDMAKeys(
                    R.string.uarfcn_format,
                    "uarfcn",
                    { R.string.arfcn_helper_text },
                    { uarfcn.avail() },
                    { uarfcn.toString() }
                )

                object DLFreqs : WCDMAKeys(
                    R.string.dl_freqs_format,
                    "dlfreqs",
                    { R.string.dl_freqs_helper_text },
                    { uarfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(uarfcn)) {
                            arfcnInfo.map { it.dlFreq }.joinToString(", ")
                        }
                    }
                )

                object ULFreqs : WCDMAKeys(
                    R.string.ul_freqs_format,
                    "ulfreqs",
                    { R.string.ul_freqs_helper_text },
                    { uarfcn.avail() },
                    {
                        rememberSaveable(inputs = arrayOf(uarfcn)) {
                            arfcnInfo.map { it.ulFreq }.joinToString(", ")
                        }
                    }
                )

                object AdditionalPLMNs : WCDMAKeys(
                    R.string.additional_plmns_format,
                    "additional-plmns",
                    { R.string.additional_plmns_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !additionalPlmns.isNullOrEmpty() },
                    { additionalPlmns?.joinToString(", ") { it.asMccMnc } }
                )

                object CSGID : WCDMAKeys(
                    R.string.csg_id_format,
                    "csg-id",
                    { R.string.csg_id_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.csgIdentity?.toString() }
                )

                object CSGIndicator : WCDMAKeys(
                    R.string.csg_indicator_format,
                    "csg-indicator",
                    { R.string.csg_indicator_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.csgIndicator?.toString() }
                )

                object HomeNodeBName : WCDMAKeys(
                    R.string.home_node_b_name_format,
                    "home-node-b-name",
                    { R.string.home_node_b_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && csgInfo != null },
                    { csgInfo?.homeNodebName }
                )
            }
        }

        sealed class StrengthKeys<T>(
            @StringRes label: Int,
            key: String,
            helpText: @Composable T?.() -> Any?,
            canRender: T.() -> Boolean,
            constructValue: @Composable T.() -> String?
        ) : Keys<T>(label, "$key-strength", helpText, canRender, constructValue) {
            object ASU :
                StrengthKeys<CellSignalStrengthWrapper>(
                    R.string.asu_format,
                    "asu",
                    { R.string.asu_helper_text },
                    { true },
                    { asuLevel.toString() }
                ) {
                override fun cast(info: Any?): CellSignalStrengthWrapper? {
                    return info?.castGeneric()
                }
            }

            object Valid : StrengthKeys<CellSignalStrengthWrapper>(
                R.string.valid_format,
                "valid",
                { R.string.valid_helper_text },
                { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q },
                { valid?.toString() }
            ) {
                override fun cast(info: Any?): CellSignalStrengthWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class CDMAKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellSignalStrengthCdmaWrapper?.() -> Any?,
                canRender: CellSignalStrengthCdmaWrapper.() -> Boolean,
                constructValue: @Composable CellSignalStrengthCdmaWrapper.() -> String?
            ) : StrengthKeys<CellSignalStrengthCdmaWrapper>(
                label,
                "$key-cdma",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellSignalStrengthCdmaWrapper? {
                    return info?.castGeneric()
                }

                object CDMAdBm : CDMAKeys(
                    R.string.cdma_dbm_format,
                    "cdma-dbm",
                    { R.string.dbm_helper_text },
                    { cdmaDbm.avail() },
                    { cdmaDbm.toString() }
                )

                object EvDOdBm : CDMAKeys(
                    R.string.evdo_dbm_format,
                    "evdo-dbm",
                    { R.string.dbm_helper_text },
                    { evdoDbm.avail() },
                    { evdoDbm.toString() }
                )

                object CDMAEcIo : CDMAKeys(
                    R.string.cdma_ecio_format,
                    "cdma-ecio",
                    { R.string.ecio_helper_text },
                    { cdmaEcio.avail() },
                    { cdmaEcio.toString() }
                )

                object EvDOEcIo : CDMAKeys(
                    R.string.evdo_ecio_format,
                    "evdo-ecio",
                    { R.string.ecio_helper_text },
                    { evdoEcio.avail() },
                    { evdoEcio.toString() }
                )

                object SnR : CDMAKeys(
                    R.string.snr_format,
                    "snr",
                    { R.string.snr_helper_text },
                    { evdoSnr.avail() },
                    { evdoSnr.toString() }
                )

                object EvDOASU : CDMAKeys(
                    R.string.evdo_asu_format,
                    "evdo-asu",
                    { R.string.asu_helper_text },
                    { true },
                    { evdoAsuLevel.toString() }
                )
            }

            sealed class GSMKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellSignalStrengthGsmWrapper?.() -> Any?,
                canRender: CellSignalStrengthGsmWrapper.() -> Boolean,
                constructValue: @Composable CellSignalStrengthGsmWrapper.() -> String?
            ) : StrengthKeys<CellSignalStrengthGsmWrapper>(
                label,
                "$key-gsm",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellSignalStrengthGsmWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : GSMKeys(
                    R.string.rssi_format,
                    "rssi",
                    { R.string.rssi_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rssi.avail() },
                    { rssi.toString() }
                )

                object BitErrorRate : GSMKeys(
                    R.string.bit_error_rate_format,
                    "ber",
                    { R.string.bit_error_rate_helper_text },
                    { bitErrorRate.avail() },
                    { bitErrorRate.toString() }
                )

                object TimingAdvance : GSMKeys(
                    R.string.timing_advance_format,
                    "timing-advance",
                    { R.string.timing_advance_helper_text },
                    { timingAdvance.avail() },
                    { timingAdvance.toString() }
                )
            }

            sealed class LTEKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellSignalStrengthLteWrapper?.() -> Any?,
                canRender: CellSignalStrengthLteWrapper.() -> Boolean,
                constructValue: @Composable CellSignalStrengthLteWrapper.() -> String?
            ) : StrengthKeys<CellSignalStrengthLteWrapper>(
                label,
                "$key-lte",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellSignalStrengthLteWrapper? {
                    return info?.castGeneric()
                }

                object RSRQ : LTEKeys(
                    R.string.rsrq_format,
                    "rsrq",
                    { R.string.rsrq_helper_text },
                    { true },
                    { rsrq.toString() }
                )

                object RSSI : LTEKeys(
                    R.string.rssi_format,
                    "rssi",
                    { R.string.rssi_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rssi.avail() },
                    { rssi.toString() }
                )

                object CQI : LTEKeys(
                    R.string.cqi_format,
                    "cqi",
                    { R.string.cqi_helper_text },
                    { cqi.avail() },
                    { cqi.toString() }
                )

                object CQIIndex : LTEKeys(
                    R.string.cqi_table_index_format,
                    "cqi-index",
                    { R.string.cqi_index_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && cqiTableIndex.avail() },
                    { cqiTableIndex.toString() }
                )

                object RSSnR : LTEKeys(
                    R.string.rssnr_format,
                    "rssnr",
                    { R.string.snr_helper_text },
                    { rssnr.avail() },
                    { rssnr.toString() }
                )

                object TimingAdvance : LTEKeys(
                    R.string.timing_advance_format,
                    "timing-advance",
                    { R.string.timing_advance_helper_text },
                    { timingAdvance.avail() },
                    { timingAdvance.toString() }
                )
            }

            sealed class NRKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellSignalStrengthNrWrapper?.() -> Any?,
                canRender: CellSignalStrengthNrWrapper.() -> Boolean,
                constructValue: @Composable CellSignalStrengthNrWrapper.() -> String?
            ) : StrengthKeys<CellSignalStrengthNrWrapper>(
                label,
                "$key-nr",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellSignalStrengthNrWrapper? {
                    return info?.castGeneric()
                }

                object SSRSRQ : NRKeys(
                    R.string.ss_rsrq_format,
                    "ss-rsrq",
                    { R.string.rsrq_helper_text },
                    { ssRsrq.avail() },
                    { ssRsrq.toString() }
                )

                object CSIRSRQ : NRKeys(
                    R.string.csi_rsrq_format,
                    "csi-rsrq",
                    { R.string.rsrq_helper_text },
                    { csiRsrq.avail() },
                    { csiRsrq.toString() }
                )

                object CSICQIReport : NRKeys(R.string.csi_cqi_report_format,
                    "csi-cqi-report",
                    { R.string.cqi_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !csiCqiReport.isNullOrEmpty() },
                    { csiCqiReport?.joinToString(", ") }
                )

                object CSICQIIndex : NRKeys(R.string.csi_cqi_table_index_format,
                    "csi-cqi-index",
                    { R.string.cqi_index_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && csiCqiTableIndex.avail() },
                    { csiCqiTableIndex.toString() }
                )

                object SSSinR : NRKeys(
                    R.string.ss_sinr_format,
                    "ss-sinr",
                    { R.string.snr_helper_text },
                    { ssSinr.avail() },
                    { ssSinr.toString() }
                )
            }

            sealed class TDSCDMAKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellSignalStrengthTdscdmaWrapper?.() -> Any?,
                canRender: CellSignalStrengthTdscdmaWrapper.() -> Boolean,
                constructValue: @Composable CellSignalStrengthTdscdmaWrapper.() -> String?
            ) : StrengthKeys<CellSignalStrengthTdscdmaWrapper>(
                label,
                "$key-tdscdma",
                helpText, canRender, constructValue
            ) {
                override fun cast(info: Any?): CellSignalStrengthTdscdmaWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : TDSCDMAKeys(
                    R.string.rssi_format,
                    "rssi",
                    { R.string.rssi_helper_text },
                    { rssi.avail() },
                    { rssi.toString() }
                )

                object BitErrorRate : TDSCDMAKeys(
                    R.string.bit_error_rate_format,
                    "ber",
                    { R.string.bit_error_rate_helper_text },
                    { bitErrorRate.avail() },
                    { bitErrorRate.toString() }
                )

                object RSCP : TDSCDMAKeys(
                    R.string.rscp_format,
                    "rscp",
                    { R.string.rscp_helper_text },
                    { rscp.avail() },
                    { rscp.toString() }
                )
            }

            sealed class WCDMAKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellSignalStrengthWcdmaWrapper?.() -> Any?,
                canRender: CellSignalStrengthWcdmaWrapper.() -> Boolean,
                constructValue: @Composable CellSignalStrengthWcdmaWrapper.() -> String?
            ) : StrengthKeys<CellSignalStrengthWcdmaWrapper>(
                label,
                "$key-wcdma",
                helpText, canRender, constructValue
            ) {
                override fun cast(info: Any?): CellSignalStrengthWcdmaWrapper? {
                    return info?.castGeneric()
                }

                object RSSI : WCDMAKeys(
                    R.string.rssi_format,
                    "rssi",
                    { R.string.rssi_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rssi.avail() },
                    { rssi.toString() }
                )

                object BitErrorRate : WCDMAKeys(
                    R.string.bit_error_rate_format,
                    "ber",
                    { R.string.bit_error_rate_helper_text },
                    { bitErrorRate.avail() },
                    { bitErrorRate.toString() }
                )

                object RSCP : WCDMAKeys(
                    R.string.rscp_format,
                    "rscp",
                    { R.string.rscp_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && rscp.avail() },
                    { rscp.toString() }
                )

                object EcNo : WCDMAKeys(
                    R.string.ecno_format,
                    "ecno",
                    { R.string.ecno_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ecNo.avail() },
                    { ecNo.toString() }
                )
            }
        }

        sealed class InfoKeys<T>(
            @StringRes label: Int,
            key: String,
            helpText: @Composable T?.() -> Any?,
            canRender: T.() -> Boolean,
            constructValue: @Composable T.() -> String?
        ) : Keys<T>(label, "$key-info", helpText, canRender, constructValue) {
            object Registered : InfoKeys<CellInfoWrapper>(
                R.string.registered_format,
                "registered",
                { R.string.registered_helper_text },
                { true },
                { isRegistered.toString() }
            ) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            object Status :
                InfoKeys<CellInfoWrapper>(
                    R.string.cell_connection_status_format,
                    "status",
                    { R.string.status_helper_text },
                    { true },
                    {
                        val context = LocalContext.current

                        dev.zwander.cellreader.data.util.CellUtils.connectionStatusToString(
                            context,
                            connectionStatus
                        )
                    }
                ) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            object Timestamp : InfoKeys<CellInfoWrapper>(
                R.string.timestamp_format,
                "timestamp",
                { R.string.timestamp_helper_text },
                { true },
                { timeStamp.toString() }
            ) {
                override fun cast(info: Any?): CellInfoWrapper? {
                    return info?.castGeneric()
                }
            }

            sealed class LTEKeys(
                @StringRes label: Int,
                key: String,
                helpText: @Composable CellInfoLteWrapper?.() -> Any?,
                canRender: CellInfoLteWrapper.() -> Boolean,
                constructValue: @Composable CellInfoLteWrapper.() -> String?
            ) : InfoKeys<CellInfoLteWrapper>(
                label,
                "$key-lte",
                helpText,
                canRender,
                constructValue
            ) {
                override fun cast(info: Any?): CellInfoLteWrapper? {
                    return info?.castGeneric()
                }

                object ENDC : LTEKeys(
                    R.string.endc_available_format,
                    "endc",
                    { R.string.endc_helper_text },
                    { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellConfig != null },
                    { cellConfig?.endcAvailable?.toString() }
                )
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