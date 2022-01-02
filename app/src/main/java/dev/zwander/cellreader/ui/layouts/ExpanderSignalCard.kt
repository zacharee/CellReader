package dev.zwander.cellreader.ui.layouts

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.ui.components.*
import dev.zwander.cellreader.utils.angledGradient
import dev.zwander.cellreader.utils.anticipateDecelerateInterpolator

@Composable
fun ExpanderSignalCard(
    isFinal: Boolean,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    level: Int,
    dBm: Int,
    colors: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    basicInfo: (@Composable() () -> Unit)? = null,
    expandedInfo: (@Composable() () -> Unit)? = null,
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

            Card(
                backgroundColor = Color.Transparent,
                elevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .angledGradient(
                            colors,
                            87f
                        )
                        .padding(
                            start = 8.dp,
                            top = 8.dp,
                            end = 8.dp,
                            bottom = if (expandedInfo != null) 0.dp else 8.dp
                        )
                        .fillMaxWidth(),
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LevelIndicator(level, dBm)

                            Spacer(Modifier.size(8.dp))

                            FlowRow(
                                mainAxisSpacing = 16.dp,
                                mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                                mainAxisSize = SizeMode.Expand
                            ) {
                                basicInfo?.invoke()
                            }
                        }

                        expandedInfo?.let {
                            AnimatedVisibility(
                                visible = expanded,
                                enter = fadeIn() + expandVertically(
                                    animationSpec = tween(
                                        durationMillis = 400,
                                        easing = {
                                            anticipateDecelerateInterpolator(it)
                                        }
                                    )
                                ),
                                exit = fadeOut() + shrinkVertically(
                                    animationSpec = tween(
                                        durationMillis = 400,
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
                                        mainAxisSize = SizeMode.Expand
                                    ) {
                                        expandedInfo()
                                    }
                                }
                            }

                            Expander(
                                expanded = expanded,
                                onExpand = onExpand,
                                modifier = Modifier.fillMaxWidth()
                                    .height(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}