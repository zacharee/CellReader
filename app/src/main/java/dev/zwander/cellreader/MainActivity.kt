package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.zwander.cellreader.data.ProvideCellModel
import dev.zwander.cellreader.ui.layouts.CellSignalStrength
import dev.zwander.cellreader.ui.layouts.SIMCard
import dev.zwander.cellreader.ui.layouts.SignalCard
import dev.zwander.cellreader.ui.theme.CellReaderTheme
import dev.zwander.cellreader.utils.PermissionUtils
import dev.zwander.cellreader.utils.cellIdentityCompat


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

        WindowCompat.setDecorFitsSystemWindows(window, false)

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
            val sysUiController = rememberSystemUiController()
            sysUiController.setStatusBarColor(Color.Transparent)
            sysUiController.setNavigationBarColor(Color.Transparent)

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
        ProvideWindowInsets {
            ProvideCellModel {
                SelectionContainer {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        val state = rememberLazyListState()

                        LazyColumn(
                            contentPadding = rememberInsetsPaddingValues(
                                insets = LocalWindowInsets.current.systemBars,
                                applyTop = true,
                                applyBottom = true,
                                additionalStart = 8.dp,
                                additionalTop = 8.dp,
                                additionalEnd = 8.dp,
                                additionalBottom = 8.dp
                            ),
                            state = state,
                        ) {
                            sortedSubIds.forEach { t ->
                                item(t) {
                                    SIMCard(
                                        telephony = telephonies[t]!!,
                                        subInfo = subInfos[t],
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

                                itemsIndexed(cellInfos[t]!!, { _, item -> "$t:${item.cellIdentityCompat}" }) { index, item ->
                                    AnimatedVisibility(
                                        visible = showingCells[t] != false,
                                        modifier = Modifier
                                            .animateItemPlacement()
                                            .padding(bottom = 8.dp),
                                        enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopEnd),
                                        exit = shrinkOut(clip = false, shrinkTowards = Alignment.TopEnd) + fadeOut()
                                    ) {
                                        val key = remember(item.cellIdentityCompat) {
                                            "$t:${item.cellIdentityCompat}"
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
                                        CellSignalStrength(
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
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}