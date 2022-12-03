package dev.zwander.cellreader.ui.components.bardialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.zwander.cellreader.data.components.WearSafeText
import tk.zwander.patreonsupportersretrieval.data.SupporterInfo
import tk.zwander.patreonsupportersretrieval.util.DataParser
import tk.zwander.patreonsupportersretrieval.util.launchUrl

@Composable
fun Supporters() {
    val context = LocalContext.current

    val supporters = remember {
        mutableStateListOf<SupporterInfo>()
    }

    LaunchedEffect(key1 = null) {
        supporters.clear()
        supporters.addAll(DataParser.getInstance(context).parseSupporters())
    }

    LazyColumn(
        modifier = Modifier.heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        itemsIndexed(supporters, { _, item -> item.hashCode() }) { _, item ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable {
                            context.launchUrl(item.link)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WearSafeText(text = item.name)
                }
            }
        }
    }
}