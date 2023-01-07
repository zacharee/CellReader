package dev.zwander.cellreader.ui.components.bardialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zwander.cellreader.BuildConfig
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.LinkIcon
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.data.BottomBarLinkInfo

@Composable
fun About() {
    val links = remember {
        listOf(
            BottomBarLinkInfo(
                R.drawable.website,
                R.string.website,
                "https://zwander.dev"
            ),
            BottomBarLinkInfo(
                R.drawable.github,
                R.string.github,
                "https://github.com/zacharee"
            ),
            BottomBarLinkInfo(
                R.drawable.patreon,
                R.string.patreon,
                "https://patreon.com/zacharywander"
            ),
            BottomBarLinkInfo(
                R.drawable.twitter,
                R.string.twitter,
                "https://twitter.com/Wander1236"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
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

        Spacer(Modifier.size(16.dp))

        Row {
            links.forEach { linkInfo ->
                LinkIcon(
                    icon = painterResource(id = linkInfo.iconRes),
                    link = linkInfo.link,
                    desc = stringResource(id = linkInfo.descRes)
                )
            }
        }
    }
}