package dev.zwander.cellreader.ui.components

import android.content.res.Configuration
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.*
import dev.zwander.cellreader.data.data.BottomDialogPage
import dev.zwander.cellreader.data.data.DialogButtonInfo
import dev.zwander.cellreader.data.data.GraphInfo
import dev.zwander.cellreader.data.util.populatePoints
import dev.zwander.cellreader.ui.components.bardialogs.About
import dev.zwander.cellreader.ui.components.bardialogs.Graph
import dev.zwander.cellreader.ui.components.bardialogs.Settings
import dev.zwander.cellreader.ui.components.bardialogs.Supporters
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun BoxScope.BottomBarScrimContainer() {
    var expanded by remember {
        mutableStateOf(false)
    }

    var scrimFullSize by remember {
        mutableStateOf(false)
    }
    var whichDialog by remember {
        mutableStateOf<BottomDialogPage?>(null)
    }

    val showFullSize by remember(scrimFullSize, expanded) {
        derivedStateOf {
            scrimFullSize || expanded
        }
    }

    val bg by animateColorAsState(
        if (expanded) {
            Color.Black.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        }
    ) {
        scrimFullSize = it != Color.Transparent
    }

    val backPressedCallback = remember {
        object : OnBackPressedCallback(expanded || whichDialog != null) {
            override fun handleOnBackPressed() {
                if (whichDialog != null) {
                    whichDialog = null
                } else if (expanded) {
                    expanded = false
                }
            }
        }
    }

    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher?.addCallback(LocalLifecycleOwner.current, backPressedCallback)

    LaunchedEffect(key1 = expanded, key2 = whichDialog) {
        backPressedCallback.isEnabled = expanded || whichDialog != null
    }

    Box(
        modifier = Modifier
            .background(bg)
            .align(Alignment.BottomCenter)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    whichDialog = null
                    expanded = false
                }
            )
            .then(
                if (showFullSize) Modifier.fillMaxSize()
                else Modifier.size(0.dp)
            ),
    )

    Box(
        modifier = Modifier
            .padding(
                WindowInsets.systemBars
                    .only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                    .asPaddingValues()
            )
            .align(Alignment.BottomCenter)
    ) {
        BottomBar(
            optionsExpanded = expanded,
            expandStateChanged = { expanded = it },
            whichDialog = whichDialog,
            onDialogChange = { whichDialog = it }
        )
    }
}

@Composable
private fun BoxScope.BottomBar(
    optionsExpanded: Boolean,
    expandStateChanged: (Boolean) -> Unit,
    whichDialog: BottomDialogPage?,
    onDialogChange: (BottomDialogPage?) -> Unit
) {
    val dialogButtons = remember {
        listOf(
            listOf(
                DialogButtonInfo(
                    BottomDialogPage.GRAPH,
                    R.drawable.chart,
                    R.string.signal_graph
                ),
            ),
            listOf(
                DialogButtonInfo(
                    BottomDialogPage.SUPPORTERS,
                    R.drawable.heart,
                    R.string.supporters
                ),
                DialogButtonInfo(
                    BottomDialogPage.ABOUT,
                    R.drawable.about,
                    R.string.about
                ),
                DialogButtonInfo(
                    BottomDialogPage.SETTINGS,
                    R.drawable.settings,
                    R.string.settings
                )
            )
        )
    }

    val points = remember {
        mutableStateMapOf<Int, GraphInfo>()
    }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        var currentX = 0

        while (isActive) {
            populatePoints(points, context, currentX)
            currentX++
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.large.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                )
            )
            .padding(
                WindowInsets.systemBars
                    .only(
                        WindowInsetsSides.Bottom
                    )
                    .add(
                        if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) WindowInsets.systemBars.only(
                            WindowInsetsSides.End + WindowInsetsSides.Start
                        ) else WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                    )
                    .asPaddingValues()
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .align(Alignment.BottomCenter)
    ) {
        Expander(
            expanded = !optionsExpanded,
            onExpand = {
                if (it) {
                    onDialogChange(null)
                }
                expandStateChanged(!it)
            },
            modifier = Modifier.height(24.dp)
        )

        AnimatedVisibility(visible = optionsExpanded) {
            BottomAppBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
            ) {
                Spacer(Modifier.size(16.dp))

                dialogButtons.forEachIndexed { index, section ->
                    section.forEach { buttonInfo ->
                        WearSafeIconButton(
                            onClick = {
                                onDialogChange(
                                    if (whichDialog == buttonInfo.whichDialog) null else buttonInfo.whichDialog
                                )
                            }
                        ) {
                            WearSafeIcon(
                                painter = painterResource(id = buttonInfo.iconRes),
                                contentDescription = stringResource(id = buttonInfo.descRes),
                                tint = if (whichDialog == buttonInfo.whichDialog) {
                                    null
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.7f
                                    )
                                }
                            )
                        }
                    }

                    if (index < dialogButtons.lastIndex) {
                        Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.size(16.dp))
            }
        }

        val dialogs = remember {
            mapOf<BottomDialogPage, @Composable () -> Unit>(
                BottomDialogPage.SUPPORTERS to { Supporters() },
                BottomDialogPage.ABOUT to { About() },
                BottomDialogPage.GRAPH to { Graph(points) },
                BottomDialogPage.SETTINGS to { Settings() }
            )
        }

        Box(
            modifier = Modifier.animateContentSize()
        ) {
            dialogs.forEach { (which, func) ->
                androidx.compose.animation.AnimatedVisibility(
                    visible = whichDialog == which,
                    enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopStart),
                    exit = fadeOut() + shrinkOut(clip = false, shrinkTowards = Alignment.TopStart)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        func()
                    }
                }
            }
        }
    }
}
