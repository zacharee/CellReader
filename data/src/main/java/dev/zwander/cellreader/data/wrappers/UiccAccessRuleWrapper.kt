package dev.zwander.cellreader.data.wrappers

import android.telephony.UiccAccessRule

data class UiccAccessRuleWrapper(
    val certificateHash: ByteArray,
    val packageName: String?,
    val accessType: Long
) {
    companion object {
        private val HEX_CHARS = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        )

        fun bytesToHexString(bytes: ByteArray?): String? {
            if (bytes == null) return null
            val ret = StringBuilder(2 * bytes.size)
            for (i in bytes.indices) {
                var b = 0x0f and (bytes[i].toInt() shr 4)
                ret.append(HEX_CHARS[b])
                b = 0x0f and bytes[i].toInt()
                ret.append(HEX_CHARS[b])
            }
            return ret.toString()
        }
    }

    constructor(rule: UiccAccessRule) : this(
        rule::class.java
            .getDeclaredField("mCertificateHash")
            .apply { isAccessible = true }
            .get(rule) as ByteArray,
        rule.packageName,
        rule::class.java
            .getDeclaredField("mAccessType")
            .apply { isAccessible = true }
            .getLong(rule)
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UiccAccessRuleWrapper

        if (!certificateHash.contentEquals(other.certificateHash)) return false
        if (packageName != other.packageName) return false
        if (accessType != other.accessType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = certificateHash.contentHashCode()
        result = 31 * result + (packageName?.hashCode() ?: 0)
        result = 31 * result + accessType.hashCode()
        return result
    }

}