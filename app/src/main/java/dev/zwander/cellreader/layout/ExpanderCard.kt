package dev.zwander.cellreader.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.R
import dev.zwander.cellreader.utils.angledGradient
import kotlin.math.exp

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
//                        .clickable {
//                            onExpand(!expanded)
//                        }
                        .padding(8.dp)
                        .fillMaxWidth(),
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LevelIndicator(level, dBm)

                            Spacer(Modifier.size(16.dp))

                            FlowRow(
                                mainAxisSpacing = 16.dp,
                                mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
                                mainAxisSize = SizeMode.Expand
                            ) {
                                basicInfo?.invoke()
                            }
                        }

                        expandedInfo?.let {
                            Expander(
                                expanded = expanded,
                                onExpand = onExpand,
                                modifier = Modifier.fillMaxWidth()
                                    .height(24.dp)
                            )

                            AnimatedVisibility(visible = expanded) {
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
                        }
                    }
                }
            }
        }
    }
}