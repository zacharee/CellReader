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
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
                    sortedSubIds.forEach { t ->
                        item(t) {
                            val subInfo = remember(t) {
                                subs.getActiveSubscriptionInfo(t)
                            }
                            val telephony = remember(t) {
                                TelephonyManager.from(context).createForSubscriptionId(t)
                            }

                            var expanded by remember {
                                mutableStateOf(false)
                            }

                            SIMCard(
                                telephony = telephony,
                                subs = subs,
                                subInfo = subInfo,
                                expanded = expanded,
                                onExpand = { expanded = it },
                                showingCells = showingCells[t] ?: true,
                                onShowingCells = { showingCells[t] = it },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }

                        val lastCellIndex = cellInfos[t]!!.lastIndex
                        val lastStrengthIndex = strengthInfos[t]!!.lastIndex
                        val strengthsEmpty = strengthInfos[t]!!.isEmpty()

                        itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                            var expanded by remember {
                                mutableStateOf(false)
                            }

                            AnimatedVisibility(
                                visible = showingCells[t] != false,
                                modifier = Modifier.animateItemPlacement(),
                                enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                                exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                            ) {
                                SignalCard(
                                    cellInfo = item,
                                    expanded = expanded,
                                    isFinal = index == lastCellIndex && strengthsEmpty,
                                    onExpand = { expanded = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }

                        itemsIndexed(strengthInfos[t]!!, { index, _ -> "$t:$index" }) { index, item ->
                            AnimatedVisibility(
                                visible = showingCells[t] != false,
                                modifier = Modifier.animateItemPlacement(),
                                enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                                exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                            ) {
                                SignalStrength(
                                    cellSignalStrength = item,
                                    isFinal = index == lastStrengthIndex,
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}