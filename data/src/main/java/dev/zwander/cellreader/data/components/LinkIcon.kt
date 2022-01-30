package dev.zwander.cellreader.data.components

import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import tk.zwander.patreonsupportersretrieval.util.launchUrl

@Composable
fun LinkIcon(
    icon: Painter,
    link: String,
    modifier: Modifier = Modifier,
    desc: String? = null,
) {
    val context = LocalContext.current

    WearSafeIconButton(
        onClick = { context.launchUrl(link) },
        modifier = modifier
    ) {
        WearSafeIcon(
            painter = icon,
            contentDescription = desc
        )
    }
}