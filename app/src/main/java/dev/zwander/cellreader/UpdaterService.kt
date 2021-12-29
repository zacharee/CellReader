package dev.zwander.cellreader

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.*
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.updateAll
import dev.zwander.cellreader.utils.CellUtils
import kotlinx.coroutines.*
import kotlin.collections.HashMap

class UpdaterService : Service(), CoroutineScope by MainScope() {
    companion object {
        const val ACTION_EXIT = "${BuildConfig.APPLICATION_ID}.EXIT"
    }

    private val telephony by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val subs by lazy { getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }

    private val telephonies = arrayListOf<TelephonyManager>()
    private val callbacks = HashMap<Int, TelephonyCallback>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_EXIT) {
            stopSelf()
            return START_NOT_STICKY
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        val nm = NotificationManagerCompat.from(this)

        nm.createNotificationChannel(
            NotificationChannelCompat.Builder("main", NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(resources.getString(R.string.app_name))
                .build()
        )

        val n = NotificationCompat.Builder(this, "main")
            .setContentTitle(resources.getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                NotificationCompat.Action.Builder(
                    null, "Exit", PendingIntent.getService(
                        this,
                        100,
                        Intent(this, UpdaterService::class.java).apply { action = ACTION_EXIT },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                ).build()
            )
            .build()

        startForeground(100, n)

        init()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        cancel()

        telephonies.forEach { telephony ->
            telephony.unregisterTelephonyCallback(callbacks[telephony.subscriptionId])
        }
    }

    @SuppressLint("MissingPermission")
    private fun init() {
        telephonies.addAll(
            subs.allSubscriptionInfoList.map { telephony.createForSubscriptionId(it.subscriptionId) }
        )

        telephonies.forEach { telephony ->
            val callback = callbacks[telephony.subscriptionId] ?: TelephonyListener(telephony.subscriptionId).apply {
                callbacks[telephony.subscriptionId] = this
            }

            update(telephony.subscriptionId, telephony.allCellInfo)
            telephony.registerTelephonyCallback(Dispatchers.Main.asExecutor(), callback)
        }
    }

    private fun update(subId: Int, infos: List<CellInfo>) {
        primaryCell = subs.defaultDataSubscriptionInfo.subscriptionId
        cellInfos[subId] = infos.sortedWith(CellUtils.CellInfoComparator)

        launch(Dispatchers.IO) {
//            PrefUtils.setCellInfos(this@UpdaterService, cellInfos, primaryCell)
            SignalWidget().updateAll(this@UpdaterService)
        }
    }

    private inner class TelephonyListener(private val subId: Int) : TelephonyCallback(),
        TelephonyCallback.CellInfoListener {
        @SuppressLint("MissingPermission")
        override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
            update(subId, cellInfo)
        }
    }
}