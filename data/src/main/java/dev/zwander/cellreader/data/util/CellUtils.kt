package dev.zwander.cellreader.data.util

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.*
import androidx.compose.runtime.Composable
import dev.zwander.cellreader.data.wrappers.*
import kotlin.math.absoluteValue

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

object CellUtils {
    fun connectionStatusToString(connectionStatus: Int): String {
        return when (connectionStatus) {
            CellInfo.CONNECTION_NONE -> "None"
            CellInfo.CONNECTION_SECONDARY_SERVING -> "Secondary Serving"
            CellInfo.CONNECTION_PRIMARY_SERVING -> "Primary Serving"
            else -> "Unknown"
        }
    }

    fun nrStateToString(@NetworkRegistrationInfo.NRState nrState: Int): String {
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

    object CellInfoComparator : Comparator<CellInfoWrapper> {
        override fun compare(o1: CellInfoWrapper, o2: CellInfoWrapper): Int {
            val statusResult = if (o1.connectionStatus != CellInfo.CONNECTION_NONE && o2.connectionStatus != CellInfo.CONNECTION_NONE
                && o1.connectionStatus != CellInfo.CONNECTION_UNKNOWN && o2.connectionStatus != CellInfo.CONNECTION_UNKNOWN) {
                o1.connectionStatus - o2.connectionStatus
            } else {
                0
            }

            if (statusResult != 0) {
                return statusResult
            }

            if (o1.connectionStatus == CellInfo.CONNECTION_NONE && o2.connectionStatus != CellInfo.CONNECTION_NONE) {
                return 1
            }

            if (o1.connectionStatus != CellInfo.CONNECTION_NONE && o2.connectionStatus == CellInfo.CONNECTION_NONE) {
                return -1
            }

            if (o1.connectionStatus == CellInfo.CONNECTION_UNKNOWN && o2.connectionStatus != CellInfo.CONNECTION_UNKNOWN) {
                return 1
            }

            if (o2.connectionStatus != CellInfo.CONNECTION_UNKNOWN && o2.connectionStatus == CellInfo.CONNECTION_UNKNOWN) {
                return -1
            }

            if (o1.isRegistered && !o2.isRegistered) {
                return -1
            }

            if (!o1.isRegistered && o2.isRegistered) {
                return 1
            }

            fun getTypeRanking(info: CellInfoWrapper): Int {
                return when {
                    info is CellInfoGsmWrapper -> 0
                    info is CellInfoCdmaWrapper -> 0
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoTdscdmaWrapper -> 1
                    info is CellInfoWcdmaWrapper -> 2
                    info is CellInfoLteWrapper -> 3
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNrWrapper -> 4
                    else -> 0
                }
            }

            val rankResult = getTypeRanking(o2) - getTypeRanking(o1)

            if (rankResult != 0) {
                return rankResult
            }

            return CellSignalStrengthComparator.compare(o1.cellSignalStrength, o2.cellSignalStrength)
        }
    }

    object CellSignalStrengthComparator : Comparator<CellSignalStrengthWrapper> {
        override fun compare(o1: CellSignalStrengthWrapper, o2: CellSignalStrengthWrapper): Int {
            fun getTypeRanking(strength: CellSignalStrengthWrapper): Int {
                return when {
                    strength is CellSignalStrengthGsmWrapper -> 0
                    strength is CellSignalStrengthCdmaWrapper -> 0
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthTdscdmaWrapper -> 1
                    strength is CellSignalStrengthWcdmaWrapper -> 2
                    strength is CellSignalStrengthLteWrapper -> 3
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && strength is CellSignalStrengthNrWrapper -> 4
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
}

inline fun <reified T : CellInfoWrapper> CellInfoWrapper.cast() = castGeneric<T>()
inline fun <reified T : CellIdentityWrapper> CellIdentityWrapper.cast() = castGeneric<T>()
inline fun <reified T : CellSignalStrengthWrapper> CellSignalStrengthWrapper.cast() = castGeneric<T>()

inline fun <reified T : CellInfoWrapper> CellInfoWrapper.onCast(block: T.() -> Unit) = cast<T>()?.let(block)
inline fun <reified T : CellIdentityWrapper> CellIdentityWrapper.onCast(block: T.() -> Unit) = cast<T>()?.let(block)
inline fun <reified T : CellSignalStrengthWrapper> CellSignalStrengthWrapper.onCast(block: T.() -> Unit) = cast<T>()?.let(block)

inline fun <reified T : Any> Any.castGeneric(): T? {
    return if (this is T) this
    else null
}

@SuppressLint("InlinedApi")
fun Int.avail() = this != CellInfo.UNAVAILABLE
@SuppressLint("InlinedApi")
fun Long.avail() = this != CellInfo.UNAVAILABLE_LONG

@SuppressLint("ComposableNaming")
inline fun Int.onAvail(block: (Int) -> Unit) {
    if (avail()) block(this)
}

@SuppressLint("ComposableNaming")
inline fun Long.onAvail(block: (Long) -> Unit) {
    if (avail()) block(this)
}

@SuppressLint("ComposableNaming")
inline fun Int.onNegAvail(block: (Int) -> Unit) {
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