package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
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
import dev.zwander.cellreader.data.util.preferences
import dev.zwander.cellreader.ui.components.BottomBarScrimContainer
import dev.zwander.cellreader.ui.components.MainContent
import dev.zwander.cellreader.ui.view.PermissionRationaleBottomSheetDialog
import dev.zwander.cellreader.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
    private var initialized = false

    private val permReq =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            handlePermissions(PermissionUtils.getMissingPermissions(this).toList())
        }

    private val locationDialog by lazy {
        PermissionRationaleBottomSheetDialog(
            this,
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.required_permission_fine_location
            )
        )
    }

    private val backgroundLocationDialog by lazy {
        PermissionRationaleBottomSheetDialog(
            this,
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.optional_permission_background_location
            ),
            titleRes = dev.zwander.cellreader.data.R.string.optional_permissions,
            negativeRes = dev.zwander.cellreader.data.R.string.skip
        )
    }

    private val otherDialog by lazy {
        PermissionRationaleBottomSheetDialog(
            this,
            PermissionRationaleBottomSheetDialog.PermissionInfo(
                dev.zwander.cellreader.data.R.string.foreground_service_use_user,
            ),
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
            handlePermissions(this.toList())
        }
    }

    @SuppressLint("InlinedApi")
    private fun handlePermissions(permissions: List<String>) {
        launch {
            val actualPermissions = ArrayList(permissions)

            @SuppressLint("InlinedApi")
            if (preferences.declinedBackgroundLocation.first()) {
                actualPermissions.remove(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            with (actualPermissions) {
                when {
                    isEmpty() -> init()
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
                    contains(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                        backgroundLocationDialog.show(
                            positiveListener = {
                                permReq.launch(arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                                backgroundLocationDialog.dismiss()
                            },
                            negativeListener = {
                                runBlocking {
                                    preferences.updateDeclinedBackgroundLocation(true)

                                    permReq.launch((actualPermissions - android.Manifest.permission.ACCESS_BACKGROUND_LOCATION).toTypedArray())
                                    backgroundLocationDialog.dismiss()
                                }
                            }
                        )
                    }
                    else -> {
                        otherDialog.show(
                            positiveListener = {
                                permReq.launch(actualPermissions.toTypedArray())
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
    }

    override fun onResume() {
        super.onResume()

        if (initialized &&
                PermissionUtils.getMissingPermissions(this)
                    .contains(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            UpdaterService.start(
                this, true
            )
        }
    }

    override fun onDestroy() {
        initialized = false

        cancel()
        super.onDestroy()
    }

    private fun init() {
        UpdaterService.start(
            this,
            false
        )

        setContent {
            val sysUiController = rememberSystemUiController()
            sysUiController.setStatusBarColor(Color.Transparent)
            sysUiController.setNavigationBarColor(Color.Transparent)
            sysUiController.statusBarDarkContentEnabled = !isSystemInDarkTheme()
            sysUiController.navigationBarDarkContentEnabled = !isSystemInDarkTheme()

            Content()
        }

        initialized = true
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

