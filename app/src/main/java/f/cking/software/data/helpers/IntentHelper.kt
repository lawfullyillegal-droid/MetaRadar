package f.cking.software.data.helpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import f.cking.software.data.helpers.IntentHelper.ScreenNavigation.Companion.toNavigationCommand
import f.cking.software.openUrl
import f.cking.software.ui.MainActivity
import f.cking.software.ui.ScreenNavigationCommands
import f.cking.software.utils.navigation.NavigationCommand
import f.cking.software.utils.navigation.Router

class IntentHelper(
    private val activityProvider: ActivityProvider,
    private val router: Router,
    private val context: Context,
) {

    private var selectDirectoryLauncher: ActivityResultLauncher<Uri?>? = null
    private var selectFileLauncher: ActivityResultLauncher<Array<String>>? = null
    private var createFileLauncher: ActivityResultLauncher<String>? = null

    fun setSelectDirectoryLauncher(launcher: ActivityResultLauncher<Uri?>) {
        selectDirectoryLauncher = launcher
    }

    fun setSelectFileLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        selectFileLauncher = launcher
    }

    fun setCreateFileLauncher(launcher: ActivityResultLauncher<String>) {
        createFileLauncher = launcher
    }

    fun selectDirectory(onResult: (directoryPath: Uri?) -> Unit) {
        val launcher = selectDirectoryLauncher
            ?: throw IllegalStateException("Select directory launcher is not initialized")
        activityProvider.setActivityResultCallback(onResult)
        launcher.launch(null)
    }

    fun selectFile(onResult: (filePath: Uri?) -> Unit) {
        val launcher = selectFileLauncher
            ?: throw IllegalStateException("Select file launcher is not initialized")
        activityProvider.setActivityResultCallback(onResult)
        launcher.launch(arrayOf("*/*"))
    }

    fun createFile(fileName: String, onResult: (directoryPath: Uri?) -> Unit) {
        val launcher = createFileLauncher
            ?: throw IllegalStateException("Create file launcher is not initialized")
        activityProvider.setActivityResultCallback(onResult)
        launcher.launch(fileName)
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