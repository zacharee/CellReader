package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.layout.AutoResizeText
import dev.zwander.cellreader.layout.FontSizeRange
import dev.zwander.cellreader.ui.theme.CellReaderTheme
import dev.zwander.cellreader.utils.PermissionUtils
import dev.zwander.cellreader.utils.cast
import dev.zwander.cellreader.utils.endcAvailable


class MainActivity : ComponentActivity() {
    private val permReq = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        if (results.values.any { !it }) {
            finish()
        } else {
            init()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(PermissionUtils.getMissingPermissions(this)) {
            if (isNotEmpty()) {
                permReq.launch(this)
            } else {
                init()
            }
        }
    }

    private fun init() {
        startForegroundService(Intent(this, UpdaterService::class.java))

        setContent {
            Content()
        }
    }
}

@Composable
fun SignalCard(cellInfo: CellInfo, expanded: Boolean, onExpand: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .clickable {
                    onExpand(!expanded)
                }
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(
                                when (cellInfo.cellSignalStrength.level) {
                                    CellSignalStrength.SIGNAL_STRENGTH_POOR -> R.drawable.cell_1
                                    CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> R.drawable.cell_2
                                    CellSignalStrength.SIGNAL_STRENGTH_GOOD -> R.drawable.cell_3
                                    CellSignalStrength.SIGNAL_STRENGTH_GREAT -> R.drawable.cell_4
                                    else -> R.drawable.cell_0
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .width(32.dp)
                                .height(32.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(Modifier.size(8.dp))

                        AutoResizeText(
                            text = "${cellInfo.cellSignalStrength.dbm} dBm",
                            fontSizeRange = FontSizeRange(8.sp, 16.sp),
                            modifier = Modifier.width(64.dp),
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.size(16.dp))

                    FlowRow(
                        mainAxisSpacing = 16.dp,
                        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                        mainAxisSize = SizeMode.Expand
                    ) {
                        with (cellInfo) {
                            with (cellIdentity) {
                                with (operatorAlphaLong) {
                                    if (!isNullOrBlank()) {
                                        Text(
                                            text = "Carrier: $this"
                                        )
                                    }
                                }

                                with (mccString) {
                                    if (!isNullOrBlank()) {
                                        Text(
                                            text = "PLMN: ${this}-${mncString}"
                                        )
                                    }
                                }

                                Text(
                                    text = "Type: ${
                                        when (type) {
                                            CellInfo.TYPE_GSM -> "GSM"
                                            CellInfo.TYPE_WCDMA -> "WCDMA"
                                            CellInfo.TYPE_CDMA -> "CDMA"
                                            CellInfo.TYPE_TDSCDMA -> "TDSCDMA"
                                            CellInfo.TYPE_LTE -> "LTE"
                                            CellInfo.TYPE_NR -> "5G NR"
                                            else -> "Unknown"
                                        }
                                    }"
                                )

                                cast<CellIdentityLte>()?.apply {
                                    Text(text = "Bands: ${bands.joinToString(", ")}")

                                    bandwidth.let {
                                        if (it != CellInfo.UNAVAILABLE) {
                                            Text(text = "Bandwidth: $it kHz")
                                        }
                                    }
                                }

                                cast<CellIdentityNr>()?.apply {
                                    Text(text = "Bands: ${bands.joinToString(", ")}")
                                }
                            }

                            with (cellSignalStrength) {
                                cast<CellSignalStrengthLte>()?.apply {
                                    Text(text = "RSRQ: $rsrq")
                                    Text(text = "RSSI: $rssi")
                                }

                                cast<CellSignalStrengthNr>()?.apply {
                                    Text(text = "RSRQ: ${csiRsrq}/${ssRsrq}")
                                }

                                cast<CellSignalStrengthGsm>()?.apply {
                                    Text(text = "RSSI: $rssi")
                                }

                                cast<CellSignalStrengthTdscdma>()?.apply {
                                    Text(text = "RSSI: $rssi")
                                }

                                cast<CellSignalStrengthWcdma>()?.apply {
                                    Text(text = "RSSI: $rssi")
                                }
                            }

                            cast<CellInfoLte>()?.apply {
                                Text(text = "ENDC: ${cellConfig.endcAvailable}")
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(Modifier.size(4.dp))

                        Divider()

                        Spacer(Modifier.size(4.dp))

                        FlowRow(
                            mainAxisSpacing = 16.dp,
                            mainAxisAlignment = MainAxisAlignment.SpaceBetween,
                            mainAxisSize = SizeMode.Expand
                        ) {
                            fun Int.avail() = this != CellInfo.UNAVAILABLE
                            fun Long.avail() = this != CellInfo.UNAVAILABLE_LONG

                            @Composable
                            fun Int.onAvail(block: @Composable()(Int) -> Unit) {
                                if (avail()) block(this)
                            }

                            @Composable
                            fun Long.onAvail(block: @Composable()(Long) -> Unit) {
                                if (avail()) block(this)
                            }

                            with (cellInfo) {
                                with (cellSignalStrength) {
                                    Text(text = "ASU: $asuLevel")
                                    Text(text = "Valid: $isValid")

                                    cast<CellSignalStrengthGsm>()?.apply {
                                        bitErrorRate.onAvail {
                                            Text(text = "Bit Error Rate: $bitErrorRate")
                                        }
                                        timingAdvance.onAvail {
                                            Text(text = "Timing Advance: $timingAdvance")
                                        }
                                    }

                                    cast<CellSignalStrengthCdma>()?.apply {
                                        cdmaDbm.onAvail {
                                            Text(text = "CDMA dBm: $cdmaDbm")
                                        }
                                        cdmaEcio.onAvail {
                                            Text(text = "CDMA Ec/Io: $cdmaEcio")
                                        }
                                        evdoDbm.onAvail {
                                            Text(text = "EvDO dBm: $evdoDbm")
                                        }
                                        evdoEcio.onAvail {
                                            Text(text = "EvDO Ec/Io: $evdoEcio")
                                        }
                                        evdoSnr.onAvail {
                                            Text(text = "EvDO SnR: $evdoSnr")
                                        }
                                    }

                                    cast<CellSignalStrengthTdscdma>()?.apply {
                                        bitErrorRate.onAvail {
                                            Text(text = "Bit Error Rate: $bitErrorRate")
                                        }

                                        rscp.onAvail {
                                            Text(text = "RSCP: $rscp")
                                        }
                                    }

                                    cast<CellSignalStrengthWcdma>()?.apply {
                                        bitErrorRate.onAvail {
                                            Text(text = "Bit Error Rate: $bitErrorRate")
                                        }

                                        rscp.onAvail {
                                            Text(text = "RSCP: $rscp")
                                        }

                                        ecNo.onAvail {
                                            Text(text = "Ec/No: $ecNo")
                                        }
                                    }

                                    cast<CellSignalStrengthLte>()?.apply {
                                        cqi.onAvail {
                                            Text(text = "CQI: $cqi")
                                        }
                                        cqiTableIndex.onAvail {
                                            Text(text = "CQI Table Index: $cqiTableIndex")
                                        }
                                        rssnr.onAvail {
                                            Text(text = "RSSnR: $rssnr")
                                        }
                                        timingAdvance.onAvail {
                                            Text(text = "Timing Advance: $timingAdvance")
                                        }
                                    }

                                    cast<CellSignalStrengthNr>()?.apply {
                                        if (csiCqiReport.isNotEmpty()) {
                                            Text(text = "CSI CQI Report: ${csiCqiReport.joinToString(", ")}")
                                        }
                                        csiCqiTableIndex.onAvail {
                                            Text(text = "CSI CQI Table Index: $csiCqiTableIndex")
                                        }
                                        ssSinr.onAvail {
                                            Text(text = "SSSinR: $ssSinr")
                                        }
                                    }
                                }

                                with (cellIdentity) {
                                    channelNumber.onAvail {
                                        Text("Channel Number: $channelNumber")
                                    }

                                    Text(text = "GCI: $globalCellId")

                                    cast<CellIdentityGsm>()?.apply {
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        arfcn.onAvail {
                                            Text("ARFCN: $arfcn")
                                        }
                                        bsic.onAvail {
                                            Text("BSIC: $bsic")
                                        }
                                        cid.onAvail {
                                            Text("CID: $cid")
                                        }
                                        lac.onAvail {
                                            Text("LAC: $lac")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                    }

                                    cast<CellIdentityCdma>()?.apply {
                                        basestationId.onAvail {
                                            Text("Basestation ID: $basestationId")
                                        }
                                        networkId.onAvail {
                                            Text("Network ID: $networkId")
                                        }
                                        systemId.onAvail {
                                            Text("System ID: $systemId")
                                        }
                                        latitude.onAvail {
                                            Text("Lat: $latitude")
                                        }
                                        longitude.onAvail {
                                            Text("Lon: $longitude")
                                        }
                                    }

                                    cast<CellIdentityTdscdma>()?.apply {
                                        cid.onAvail {
                                            Text("CID: $cid")
                                        }
                                        cpid.onAvail {
                                            Text("CPID: $cpid")
                                        }
                                        lac.onAvail {
                                            Text("LAC: $lac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        closedSubscriberGroupInfo?.apply {
                                            Text("CSG Identity: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        uarfcn.onAvail {
                                            Text("UARFCN: $uarfcn")
                                        }
                                    }

                                    cast<CellIdentityWcdma>()?.apply {
                                        cid.onAvail {
                                            Text("CID: $cid")
                                        }
                                        lac.onAvail {
                                            Text("LAC: $lac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        closedSubscriberGroupInfo?.apply {
                                            Text("CSG Identity: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        psc.onAvail {
                                            Text("PSC: $psc")
                                        }
                                        uarfcn.onAvail {
                                            Text("UARFCN: $uarfcn")
                                        }
                                    }

                                    cast<CellIdentityLte>()?.apply {
                                        ci.onAvail {
                                            Text("CI: $ci")
                                        }
                                        pci.onAvail {
                                            Text("PCI: $pci")
                                        }
                                        tac.onAvail {
                                            Text("TAC: $tac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        closedSubscriberGroupInfo?.apply {
                                            Text("CSG Identity: $csgIdentity")
                                            Text("CSG Indicator: $csgIndicator")
                                            Text("Home Node-B Name: $homeNodebName")
                                        }
                                        if (mobileNetworkOperator != null) {
                                            Text("Operator: $mobileNetworkOperator")
                                        }
                                        earfcn.onAvail {
                                            Text("EARFCN: $earfcn")
                                        }
                                    }

                                    cast<CellIdentityNr>()?.apply {
                                        nci.onAvail {
                                            Text("NCI: $nci")
                                        }
                                        pci.onAvail {
                                            Text("PCI: $pci")
                                        }
                                        tac.onAvail {
                                            Text("TAC: $tac")
                                        }
                                        if (additionalPlmns.isNotEmpty()) {
                                            Text("Additional PLMNs: ${additionalPlmns.joinToString(", ")}")
                                        }
                                        nrarfcn.onAvail {
                                            Text("NRARFCN: $nrarfcn")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Content() {
    val context = LocalContext.current
    val subs = remember {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    }

    CellReaderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val map = HashMap(cellInfos)
                val primaryInfo = map.remove(primaryCell) ?: return@LazyColumn

                val entryList = map.entries.map { it.key to it.value }.toMutableList()
                entryList.add(0, (primaryCell to primaryInfo))

                entryList.forEach { (t, u) ->
                    stickyHeader(t) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .animateItemPlacement(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val subInfo = subs.getActiveSubscriptionInfo(t)
                            val telephony = TelephonyManager.from(context).createForSubscriptionId(t)

                            val properInfo = telephony.serviceState
                                .getNetworkRegistrationInfoListForTransportType(
                                    AccessNetworkConstants.TRANSPORT_TYPE_WWAN
                                )
                                .first { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }

                            FlowRow(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                mainAxisSpacing = 16.dp,
                                mainAxisAlignment = FlowMainAxisAlignment.Center
                            ) {
                                Image(bitmap = subInfo.createIconBitmap(context).asImageBitmap(), contentDescription = null)
                                Text(text = "${subInfo.carrierName}")
                            }

                            FlowRow(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                mainAxisSpacing = 16.dp,
                                mainAxisAlignment = FlowMainAxisAlignment.Center
                            ) {
                                Text(text = "R-PLMN: ${StringBuilder(properInfo.registeredPlmn).insert(3, "-")}")
                                Text(text = "Type: ${telephony.networkTypeName}")
                                Text(text = "CA: ${telephony.serviceState.isUsingCarrierAggregation}")
                                Text(text = "NR: ${NetworkRegistrationInfo.nrStateToString(telephony.serviceState.nrState)}/" +
                                        ServiceState.frequencyRangeToString(telephony.serviceState.nrFrequencyRange)
                                )
                            }
                        }
                    }

                    items(u.size, { "$t:$it:${u[it].cellIdentity.globalCellId}" }) {
                        var expanded by remember {
                            mutableStateOf(false)
                        }
                        val info = u[it]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                        ) {
                            SignalCard(cellInfo = info, expanded = expanded, onExpand = { expanded = it })
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}