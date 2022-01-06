package dev.zwander.cellreader.ui.components

import androidx.compose.material.Icon
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

    IconButton(
        onClick = { context.launchUrl(link) },
        modifier = modifier
    ) {
        Icon(
            painter = icon,
            contentDescription = desc
        )
    }
}