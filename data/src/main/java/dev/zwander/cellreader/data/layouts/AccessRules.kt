package dev.zwander.cellreader.data.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.wrappers.UiccAccessRuleWrapper

@Composable
fun AccessRules(
    accessRules: List<UiccAccessRuleWrapper>
) {
    WearSafeText(
        text = stringResource(id = R.string.access_rules),
        modifier = Modifier.fillMaxWidth()
    )

    accessRules.forEach { rule ->
        Card(
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            border = BorderStroke(1.dp, Color.White),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        ) {
            FlowRow(
                mainAxisSize = SizeMode.Expand,
                mainAxisAlignment = MainAxisAlignment.SpaceAround,
                mainAxisSpacing = 16.dp,
                modifier = Modifier.padding(8.dp)
            ) {
                FormatText(R.string.package_name_format, rule.packageName ?: "null")
                FormatText(R.string.access_type_format, rule.accessType.toString())
                FormatText(R.string.certificate_format, UiccAccessRuleWrapper.bytesToHexString(rule.certificateHash) ?: "")
            }
        }
    }
}