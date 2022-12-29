package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.zwander.cellreader.data.CellReaderTheme
import dev.zwander.cellreader.data.data.CellModel
import dev.zwander.cellreader.data.data.ProvideCellModel
import dev.zwander.cellreader.ui.components.BottomBarScrimContainer
import dev.zwander.cellreader.ui.components.MainContent
import dev.zwander.cellreader.ui.view.PermissionRationaleBottomSheetDialog
import dev.zwander.cellreader.utils.PermissionUtils

class MainActivity : ComponentActivity() {
    private var initialized = false

    private val permReq =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            handlePermissions(PermissionUtils.getMissingPermissions(this).toList(), false)
        }

    private val locationDialog by lazy {
        PermissionRationaleBottomSheetDialog(
            this,
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.required_permission_fine_location
            )
        )
    }

    private val otherDialog by lazy {
        PermissionRationaleBottomSheetDialog(
            this,
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.required_permission_notifications,
                Build.VERSION_CODES.TIRAMISU
            ),
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.required_permission_phone_state
            ),
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.required_permission_phone_numbers
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        with(PermissionUtils.getMissingPermissions(this)) {
            handlePermissions(this.toList(), true)
        }
    }

    override fun onResume() {
        super.onResume()

        if (initialized) {
            refresh()
        }
    }

    private fun handlePermissions(permissions: List<String>, initialCheck: Boolean) {
        with (permissions) {
            when {
                isEmpty() -> init(initialCheck)
                contains(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    locationDialog.show(
                        positiveListener = {
                            permReq.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION))
                            locationDialog.dismiss()
                        },
                        negativeListener = {
                            finish()
                        }
                    )
                }
                else -> {
                    otherDialog.show(
                        positiveListener = {
                            permReq.launch(permissions.toTypedArray())
                            otherDialog.dismiss()
                        },
                        negativeListener = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        initialized = false

        super.onDestroy()
    }

    private fun init(initialCheck: Boolean) {
        if (!initialCheck) {
            refresh()
        }

        setContent {
            val sysUiController = rememberSystemUiController()
            sysUiController.setStatusBarColor(Color.Transparent)
            sysUiController.setNavigationBarColor(Color.Transparent)

            Content()
        }

        initialized = true
    }

    private fun refresh() {
        UpdaterService.refresh(this)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Content() {
    CellReaderTheme {
        ProvideCellModel(CellModel.getInstance()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box {
                    MainContent()

                    BottomBarScrimContainer()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}

