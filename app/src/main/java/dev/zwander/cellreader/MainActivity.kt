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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import dev.zwander.cellreader.layout.*
import dev.zwander.cellreader.ui.theme.CellReaderTheme
import dev.zwander.cellreader.utils.*


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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Content() {
    val context = LocalContext.current
    val subs = remember {
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    }

    CellReaderTheme {
        SelectionContainer {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sortedInfos.forEach { (t, u) ->
                        item(t) {
                            var expanded by mutableStateOf(false)

                            Card(
                                modifier = Modifier.clickable {
                                    expanded = !expanded
                                },
                                backgroundColor = MaterialTheme.colors.background,
                                elevation = 8.dp
                            ) {
                                val subInfo = subs.getActiveSubscriptionInfo(t)
                                val telephony = TelephonyManager.from(context).createForSubscriptionId(t)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .animateItemPlacement(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
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
                                        Text(text = "R-PLMN: ${StringBuilder(properInfo.safeRegisteredPlmn).insert(3, "-")}")
                                        Text(text = "Type: ${telephony.networkTypeName}")
                                        Text(text = "CA: ${telephony.serviceState.isUsingCarrierAggregation}")
                                        Text(text = "NR: ${NetworkRegistrationInfo.nrStateToString(telephony.serviceState.nrState)}/" +
                                                ServiceState.frequencyRangeToString(telephony.serviceState.nrFrequencyRange)
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = expanded
                                    ) {
                                        Column {
                                            Spacer(Modifier.size(4.dp))

                                            Divider()

                                            Spacer(Modifier.size(4.dp))

                                            AdvancedSubInfo(telephony = telephony, subs = subs)
                                        }
                                    }
                                }
                            }
                        }

                        items(u.size, { "$t:${u[it]}" }) {
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
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}