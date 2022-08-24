package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.zwander.cellreader.data.CellReaderTheme
import dev.zwander.cellreader.data.databinding.PermissionDialogLayoutBinding
import dev.zwander.cellreader.ui.components.BottomBarScrimContainer
import dev.zwander.cellreader.ui.components.MainContent
import dev.zwander.cellreader.utils.PermissionUtils

class MainActivity : ComponentActivity() {
    private val permReq =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.any { !it }) {
                finish()
            } else {
                init()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        with(PermissionUtils.getMissingPermissions(this)) {
            if (isNotEmpty()) {
                BottomSheetDialog(this@MainActivity).apply {
                    val viewBinding = PermissionDialogLayoutBinding.inflate(layoutInflater)

                    setCancelable(false)
                    setTitle(dev.zwander.cellreader.data.R.string.required_permissions)
                    setContentView(viewBinding.root)

                    viewBinding.postNotifications.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                    viewBinding.confirmButton.setOnClickListener {
                        permReq.launch(this@with)
                        dismiss()
                    }
                    viewBinding.cancelButton.setOnClickListener {
                        finish()
                        dismiss()
                    }
                    show()
                }
            } else {
                init()
            }
        }
    }

    private fun init() {
        startForegroundService(Intent(this, UpdaterService::class.java))

        setContent {
            val sysUiController = rememberSystemUiController()
            sysUiController.setStatusBarColor(Color.Transparent)
            sysUiController.setNavigationBarColor(Color.Transparent)

            Content()
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Content() {
    CellReaderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Box {
                MainContent()

                BottomBarScrimContainer()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Content()
}

