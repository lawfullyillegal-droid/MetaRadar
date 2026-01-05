package f.cking.software.data.helpers

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

/**
 * Provides access to the current Activity instance.
 * 
 * This class maintains a reference to the current Activity for dependency injection purposes.
 * The Activity reference is set when the Activity is created and cleared when destroyed
 * to prevent memory leaks.
 */
class ActivityProvider {

    private var activity: AppCompatActivity? = null

    /**
     * Sets the current Activity instance.
     * Should be called in Activity.onCreate() and cleared in Activity.onDestroy().
     * 
     * @param activity The current Activity instance, or null to clear the reference
     */
    fun setActivity(activity: AppCompatActivity?) {
        this.activity = activity
    }

    /**
     * Returns the current Activity instance.
     * 
     * @return The current Activity instance
     * @throws IllegalStateException if the Activity is not initialized
     */
    fun requireActivity(): Activity = activity 
        ?: throw IllegalStateException("Activity is not initialized. Ensure setActivity() was called.")
}
