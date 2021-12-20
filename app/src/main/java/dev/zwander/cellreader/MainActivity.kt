package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.*
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import dev.zwander.cellreader.layout.StaggeredVerticalGrid
import dev.zwander.cellreader.ui.theme.CellReaderTheme
import dev.zwander.cellreader.utils.PermissionUtils
import kotlin.math.floor


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
fun SignalCard(cellInfo: CellInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
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

                    Text(text = "${cellInfo.cellSignalStrength.dbm} dBm")
                }

                Spacer(Modifier.size(16.dp))

                FlowRow(
                    mainAxisSpacing = 16.dp,
                    mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                ) {
                    with(cellInfo.cellIdentity.operatorAlphaLong) {
                        if (!isNullOrBlank()) {
                            Text(
                                text = "Carrier: $this"
                            )
                        }
                    }

                    with(cellInfo.cellIdentity.mccString) {
                        if (!isNullOrBlank()) {
                            Text(
                                text = "MCC-MNC: ${this}-${cellInfo.cellIdentity.mncString}"
                            )
                        }
                    }

                    Text(
                        text = "Signal type: ${
                            when (cellInfo.cellSignalStrength) {
                                is CellSignalStrengthGsm -> "GSM"
                                is CellSignalStrengthWcdma -> "WCDMA"
                                is CellSignalStrengthCdma -> "CDMA"
                                is CellSignalStrengthTdscdma -> "TDSCDMA"
                                is CellSignalStrengthLte -> "LTE"
                                is CellSignalStrengthNr -> "5G NR"
                                else -> "Unknown"
                            }
                        }"
                    )

                    with(cellInfo.cellSignalStrength) {
                        when (this) {
                            is CellSignalStrengthLte -> {
                                Text(text = "RSSI: ${rssi}")
                                Text(text = "RSRQ: ${rsrq}")
                            }
                            is CellSignalStrengthNr -> {
                                Text(text = "RSRQ: ${csiRsrq}/${ssRsrq}")
                            }
                        }
                    }

                    with(cellInfo.cellIdentity) {
                        when (this) {
                            is CellIdentityLte -> {
                                Text(text = "Bands: ${bands.contentToString()}")
                            }
                            is CellIdentityNr -> {
                                Text(text = "Bands: ${bands.contentToString()}")
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
            val cells = 205.dp

//            Column(
//                modifier = Modifier.verticalScroll(rememberScrollState())
//            ) {
//                StaggeredVerticalGrid(maxColumnWidth = cells) {
//                    val map = HashMap(cellInfos)
//                    val primaryInfo = map.remove(primaryCell) ?: return@StaggeredVerticalGrid
//
//                    val entryList = map.entries.map { it.key to it.value }.toMutableList()
//                    entryList.add(0, (primaryCell to primaryInfo))
//
//                    entryList.forEach { (t, u) ->
//                        Column(
//                            modifier = Modifier
//                                .fullSpan()
//                                .padding(8.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(text = "SIM $t")
//
//                            val subInfo = subs.getActiveSubscriptionInfo(t)
//                            val telephony = TelephonyManager.from(context).createForSubscriptionId(t)
//
//                            val properInfo = telephony.serviceState
//                                .getNetworkRegistrationInfoListForTransportType(
//                                    AccessNetworkConstants.TRANSPORT_TYPE_WWAN
//                                )
//                                .first { it.accessNetworkTechnology != TelephonyManager.NETWORK_TYPE_IWLAN }
//
//                            Text(text = "Carrier Name: ${subInfo.carrierName}")
//                            Text(text = "MCC-MNC: ${properInfo.cellIdentity.mccString}-${properInfo.cellIdentity.mncString}")
//                        }
//
//                        u.forEach { info ->
//                            Card(
//                                modifier = Modifier
//                            ) {
//                                SignalCard(cellInfo = info)
//                            }
//                        }
//                    }
//                }
//            }

            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val map = HashMap(cellInfos)
                val primaryInfo = map.remove(primaryCell) ?: return@LazyColumn

                val entryList = map.entries.map { it.key to it.value }.toMutableList()
                entryList.add(0, (primaryCell to primaryInfo))

                entryList.forEach { (t, u) ->
                    item(t) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .animateItemPlacement(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "SIM $t")

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
                                Text(text = "Carrier: ${subInfo.carrierName}")
                                Text(text = "RPLMN: ${properInfo.registeredPlmn}")
                                Text(text = "Type: ${telephony.networkTypeName}")
                                Text(text = "CA: ${telephony.serviceState.isUsingCarrierAggregation}")
                                Text(text = "NR: ${NetworkRegistrationInfo.nrStateToString(telephony.serviceState.nrState)}/" +
                                        ServiceState.frequencyRangeToString(telephony.serviceState.nrFrequencyRange)
                                )
                            }
                        }
                    }

                    items(u.size, { "$t:$it:${u[it].cellIdentity.globalCellId}" }) {
                        val info = u[it]

                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .animateItemPlacement()
                        ) {
                            SignalCard(cellInfo = info)
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