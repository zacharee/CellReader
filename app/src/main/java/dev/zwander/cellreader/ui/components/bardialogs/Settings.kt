package dev.zwander.cellreader.ui.components.bardialogs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.data.SettingsItemData
import dev.zwander.cellreader.data.util.PrefManager
import dev.zwander.cellreader.data.util.preferences
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

            val checked by prefs.sendToWear.collectAsState(
                initial = false,
                context = scope.coroutineContext
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = setting.nameRes)
                )

                Spacer(Modifier.weight(1f))

                Switch(checked = checked, onCheckedChange = {
                    scope.launch {
                        prefs.updateSendToWear(it)
                    }
                })
            }
        }
    }
}