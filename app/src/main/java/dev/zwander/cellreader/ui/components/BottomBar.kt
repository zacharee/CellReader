package dev.zwander.cellreader.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import dev.zwander.cellreader.BuildConfig
import dev.zwander.cellreader.R
import tk.zwander.patreonsupportersretrieval.data.SupporterInfo
import tk.zwander.patreonsupportersretrieval.util.DataParser
import tk.zwander.patreonsupportersretrieval.util.launchUrl

private enum class BottomDialogPage {
    SUPPORTERS,
    ABOUT
}

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

                LinkIcon(
                    icon = painterResource(id = R.drawable.website),
                    link = "https://zwander.dev",
                    desc = stringResource(id = R.string.website)
                )

                LinkIcon(
                    icon = painterResource(id = R.drawable.github),
                    link = "https://github.com/zacharee",
                    desc = stringResource(id = R.string.github)
                )

                LinkIcon(
                    icon = painterResource(id = R.drawable.patreon),
                    link = "https://patreon.com/zacharywander",
                    desc = stringResource(id = R.string.patreon)
                )

                Spacer(Modifier.size(16.dp))

                IconButton(
                    onClick = {
                        onDialogChange(
                            if (whichDialog == BottomDialogPage.SUPPORTERS) null else BottomDialogPage.SUPPORTERS
                        )
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = stringResource(id = R.string.supporters)
                    )
                }

                IconButton(
                    onClick = {
                        onDialogChange(
                            if (whichDialog == BottomDialogPage.ABOUT) null else BottomDialogPage.ABOUT
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.about),
                        contentDescription = stringResource(id = R.string.about)
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }

        val context = LocalContext.current

        Box(
            modifier = Modifier.animateContentSize()
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = whichDialog == BottomDialogPage.SUPPORTERS,
                enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopStart),
                exit = fadeOut() + shrinkOut(clip = false, shrinkTowards = Alignment.TopStart)
            ) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
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

            androidx.compose.animation.AnimatedVisibility(
                visible = whichDialog == BottomDialogPage.ABOUT,
                enter = fadeIn() + expandIn(clip = false, expandFrom = Alignment.TopStart),
                exit = fadeOut() + shrinkOut(clip = false, shrinkTowards = Alignment.TopStart)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.size(4.dp))

                    Text(
                        text = "v${BuildConfig.VERSION_NAME}"
                    )
                }
            }
        }
    }
}