package dev.zwander.cellreader.data.layouts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.components.WearSafeText
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.wrappers.ServiceStateWrapper
import dev.zwander.cellreader.data.wrappers.TelephonyDisplayInfoWrapper

@Composable
fun DisplayInfo(info: TelephonyDisplayInfoWrapper) {
    val context = LocalContext.current

    with (info) {
        WearSafeText(
            text = stringResource(id = R.string.display_info),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        FormatText(
            R.string.network_type_format,
            ServiceStateWrapper.rilRadioTechnologyToString(context, ServiceStateWrapper.networkTypeToRilRadioTechnology(networkType))
        )

        FormatText(R.string.override_type_format, TelephonyDisplayInfoWrapper.overrideNetworkTypeToString(context, overrideNetworkType))

        isRoaming?.let {
            FormatText(R.string.roaming_format, it)
        }
    }
}