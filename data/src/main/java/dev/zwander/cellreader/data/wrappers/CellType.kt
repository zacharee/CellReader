package dev.zwander.cellreader.data.wrappers

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.zwander.cellreader.data.R

sealed class CellType(@StringRes private val labelRes: Int) {
    val Context.label: CharSequence
        get() = resources.getString(labelRes)

    @Composable
    fun label(): String {
        return stringResource(id = labelRes)
    }

    object GSM : CellType(R.string.gsm)
    object CDMA : CellType(R.string.cdma)
    object WCDMA : CellType(R.string.wcdma)
    object TDSCDMA : CellType(R.string.tdscdma)
    object LTE : CellType(R.string.lte)
    object NR : CellType(R.string.nr)
}
