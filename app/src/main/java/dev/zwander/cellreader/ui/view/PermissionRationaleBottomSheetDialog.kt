package dev.zwander.cellreader.ui.view

import android.content.Context
import android.os.Build
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ComponentActivity
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.zwander.cellreader.data.CellReaderTheme
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.databinding.PermissionDialogLayoutBinding
import kotlinx.parcelize.Parcelize
import tk.zwander.patreonsupportersretrieval.util.launchUrl

class PermissionRationaleBottomSheetDialog(
    context: Context,
    private vararg val permissions: PermissionInfo,
    @StringRes private val titleRes: Int = R.string.required_permissions,
    @StringRes private val negativeRes: Int = R.string.close_app,
) : BottomSheetDialog(context) {
    private val layout = PermissionDialogLayoutBinding.inflate(window.layoutInflater)

    init {
        setCancelable(false)
        setTitle(titleRes)
        setContentView(layout.root)

        window.decorView.setViewTreeLifecycleOwner(context as ComponentActivity)
        window.decorView.setViewTreeViewModelStoreOwner(context as androidx.activity.ComponentActivity)
        window.decorView.setViewTreeSavedStateRegistryOwner(context)
    }

    fun show(positiveListener: () -> Unit, negativeListener: () -> Unit) {
        layout.root.setContent {
            Content(permissions, positiveListener, negativeListener)
        }
        super.show()
    }

    @Composable
    private fun Content(permissions: Array<out PermissionInfo>, positiveListener: () -> Unit, negativeListener: () -> Unit) {
        CellReaderTheme {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(permissions.toList().filter { it.minApi <= Build.VERSION.SDK_INT }, { it.text }) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = it.text)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.size(8.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                context.launchUrl("https://zacharee.github.io/CellReader/privacy.html")
                            }
                        ) {
                            Text(text = stringResource(id = R.string.privacy_policy))
                        }

                        TextButton(
                            onClick = negativeListener
                        ) {
                            Text(text = stringResource(id = negativeRes))
                        }

                        TextButton(
                            onClick = positiveListener
                        ) {
                            Text(text = stringResource(id = R.string.grant))
                        }
                    }
                }
            }
        }
    }

    @Parcelize
    data class PermissionInfo(
        @StringRes val text: Int,
        val minApi: Int = Build.VERSION_CODES.BASE
    ) : Parcelable
}