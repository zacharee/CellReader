package dev.zwander.cellreader.data.layouts.cellIdentity

import android.telephony.CellIdentityCdma
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.util.FormatText
import dev.zwander.cellreader.data.util.onAvail
import dev.zwander.cellreader.data.wrappers.CellIdentityCdmaWrapper

@Suppress("UNUSED_PARAMETER")
@Composable
fun CellIdentityCdmaWrapper.CellIdentityCdma(
    simple: Boolean,
    advanced: Boolean
) {
    if (advanced) {
        longitude.onAvail {
            FormatText(
                R.string.lat_lon_format,
                "${latitude}/${longitude}"
            )
        }
        networkId.onAvail {
            FormatText(R.string.cdma_network_id_format, it.toString())
        }
        basestationId.onAvail {
            FormatText(R.string.basestation_id_format, it.toString())
        }
        systemId.onAvail {
            FormatText(R.string.cdma_system_id_format, it.toString())
        }
    }
}