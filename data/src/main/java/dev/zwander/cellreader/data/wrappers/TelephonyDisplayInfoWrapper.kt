package dev.zwander.cellreader.data.wrappers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.telephony.Annotation.OverrideNetworkType
import android.telephony.CellInfo
import android.telephony.TelephonyDisplayInfo
import dev.zwander.cellreader.data.R

data class TelephonyDisplayInfoWrapper(
    val networkType: Int = CellInfo.TYPE_UNKNOWN,
    val overrideNetworkType: Int = CellInfo.TYPE_UNKNOWN,
    val isRoaming: Boolean?,
) {
    @SuppressLint("UseRequiresApi")
    @TargetApi(Build.VERSION_CODES.R)
    constructor(info: TelephonyDisplayInfo) : this(
        networkType = info.networkType,
        overrideNetworkType = info.overrideNetworkType,
        isRoaming = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) info.isRoaming else null,
    )

    companion object {
        fun overrideNetworkTypeToString(context: Context, @OverrideNetworkType type: Int): String {
            @Suppress("DEPRECATION")
            return context.resources.getString(
                when (type) {
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE -> R.string.none
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> R.string.lte_ca
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> R.string.lte_adv_pro
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> R.string.nr_nsa
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE -> R.string.nr_nsa_mmwave
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> R.string.nr_adv
                    else -> R.string.unknown
                }
            )
        }
    }
}