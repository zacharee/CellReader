package dev.zwander.cellreader.ui.components.bardialogs

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.data.CellSignalInfo
import dev.zwander.cellreader.data.data.ReorderSettingsItemData
import dev.zwander.cellreader.data.data.SettingsItemData
import dev.zwander.cellreader.data.util.PrefManager
import dev.zwander.cellreader.data.util.preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

val settings = listOf(
    SettingsItemData(
        nameRes = R.string.send_data_to_wear,
        key = PrefManager.SEND_TO_WEAR,
        default = true
    )
)

val reorderSettings = listOf(
    ReorderSettingsItemData(
        nameRes = R.string.cdma_signal_strength,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Strength.CDMA) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Strength.CDMA) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.gsm_signal_strength,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Strength.GSM) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Strength.GSM) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.lte_signal_strength,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Strength.LTE) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Strength.LTE) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.nr_signal_strength,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Strength.NR) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Strength.NR) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.tdscdma_signal_strength,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Strength.TDSCDMA) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Strength.TDSCDMA) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.wcdma_signal_strength,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Strength.WCDMA) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Strength.WCDMA) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.cdma_info,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Identity.CDMA) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Identity.CDMA) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.gsm_info,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Identity.GSM) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Identity.GSM) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.lte_info,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Identity.LTE) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Identity.LTE) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.nr_info,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Identity.NR) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Identity.NR) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.tdscdma_info,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Identity.TDSCDMA) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Identity.TDSCDMA) {
                updateOrder(it)
            }
        }
    ),
    ReorderSettingsItemData(
        nameRes = R.string.wcdma_info,
        initialValue = {
            with (CellSignalInfo.Orderer.Order.Identity.WCDMA) {
                order
            }
        },
        onSave = {
            with (CellSignalInfo.Orderer.Order.Identity.WCDMA) {
                updateOrder(it)
            }
        }
    ),
)

@Composable
fun Settings() {
    val context = LocalContext.current
    val prefs = context.preferences

    val scope = rememberCoroutineScope()

    var selectedReorderSetting by remember {
        mutableStateOf<ReorderSettingsItemData?>(null)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(settings, key = { it.key.name }) { setting ->
            val checked by with(prefs) {
                context.store.data.map { p ->
                    p[setting.key] ?: false
                }
            }.collectAsState(
                initial = false,
                context = scope.coroutineContext
            )

            SwitchGuts(
                nameRes = setting.nameRes,
                checked = checked,
                enabled = true,
                onCheckedChange = {
                    scope.launch {
                        prefs.updateSendToWear(it)
                    }
                }
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            item(key = "background_location" ) {
                var checked by remember {
                    mutableStateOf(
                        context.checkCallingOrSelfPermission(
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) {
                    checked = context.checkCallingOrSelfPermission(
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                }

                SwitchGuts(
                    nameRes = R.string.background_location,
                    checked = checked,
                    enabled = !checked,
                    onCheckedChange = {
                        if (it) {
                            permissionResultLauncher.launch(
                                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        }
                    }
                )
            }
        }

        item(key = "reorder_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 32.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(id = R.string.reorder),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        items(reorderSettings, key = { it.nameRes }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable {
                        selectedReorderSetting = it
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = it.nameRes),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    selectedReorderSetting?.let { setting ->
        ReorderDialog(
            setting = setting,
            onDismissRequest = { selectedReorderSetting = null }
        )
    }
}

@Composable
private fun SwitchGuts(
    @StringRes
    nameRes: Int,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = nameRes)
        )

        Spacer(Modifier.weight(1f))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun ReorderDialog(
    setting: ReorderSettingsItemData,
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentOrder = remember {
        mutableStateListOf<CellSignalInfo.Keys<*>>()
    }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        currentOrder.add(to.index, currentOrder.removeAt(from.index))
    })

    val initialValue by setting.initialValue(context).collectAsState(initial = listOf())

    var infoDialogText by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(key1 = setting.nameRes, key2 = initialValue.toList()) {
        withContext(Dispatchers.IO) {
            currentOrder.clear()
            currentOrder.addAll(initialValue)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = setting.nameRes))
        },
        text = {
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .reorderable(state)
                    .detectReorderAfterLongPress(state)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentOrder, { it.key }) {
                    ReorderableItem(
                        reorderableState = state,
                        key = it.key
                    ) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)
                        val isAdvancedSeparator = it is CellSignalInfo.Keys.AdvancedSeparator

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.outlinedCardElevation()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = if (isAdvancedSeparator) 32.dp else 48.dp)
                                    .background(
                                        if (isAdvancedSeparator) {
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = it.label),
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    fontWeight = if (isAdvancedSeparator) FontWeight.Bold else FontWeight.Normal
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                if (!isAdvancedSeparator) {
                                    val helpText = it.retrieveHelpText(info = it.retrieveHelpText(info = null))

                                    IconButton(onClick = { infoDialogText = helpText }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_help_outline_24),
                                            contentDescription = stringResource(id = R.string.info)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        if (currentOrder.isNotEmpty()) {
                            setting.onSave(context, currentOrder.toList())
                        }
                        onDismissRequest()
                    }
                }
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier.fillMaxWidth(0.7f)
    )

    infoDialogText?.let {
        AlertDialog(
            onDismissRequest = { infoDialogText = null },
            title = {
                Text(text = stringResource(id = R.string.info))
            },
            text = {
                Text(text = it)
            },
            confirmButton = {
                TextButton(onClick = { infoDialogText = null }) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}
