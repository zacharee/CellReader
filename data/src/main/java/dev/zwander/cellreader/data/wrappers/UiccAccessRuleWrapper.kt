package dev.zwander.cellreader.data.wrappers

import android.telephony.UiccAccessRule

data class UiccAccessRuleWrapper(
    val certificateHash: ByteArray,
    val packageName: String?,
    val accessType: Long
) {
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