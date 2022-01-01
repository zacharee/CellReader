package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

    CellReaderTheme {
        SelectionContainer {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                ) {
                    sortedSubIds.forEach { t ->
                        item(t) {
                            SIMCard(
                                telephony = telephonies[t]!!,
                                subInfo = subInfos[t]!!,
                                expanded = expanded[t.toString()] ?: false,
                                onExpand = { expanded[t.toString()] = it },
                                showingCells = showingCells[t] ?: true,
                                onShowingCells = { showingCells[t] = it },
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        val lastCellIndex = cellInfos[t]!!.lastIndex
                        val lastStrengthIndex = strengthInfos[t]!!.lastIndex
                        val strengthsEmpty = strengthInfos[t]!!.isEmpty()

                        itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentity}" }) { index, item ->
                            AnimatedVisibility(
                                visible = showingCells[t] != false,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .padding(bottom = 8.dp),
                                enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                                exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                            ) {
                                val key = remember(item.cellIdentity) {
                                    "$t:${item.cellIdentity}"
                                }

                                SignalCard(
                                    cellInfo = item,
                                    expanded = expanded[key] ?: false,
                                    isFinal = index == lastCellIndex && strengthsEmpty,
                                    onExpand = { expanded[key] = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }

                        itemsIndexed(strengthInfos[t]!!, { index, _ -> "$t:$index" }) { index, item ->
                            AnimatedVisibility(
                                visible = showingCells[t] != false,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .padding(bottom = 8.dp),
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