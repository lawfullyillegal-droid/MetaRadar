package f.cking.software.data.helpers

import android.app.Activity
import android.net.Uri
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity

class ActivityProvider {

    private var activity: AppCompatActivity? = null
    
    @Volatile
    private var activityResultCallback: ((Uri?) -> Unit)? = null

    fun setActivity(activity: AppCompatActivity?) {
        this.activity = activity
    }

    fun requireActivity(): Activity = activity ?: throw IllegalStateException("Activity is not initialized")

    @MainThread
    fun setActivityResultCallback(callback: (Uri?) -> Unit) {
        activityResultCallback = callback
    }

    @MainThread
    fun invokeActivityResultCallback(uri: Uri?) {
        activityResultCallback?.invoke(uri)
        activityResultCallback = null
    }
}