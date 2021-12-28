package dev.zwander.cellreader.utils

import android.telephony.*
import kotlin.math.absoluteValue

object CellUtils {
    fun connectionStatusToString(connectionStatus: Int): String {
        return when (connectionStatus) {
            CellInfo.CONNECTION_NONE -> "None"
            CellInfo.CONNECTION_SECONDARY_SERVING -> "Secondary Serving"
            CellInfo.CONNECTION_PRIMARY_SERVING -> "Primary Serving"
            else -> "Unknown"
        }
    }

    class CellInfoComparator : Comparator<CellInfo> {
        override fun compare(o1: CellInfo, o2: CellInfo): Int {
            val statusResult = if (o1.cellConnectionStatus != CellInfo.CONNECTION_NONE && o2.cellConnectionStatus != CellInfo.CONNECTION_NONE
                && o1.cellConnectionStatus != CellInfo.CONNECTION_UNKNOWN && o2.cellConnectionStatus != CellInfo.CONNECTION_UNKNOWN) {
                o1.cellConnectionStatus - o2.cellConnectionStatus
            } else {
                0
            }

            if (statusResult != 0) {
                return statusResult
            }

            if (o1.cellConnectionStatus == CellInfo.CONNECTION_NONE && o2.cellConnectionStatus != CellInfo.CONNECTION_NONE) {
                return 1
            }

            if (o1.cellConnectionStatus != CellInfo.CONNECTION_NONE && o2.cellConnectionStatus == CellInfo.CONNECTION_NONE) {
                return -1
            }

            if (o1.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN && o2.cellConnectionStatus != CellInfo.CONNECTION_UNKNOWN) {
                return 1
            }

            if (o2.cellConnectionStatus != CellInfo.CONNECTION_UNKNOWN && o2.cellConnectionStatus == CellInfo.CONNECTION_UNKNOWN) {
                return -1
            }

            if (o1.isRegistered && !o2.isRegistered) {
                return -1
            }

            if (!o1.isRegistered && o2.isRegistered) {
                return 1
            }

            fun getTypeRanking(info: CellInfo): Int {
                return when (info) {
                    is CellInfoGsm -> 0
                    is CellInfoCdma -> 0
                    is CellInfoTdscdma -> 1
                    is CellInfoWcdma -> 2
                    is CellInfoLte -> 3
                    is CellInfoNr -> 4
                    else -> 0
                }
            }

            val rankResult = getTypeRanking(o2) - getTypeRanking(o1)

            if (rankResult != 0) {
                return rankResult
            }

            return o1.cellSignalStrength.dbm.absoluteValue - o2.cellSignalStrength.dbm.absoluteValue
        }
    }
}

val CellConfigLte.endcAvailable: Boolean
    get() = this::class.java
        .getDeclaredMethod("isEndcAvailable")
        .apply { isAccessible = true }
        .invoke(this) as Boolean

val NetworkRegistrationInfo.safeRegisteredPlmn: String
    get() = when {
        registeredPlmn.isNullOrBlank() -> "000000"
        registeredPlmn.length < 3 -> StringBuilder(registeredPlmn).run {
            val makeup = 6 - registeredPlmn.length

            appendRange("000000", 0, makeup + 1)
            toString()
        }
        else -> registeredPlmn
    }

inline fun <reified T : CellInfo> CellInfo.cast() = castGeneric<T>()
inline fun <reified T : CellIdentity> CellIdentity.cast() = castGeneric<T>()
inline fun <reified T : CellSignalStrength> CellSignalStrength.cast() = castGeneric<T>()

inline fun <reified T : Any> Any.castGeneric(): T? {
    return if (this is T) this
            else null
}