package dev.zwander.cellreader.ui.components.bardialogs

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.data.SettingsItemData
import dev.zwander.cellreader.data.util.PrefManager
import dev.zwander.cellreader.data.util.preferences
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val settings = listOf(
    SettingsItemData(
        nameRes = dev.zwander.cellreader.data.R.string.send_data_to_wear,
        key = PrefManager.SEND_TO_WEAR,
        default = true
    )
)

@Composable
fun Settings() {
    val context = LocalContext.current
    val prefs = context.preferences

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(settings.size, key = { settings[it].key.name }) {
            val setting = settings[it]

            val checked by with(prefs) {
                context.store.data.map {
                    it[setting.key] ?: false
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
                    nameRes = dev.zwander.cellreader.data.R.string.background_location,
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
            .heightIn(min = 48.dp),
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
