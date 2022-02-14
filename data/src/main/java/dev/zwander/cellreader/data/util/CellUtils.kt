package dev.zwander.cellreader.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.CellInfo
import android.telephony.NetworkRegistrationInfo
import android.telephony.ServiceState
import android.telephony.SubscriptionManager
import dev.zwander.cellreader.data.R
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
    fun connectionStatusToString(context: Context, connectionStatus: Int): String {
        return context.resources.getString(
            when (connectionStatus) {
                CellInfo.CONNECTION_NONE -> R.string.none
                CellInfo.CONNECTION_SECONDARY_SERVING -> R.string.secondary_serving
                CellInfo.CONNECTION_PRIMARY_SERVING -> R.string.primary_serving
                else -> R.string.unknown
            }
        )
    }

    fun nrStateToString(context: Context, @NetworkRegistrationInfo.NRState nrState: Int): String {
        return context.resources.getString(
            when (nrState) {
                NetworkRegistrationInfo.NR_STATE_RESTRICTED -> R.string.restricted
                NetworkRegistrationInfo.NR_STATE_NOT_RESTRICTED -> R.string.not_restricted
                NetworkRegistrationInfo.NR_STATE_CONNECTED -> R.string.connected
                else -> R.string.none
            }
        )
    }

    fun frequencyRangeToString(context: Context, @ServiceState.FrequencyRange range: Int): String {
        return context.resources.getString(
            when (range) {
                ServiceState.FREQUENCY_RANGE_LOW -> R.string.low
                ServiceState.FREQUENCY_RANGE_MID -> R.string.mid
                ServiceState.FREQUENCY_RANGE_HIGH -> R.string.high
                ServiceState.FREQUENCY_RANGE_MMWAVE -> R.string.mmwave
                else -> R.string.unknown
            }
        )
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

fun duplexModeToString(context: Context, duplex: Int): String {
    return context.resources.getString(
        when (duplex) {
            ServiceState.DUPLEX_MODE_FDD -> R.string.fdd
            ServiceState.DUPLEX_MODE_TDD -> R.string.tdd
            else -> R.string.unknown
        }
    )
}

fun domainToString(context: Context, @NetworkRegistrationInfo.Domain domain: Int): String {
    return context.resources.getString(
        when (domain) {
            NetworkRegistrationInfo.DOMAIN_CS -> R.string.cs
            NetworkRegistrationInfo.DOMAIN_PS -> R.string.ps
            NetworkRegistrationInfo.DOMAIN_CS_PS -> R.string.cs_ps
            else -> R.string.unknown
        }
    )
}

fun subscriptionTypeToString(context: Context, type: Int): String {
    return context.resources.getString(
        when (type) {
            SubscriptionManager.SUBSCRIPTION_TYPE_LOCAL_SIM -> R.string.local_sim
            SubscriptionManager.SUBSCRIPTION_TYPE_REMOTE_SIM -> R.string.remote_sim
            else -> R.string.unknown
        }
    )
}

fun profileClassToString(context: Context, clazz: Int): String {
    return context.resources.getString(
        when (clazz) {
            SubscriptionManager.PROFILE_CLASS_OPERATIONAL -> R.string.operational
            SubscriptionManager.PROFILE_CLASS_PROVISIONING -> R.string.provisioning
            SubscriptionManager.PROFILE_CLASS_TESTING -> R.string.testing
            SubscriptionManager.PROFILE_CLASS_UNSET -> R.string.unset
            else -> R.string.unknown
        }
    )
}

fun nameSourceToString(context: Context, source: Int): String {
    return context.resources.getString(
        when (source) {
            SubscriptionManager.NAME_SOURCE_CARRIER -> R.string.carrier
            SubscriptionManager.NAME_SOURCE_CARRIER_ID -> R.string.carrier_id
            SubscriptionManager.NAME_SOURCE_SIM_SPN -> R.string.sim_spn
            SubscriptionManager.NAME_SOURCE_SIM_PNN -> R.string.sim_pnn
            SubscriptionManager.NAME_SOURCE_USER_INPUT -> R.string.user_input
            else -> R.string.unknown
        }
    )
}