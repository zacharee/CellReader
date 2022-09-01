package dev.zwander.cellreader.ui.view

import android.content.Context
import android.os.Build
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ComponentActivity
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.zwander.cellreader.data.CellReaderTheme
import dev.zwander.cellreader.data.R
import dev.zwander.cellreader.data.databinding.PermissionDialogLayoutBinding
import kotlinx.parcelize.Parcelize
import tk.zwander.patreonsupportersretrieval.util.launchUrl

class PermissionRationaleBottomSheetDialog(context: Context, private vararg val permissions: PermissionInfo) : BottomSheetDialog(context) {
    private val layout = PermissionDialogLayoutBinding.inflate(window.layoutInflater)

    init {
        setCancelable(false)
        setTitle(R.string.required_permissions)
        setContentView(layout.root)

        ViewTreeLifecycleOwner.set(window.decorView, (context as ComponentActivity))
        ViewTreeViewModelStoreOwner.set(window.decorView, context as androidx.activity.ComponentActivity)
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
                color = Color.Transparent
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
                                context.launchUrl("https://github.com/zacharee/CellReader/blob/master/privacy.md")
                            }
                        ) {
                            Text(text = stringResource(id = R.string.privacy_policy))
                        }

                        TextButton(
                            onClick = negativeListener
                        ) {
                            Text(text = stringResource(id = R.string.close_app))
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