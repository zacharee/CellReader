package dev.zwander.cellreader.data.layouts

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import dev.zwander.cellreader.data.LocalAnimationDuration
import dev.zwander.cellreader.data.components.*
import dev.zwander.cellreader.data.util.angledGradient
import dev.zwander.cellreader.data.util.anticipateDecelerateInterpolator

@Composable
fun ExpanderSignalCard(
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    level: Int,
    dBm: Int,
    type: String,
    colors: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    basicInfo: @Composable() (() -> Unit)? = null,
    expandedInfo: @Composable() (() -> Unit)? = null
) {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }

    Box(
        modifier = modifier,
    ) {
        IndenticatorLine(size = size, isFinal = isFinal)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.onSizeChanged {
                size = it
            }
        ) {
            IndenticatorArrow()

            @Composable
            fun contents() {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .angledGradient(
                            colors,
                            87f
                        )
                        .fillMaxWidth(),
                ) {
                    Column {
                        Column(
                            modifier = Modifier.padding(
                                start = 8.dp,
                                top = 8.dp,
                                end = 8.dp,
                                bottom = if (expandedInfo != null) 0.dp else 8.dp
                            )
                        ) {
                            HybridFlowRow(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                mainAxisSpacing = 8.dp,
                                mainAxisAlignment = FlowMainAxisAlignment.Center,
                                crossAxisAlignment = FlowCrossAxisAlignment.Center
                            ) {
                                LevelIndicator(level, dBm, type)

                                FlowRow(
                                    mainAxisSpacing = 16.dp,
                                    mainAxisAlignment = FlowMainAxisAlignment.SpaceAround,
                                    mainAxisSize = com.google.accompanist.flowlayout.SizeMode.Expand
                                ) {
                                    basicInfo?.invoke()
                                }
                            }

                            expandedInfo?.let {
                                AnimatedVisibility(
                                    visible = expanded,
                                    enter = fadeIn() + expandVertically(
                                        animationSpec = tween(
                                            durationMillis = LocalAnimationDuration.current,
                                            easing = {
                                                anticipateDecelerateInterpolator(it)
                                            }
                                        )
                                    ),
                                    exit = fadeOut() + shrinkVertically(
                                        animationSpec = tween(
                                            durationMillis = LocalAnimationDuration.current,
                                            easing = {
                                                anticipateDecelerateInterpolator(it)
                                            }
                                        )
                                    )
                                ) {
                                    Column {
                                        PaddedDivider()

                                        FlowRow(
                                            mainAxisSpacing = 16.dp,
                                            mainAxisAlignment = MainAxisAlignment.SpaceBetween,
                                            mainAxisSize = com.google.accompanist.flowlayout.SizeMode.Expand
                                        ) {
                                            expandedInfo()
                                        }
                                    }
                                }
                            }
                        }

                        expandedInfo?.let {
                            Expander(
                                expanded = expanded,
                                onExpand = onExpand,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                            )
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.elevatedCardElevation()
            ) {
                contents()
            }
        }
    }
}