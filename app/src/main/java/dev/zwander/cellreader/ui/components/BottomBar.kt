package dev.zwander.cellreader.ui.components

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.*
import dev.zwander.cellreader.data.data.BottomBarLinkInfo
import dev.zwander.cellreader.data.data.BottomDialogPage
import dev.zwander.cellreader.data.data.DialogButtonInfo
import dev.zwander.cellreader.ui.components.bardialogs.About
import dev.zwander.cellreader.ui.components.bardialogs.Graph
import dev.zwander.cellreader.ui.components.bardialogs.Supporters

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

    val showFullSize by derivedStateOf {
        scrimFullSize || expanded
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

    BottomBar(
        optionsExpanded = expanded,
        expandStateChanged = { expanded = it },
        whichDialog = whichDialog,
        onDialogChange = { whichDialog = it }
    )
}

@Composable
private fun BoxScope.BottomBar(
    optionsExpanded: Boolean,
    expandStateChanged: (Boolean) -> Unit,
    whichDialog: BottomDialogPage?,
    onDialogChange: (BottomDialogPage?) -> Unit
) {
    val context = LocalContext.current

    val dialogButtons = remember {
        listOf(
            DialogButtonInfo(
                BottomDialogPage.GRAPH,
                R.drawable.chart,
                R.string.signal_graph
            ),
            DialogButtonInfo(
                BottomDialogPage.SUPPORTERS,
                R.drawable.heart,
                R.string.supporters
            ),
            DialogButtonInfo(
                BottomDialogPage.ABOUT,
                R.drawable.about,
                R.string.about
            )
        )
    }

    val links = remember {
        listOf(
            BottomBarLinkInfo(
                R.drawable.website,
                R.string.website,
                "https://zwander.dev"
            ),
            BottomBarLinkInfo(
                R.drawable.github,
                R.string.github,
                "https://github.com/zacharee"
            ),
            BottomBarLinkInfo(
                R.drawable.patreon,
                R.string.patreon,
                "https://patreon.com/zacharywander"
            )
        )
    }

    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colors.primarySurface,
                MaterialTheme.shapes.large.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                )
            )
            .padding(
                rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyTop = false,
                    applyBottom = true,
                )
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
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
            ) {
                Spacer(Modifier.weight(1f))

                links.forEach { linkInfo ->
                    LinkIcon(
                        icon = painterResource(id = linkInfo.iconRes),
                        link = linkInfo.link,
                        desc = stringResource(id = linkInfo.descRes)
                    )
                }

                Spacer(Modifier.size(16.dp))

                dialogButtons.forEach { buttonInfo ->
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
                            tint = if (whichDialog == buttonInfo.whichDialog) Color.White else null
                        )
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        }

        val dialogs = remember {
            mapOf<BottomDialogPage, @Composable()() -> Unit>(
                BottomDialogPage.SUPPORTERS to { Supporters() },
                BottomDialogPage.ABOUT to { About() },
                BottomDialogPage.GRAPH to { Graph() }
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
                    func()
                }
            }
        }
    }
}

