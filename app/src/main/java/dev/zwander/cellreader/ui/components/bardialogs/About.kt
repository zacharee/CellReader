package dev.zwander.cellreader.ui.components.bardialogs

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zwander.cellreader.BuildConfig
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText

@Composable
fun About() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WearSafeText(
            text = stringResource(id = R.string.app_name),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.size(4.dp))

        WearSafeText(
            text = "v${BuildConfig.VERSION_NAME}"
        )
    }
}