package f.cking.software.data.helpers

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.MainThread
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe manager for handling activity results using the modern ActivityResultContracts API.
 * 
 * This class provides a centralized, type-safe way to manage activity result callbacks,
 * eliminating the need for manual request code management and unsafe mutable maps.
 * 
 * Benefits over deprecated startActivityForResult:
 * - Type-safe launcher registration
 * - Thread-safe callback management using atomic operations
 * - Automatic cleanup after callback invocation
 * - No memory leaks from stale callbacks
 * - Lifecycle-aware through ActivityResultLauncher
 */
class ActivityResultManager {

    @Volatile
    private var selectDirectoryLauncher: ActivityResultLauncher<Uri?>? = null
    
    @Volatile
    private var selectFileLauncher: ActivityResultLauncher<Array<String>>? = null
    
    @Volatile
    private var createFileLauncher: ActivityResultLauncher<String>? = null

    // Using AtomicReference for truly thread-safe callback management
    private val pendingCallback = AtomicReference<((Uri?) -> Unit)?>(null)

    /**
     * Registers the directory selection launcher.
     * Must be called before onCreate returns.
     * 
     * @param launcher The ActivityResultLauncher for selecting directories
     */
    @MainThread
    fun setSelectDirectoryLauncher(launcher: ActivityResultLauncher<Uri?>) {
        selectDirectoryLauncher = launcher
    }

    /**
     * Registers the file selection launcher.
     * Must be called before onCreate returns.
     * 
     * @param launcher The ActivityResultLauncher for selecting files
     */
    @MainThread
    fun setSelectFileLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        selectFileLauncher = launcher
    }

    /**
     * Registers the file creation launcher.
     * Must be called before onCreate returns.
     * 
     * @param launcher The ActivityResultLauncher for creating files
     */
    @MainThread
    fun setCreateFileLauncher(launcher: ActivityResultLauncher<String>) {
        createFileLauncher = launcher
    }

    /**
     * Launches directory selection and stores the callback for result handling.
     * 
     * @param onResult Callback to be invoked with the selected directory URI or null if cancelled
     * @throws IllegalStateException if the launcher is not initialized
     */
    @MainThread
    fun launchSelectDirectory(onResult: (directoryPath: Uri?) -> Unit) {
        val launcher = selectDirectoryLauncher
            ?: throw IllegalStateException("Select directory launcher is not initialized. Call setSelectDirectoryLauncher() first.")
        
        if (!pendingCallback.compareAndSet(null, onResult)) {
            throw IllegalStateException("Another activity result operation is already in progress")
        }
        
        launcher.launch(null)
    }

    /**
     * Launches file selection and stores the callback for result handling.
     * 
     * @param mimeTypes Array of MIME types to filter (default: ["*/*"] for all files)
     * @param onResult Callback to be invoked with the selected file URI or null if cancelled
     * @throws IllegalStateException if the launcher is not initialized
     */
    @MainThread
    fun launchSelectFile(mimeTypes: Array<String> = arrayOf("*/*"), onResult: (filePath: Uri?) -> Unit) {
        val launcher = selectFileLauncher
            ?: throw IllegalStateException("Select file launcher is not initialized. Call setSelectFileLauncher() first.")
        
        if (!pendingCallback.compareAndSet(null, onResult)) {
            throw IllegalStateException("Another activity result operation is already in progress")
        }
        
        launcher.launch(mimeTypes)
    }

    /**
     * Launches file creation and stores the callback for result handling.
     * 
     * @param fileName The suggested file name
     * @param onResult Callback to be invoked with the created file URI or null if cancelled
     * @throws IllegalStateException if the launcher is not initialized
     */
    @MainThread
    fun launchCreateFile(fileName: String, onResult: (filePath: Uri?) -> Unit) {
        val launcher = createFileLauncher
            ?: throw IllegalStateException("Create file launcher is not initialized. Call setCreateFileLauncher() first.")
        
        if (!pendingCallback.compareAndSet(null, onResult)) {
            throw IllegalStateException("Another activity result operation is already in progress")
        }
        
        launcher.launch(fileName)
    }

    /**
     * Handles the activity result by invoking and clearing the pending callback.
     * This method is called automatically by the registered ActivityResultLauncher.
     * 
     * Thread-safe through atomic compareAndSet operation.
     * 
     * @param uri The result URI, or null if the operation was cancelled
     */
    @MainThread
    fun handleResult(uri: Uri?) {
        // Atomically get and clear the callback
        val callback = pendingCallback.getAndSet(null)
        callback?.invoke(uri)
    }

    /**
     * Checks if all launchers are properly initialized.
     * Useful for debugging and ensuring proper setup.
     * 
     * @return true if all launchers are set, false otherwise
     */
    fun isInitialized(): Boolean {
        return selectDirectoryLauncher != null && 
               selectFileLauncher != null && 
               createFileLauncher != null
    }

    /**
     * Clears all registered launchers.
     * Should be called when the activity is being destroyed to prevent memory leaks.
     */
    fun clear() {
        selectDirectoryLauncher = null
        selectFileLauncher = null
        createFileLauncher = null
        pendingCallback.set(null)
    }
}
