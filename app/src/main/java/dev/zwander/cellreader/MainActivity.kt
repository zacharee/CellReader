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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.layout.*
import dev.zwander.cellreader.ui.theme.CellReaderTheme
import dev.zwander.cellreader.utils.*


class MainActivity : ComponentActivity() {
    private val permReq =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
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

    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }

    CellReaderTheme {
        SelectionContainer {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sortedInfos.forEach { (t, u) ->
                        val (cellInfos, signalStrengths) = u

                        val subInfo = subs.getActiveSubscriptionInfo(t)
                        val telephony =
                            TelephonyManager.from(context).createForSubscriptionId(t)

                        item(t) {
                            var expanded by remember {
                                mutableStateOf(false)
                            }

                            Box(
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                SIMCard(
                                    telephony = telephony,
                                    subs = subs,
                                    subInfo = subInfo,
                                    expanded = expanded,
                                    onExpand = { expanded = it },
                                    showingCells = showingCells[t] ?: true,
                                    onShowingCells = { showingCells[t] = it }
                                )
                            }
                        }

                        items(cellInfos.size, { "$t:${cellInfos[it].cellIdentity}" }) {
                            var expanded by remember {
                                mutableStateOf(false)
                            }
                            val info = cellInfos[it]

                            AnimatedVisibility(
                                visible = showingCells[t] != false,
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                Box {
                                    SignalCard(
                                        cellInfo = info,
                                        expanded = expanded,
                                        isFinal = it == cellInfos.lastIndex && signalStrengths.isEmpty(),
                                        onExpand = { expanded = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }

                        items(signalStrengths.size, { "$t:$it" }) {
                            val info = signalStrengths[it]

                            AnimatedVisibility(
                                visible = showingCells[t] != false,
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                Box {
                                    SignalStrength(
                                        cellSignalStrength = info,
                                        isFinal = it == signalStrengths.lastIndex,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
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