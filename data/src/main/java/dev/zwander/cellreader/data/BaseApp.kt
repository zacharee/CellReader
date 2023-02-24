package dev.zwander.cellreader.data

import android.app.Application
import android.os.Build
import androidx.annotation.CallSuper
import com.bugsnag.android.Bugsnag
import org.lsposed.hiddenapibypass.HiddenApiBypass

abstract class BaseApp : Application() {
    @CallSuper
    override fun onCreate() {
        super.onCreate()

        Bugsnag.start(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                HiddenApiBypass.setHiddenApiExemptions("")
            } catch (e: Throwable) {
                Bugsnag.notify(e)
            }
        }
    }
}