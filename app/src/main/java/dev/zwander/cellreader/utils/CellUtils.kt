package dev.zwander.cellreader.utils

import android.os.Build
import android.telephony.*
import android.telephony.NetworkRegistrationInfo.NRState
import androidx.compose.runtime.Composable
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

    fun nrStateToString(@NRState nrState: Int): String {
        return when (nrState) {
            NetworkRegistrationInfo.NR_STATE_RESTRICTED -> "RESTRICTED"
            NetworkRegistrationInfo.NR_STATE_NOT_RESTRICTED -> "NOT_RESTRICTED"
            NetworkRegistrationInfo.NR_STATE_CONNECTED -> "CONNECTED"
            else -> "NONE"
        }
    }

    fun frequencyRangeToString(@ServiceState.FrequencyRange range: Int): String {
        return when (range) {
            ServiceState.FREQUENCY_RANGE_UNKNOWN -> "UNKNOWN"
            ServiceState.FREQUENCY_RANGE_LOW -> "LOW"
            ServiceState.FREQUENCY_RANGE_MID -> "MID"
            ServiceState.FREQUENCY_RANGE_HIGH -> "HIGH"
            ServiceState.FREQUENCY_RANGE_MMWAVE -> "MMWAVE"
            else -> range.toString()
        }
    }

    object CellInfoComparator : Comparator<CellInfo> {
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
                return when {
                    info is CellInfoGsm -> 0
                    info is CellInfoCdma -> 0
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoTdscdma -> 1
                    info is CellInfoWcdma -> 2
                    info is CellInfoLte -> 3
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr -> 4
                    else -> 0
                }
            }

            val rankResult = getTypeRanking(o2) - getTypeRanking(o1)

            if (rankResult != 0) {
                return rankResult
            }

            return CellSignalStrengthComparator.compare(o1.cellSignalStrengthCompat, o2.cellSignalStrengthCompat)
        }
    }

    object CellSignalStrengthComparator : Comparator<CellSignalStrength> {
        override fun compare(o1: CellSignalStrength, o2: CellSignalStrength): Int {
            fun getTypeRanking(strength: CellSignalStrength): Int {
                return when {
                    strength is CellSignalStrengthGsm -> 0
                    strength is CellSignalStrengthCdma -> 0
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthTdscdma -> 1
                    strength is CellSignalStrengthWcdma -> 2
                    strength is CellSignalStrengthLte -> 3
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthNr -> 4
                    else -> 0
                }
            }

            val rankResult = getTypeRanking(o2) - getTypeRanking(o1)

            if (rankResult != 0) {
                return rankResult
            }

            return o1.dbm.absoluteValue - o2.dbm.absoluteValue
        }
    }

    class SubsComparator(private val primarySub: Int) : Comparator<Int> {
        override fun compare(o1: Int, o2: Int): Int {
            if (o1 == primarySub) {
                return -1
            }

            if (o2 == primarySub) {
                return 1
            }

            return o1.compareTo(o2)
        }
    }
}

val CellConfigLte.endcAvailable: Boolean
    get() = this::class.java
        .getDeclaredMethod("isEndcAvailable")
        .apply { isAccessible = true }
        .invoke(this) as Boolean

val String?.asMccMnc: String
    get() = StringBuilder(
        when {
            isNullOrBlank() -> "000000"
            length < 3 -> StringBuilder(this).run {
                val makeup = 6 - this@asMccMnc.length

                appendRange("000000", 0, makeup + 1)
                toString()
            }
            else -> this
        }
    ).insert(3, "-").toString()

inline fun <reified T : CellInfo> CellInfo.cast() = castGeneric<T>()
inline fun <reified T : CellIdentity> CellIdentity.cast() = castGeneric<T>()
inline fun <reified T : CellSignalStrength> CellSignalStrength.cast() = castGeneric<T>()

inline fun <reified T : Any> Any.castGeneric(): T? {
    return if (this is T) this
            else null
}

fun Int.avail() = this != CellInfo.UNAVAILABLE
fun Long.avail() = this != CellInfo.UNAVAILABLE_LONG

@Composable
fun Int.onAvail(block: @Composable()(Int) -> Unit) {
    if (avail()) block(this)
}

@Composable
fun Long.onAvail(block: @Composable()(Long) -> Unit) {
    if (avail()) block(this)
}

@Composable
fun Int.onNegAvail(block: @Composable()(Int) -> Unit) {
    if (this != -1) block(this)
}

fun duplexModeToString(duplex: Int): String {
    return when (duplex) {
        ServiceState.DUPLEX_MODE_FDD -> "FDD"
        ServiceState.DUPLEX_MODE_TDD -> "TDD"
        else -> "UNKNOWN"
    }
}

fun domainToString(@NetworkRegistrationInfo.Domain domain: Int): String {
    return when (domain) {
        NetworkRegistrationInfo.DOMAIN_CS -> "CS"
        NetworkRegistrationInfo.DOMAIN_PS -> "PS"
        NetworkRegistrationInfo.DOMAIN_CS_PS -> "CS_PS"
        else -> "UNKNOWN"
    }
}

fun typeToString(type: Int): String {
    return when (type) {
        CellInfo.TYPE_GSM -> "GSM"
        CellInfo.TYPE_CDMA -> "CDMA"
        CellInfo.TYPE_TDSCDMA -> "TDSCDMA"
        CellInfo.TYPE_WCDMA -> "WCDMA"
        CellInfo.TYPE_LTE -> "LTE"
        CellInfo.TYPE_NR -> "NR"
        else -> "UNKNOWN"
    }
}

fun subscriptionTypeToString(type: Int): String {
    return when (type) {
        SubscriptionManager.SUBSCRIPTION_TYPE_LOCAL_SIM -> "Local SIM"
        SubscriptionManager.SUBSCRIPTION_TYPE_REMOTE_SIM -> "Remote SIM"
        else -> "Unknown"
    }
}

fun profileClassToString(clazz: Int): String {
    return when (clazz) {
        SubscriptionManager.PROFILE_CLASS_OPERATIONAL -> "Operational"
        SubscriptionManager.PROFILE_CLASS_PROVISIONING -> "Provisioning"
        SubscriptionManager.PROFILE_CLASS_TESTING -> "Testing"
        SubscriptionManager.PROFILE_CLASS_UNSET -> "Unset"
        else -> "Unknown"
    }
}

fun nameSourceToString(source: Int): String {
    return when (source) {
        SubscriptionManager.NAME_SOURCE_CARRIER -> "Carrier"
        SubscriptionManager.NAME_SOURCE_CARRIER_ID -> "Carrier ID"
        SubscriptionManager.NAME_SOURCE_SIM_SPN -> "SIM SPN"
        SubscriptionManager.NAME_SOURCE_SIM_PNN -> "SIM PNN"
        SubscriptionManager.NAME_SOURCE_USER_INPUT -> "User Input"
        else -> "Unknown"
    }
}