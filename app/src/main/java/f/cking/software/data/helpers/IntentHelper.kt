package f.cking.software.data.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.MainThread
import f.cking.software.data.helpers.IntentHelper.ScreenNavigation.Companion.toNavigationCommand
import f.cking.software.openUrl
import f.cking.software.ui.MainActivity
import f.cking.software.ui.ScreenNavigationCommands
import f.cking.software.utils.navigation.NavigationCommand
import f.cking.software.utils.navigation.Router

/**
 * Helper class for managing system intents and navigation.
 * 
 * Utilizes the modern ActivityResultContracts API through [ActivityResultManager]
 * for safe, lifecycle-aware file/directory operations.
 */
class IntentHelper(
    private val activityProvider: ActivityProvider,
    private val router: Router,
    private val context: Context,
) {

    private val activityResultManager = ActivityResultManager()

    @MainThread
    fun setSelectDirectoryLauncher(launcher: ActivityResultLauncher<Uri?>) {
        activityResultManager.setSelectDirectoryLauncher(launcher)
    }

    @MainThread
    fun setSelectFileLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        activityResultManager.setSelectFileLauncher(launcher)
    }

    @MainThread
    fun setCreateFileLauncher(launcher: ActivityResultLauncher<String>) {
        activityResultManager.setCreateFileLauncher(launcher)
    }

    /**
     * Launches a directory picker and returns the selected directory URI via callback.
     * Uses the modern ActivityResultContracts.OpenDocumentTree API.
     * 
     * @param onResult Callback invoked with the selected directory URI, or null if cancelled
     */
    fun selectDirectory(onResult: (directoryPath: Uri?) -> Unit) {
        activityResultManager.launchSelectDirectory(onResult)
    }

    /**
     * Launches a file picker and returns the selected file URI via callback.
     * Uses the modern ActivityResultContracts.OpenDocument API.
     * 
     * @param onResult Callback invoked with the selected file URI, or null if cancelled
     */
    fun selectFile(onResult: (filePath: Uri?) -> Unit) {
        activityResultManager.launchSelectFile(onResult)
    }

    /**
     * Launches a file creation dialog and returns the created file URI via callback.
     * Uses the modern ActivityResultContracts.CreateDocument API.
     * 
     * @param fileName The suggested file name
     * @param onResult Callback invoked with the created file URI, or null if cancelled
     */
    fun createFile(fileName: String, onResult: (directoryPath: Uri?) -> Unit) {
        activityResultManager.launchCreateFile(fileName, onResult)
    }

    /**
     * Internal method to handle activity results.
     * Called by MainActivity when an activity result is received.
     */
    internal fun handleActivityResult(uri: Uri?) {
        activityResultManager.handleResult(uri)
    }

    /**
     * Clears all activity result launchers.
     * Should be called when the activity is destroyed.
     */
    internal fun clearActivityResults() {
        activityResultManager.clear()
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        activityProvider.requireActivity().startActivity(intent)
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        activityProvider.requireActivity().startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    fun openBluetoothSettings() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activityProvider.requireActivity().startActivity(intent)
    }

    fun openUrl(url: String) {
        val activity = activityProvider.requireActivity()
        activity.openUrl(url)
    }

    fun openScreenIntent(screenName: ScreenNavigation): Intent {
        val intent = Intent(context, MainActivity::class.java)
        intent.setAction(ACTION_OPEN_SCREEN)
        intent.putExtra(SCREEN_NAME, screenName.name)
        return intent
    }

    fun tryHandleIntent(intent: Intent): Boolean {
        if (intent.isScreenNavigation()) {
            val screenNavigation = ScreenNavigation.fromIntent(intent) ?: return false
            router.navigate(screenNavigation.toNavigationCommand())
            return true
        }
        return false
    }

    private fun Intent.isScreenNavigation(): Boolean {
        return action == ACTION_OPEN_SCREEN
    }

    enum class ScreenNavigation {
        MAIN,
        BACKGROUND_LOCATION_DESCRIPTION;

        companion object {

            fun fromIntent(intent: Intent): ScreenNavigation? {
                val name = intent.getStringExtra(SCREEN_NAME) ?: return null
                return entries.firstOrNull { it.name == name }
            }

            fun ScreenNavigation.toNavigationCommand(): NavigationCommand {
                return when (this) {
                    MAIN -> ScreenNavigationCommands.OpenMainScreen
                    BACKGROUND_LOCATION_DESCRIPTION -> ScreenNavigationCommands.OpenBackgroundLocationScreen
                }
            }
        }
    }

    companion object {
        private const val ACTION_OPEN_SCREEN = "action_open_screen"
        private const val SCREEN_NAME = "screen_name"
    }
}