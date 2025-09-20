package dev.zwander.cellreader

import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import dev.zwander.cellreader.data.BaseApp
import dev.zwander.cellreader.widget.SignalWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            launch(Dispatchers.IO) {
                try {
                    val result = GlanceAppWidgetManager(this@App).setWidgetPreviews(
                        receiver = SignalWidgetReceiver::class,
                    )

                    Log.e("CellReader", "Result $result")
                } catch (e: Throwable) {
                    Log.e("CellReader", "error", e)
                }
            }
        }
    }
}
