package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.zwander.cellreader.data.ProvideCellModel
import dev.zwander.cellreader.ui.components.Expander
import dev.zwander.cellreader.ui.layouts.CellSignalStrengthCard
import dev.zwander.cellreader.ui.layouts.cellsignalstrength.CellSignalStrength
import dev.zwander.cellreader.ui.layouts.SIMCard
import dev.zwander.cellreader.ui.layouts.SignalCard
import dev.zwander.cellreader.ui.theme.CellReaderTheme
import dev.zwander.cellreader.utils.PermissionUtils
import dev.zwander.cellreader.utils.cellIdentityCompat
import tk.zwander.patreonsupportersretrieval.data.SupporterInfo
import tk.zwander.patreonsupportersretrieval.util.DataParser
import tk.zwander.patreonsupportersretrieval.util.launchUrl


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
    CellReaderTheme {
        ProvideWindowInsets {
            Scaffold(
                content = { scaffold ->
                    MainContent(scaffold = scaffold)
                },
                bottomBar = {
                    var optionsExpanded by remember {
                        mutableStateOf(false)
                    }
                    var whichDialog by remember {
                        mutableStateOf(-1)
                    }

                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colors.primarySurface)
                            .padding(
                                rememberInsetsPaddingValues(
                                    insets = LocalWindowInsets.current.systemBars,
                                    applyTop = false,
                                    applyBottom = true,
                                )
                            )
//                            .animateContentSize()
                    ) {
                        Expander(expanded = !optionsExpanded, onExpand = {
                            optionsExpanded = !it
                            if (!optionsExpanded) {
                                whichDialog = -1
                            }
                        })

                        AnimatedVisibility(visible = optionsExpanded) {
                            BottomAppBar(
                                backgroundColor = Color.Transparent,
                                elevation = 0.dp,
                            ) {
                                Spacer(Modifier.weight(1f))

                                IconButton(
                                    onClick = {
                                        whichDialog = if (whichDialog == 0) -1 else 0
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.heart),
                                        contentDescription = stringResource(id = R.string.supporters)
                                    )
                                }

                                Spacer(Modifier.weight(1f))
                            }
                        }

                        val context = LocalContext.current

                        AnimatedVisibility(visible = whichDialog != -1) {
                            Crossfade(targetState = whichDialog) {
                                when (it) {
                                    0 -> {
                                        val supporters = remember {
                                            mutableStateListOf<SupporterInfo>()
                                        }

                                        LaunchedEffect(key1 = null) {
                                            supporters.clear()
                                            supporters.addAll(DataParser.getInstance(context).parseSupporters())
                                        }

                                        LazyColumn(
                                            modifier = Modifier.heightIn(max = 300.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(8.dp)
                                        ) {
                                            itemsIndexed(supporters, { _, item -> item.hashCode() }) { _, item ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .heightIn(min = 48.dp)
                                                            .clickable {
                                                                context.launchUrl(item.link)
                                                            }
                                                            .padding(8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = item.name)
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
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(scaffold: PaddingValues) {
    val showingCells = remember {
        mutableStateMapOf<Int, Boolean>()
    }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>()
    }

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
                        additionalStart = 8.dp + scaffold.calculateStartPadding(
                            LocalLayoutDirection.current
                        ),
                        additionalTop = 8.dp + scaffold.calculateTopPadding(),
                        additionalEnd = 8.dp + scaffold.calculateEndPadding(
                            LocalLayoutDirection.current
                        ),
                        additionalBottom = 8.dp + scaffold.calculateBottomPadding()
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
                                CellSignalStrengthCard(
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