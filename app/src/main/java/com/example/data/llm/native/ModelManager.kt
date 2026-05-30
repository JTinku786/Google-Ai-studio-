package com.example.data.llm.native

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale

object ModelManager {
    private const val TAG = "ModelManager"
    private const val MODELS_DIR_NAME = "models"

    /**
     * Gets or creates the app-specific directory for GGUF models.
     */
    fun getModelsDir(context: Context): File {
        val dir = File(context.filesDir, MODELS_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Copies a GGUF model from a selected Uri (SAF) into app-specific storage.
     * Returns the target File or logs and returns null on failure.
     */
    fun importGgufModel(context: Context, sourceUri: Uri): File? {
        val contentResolver = context.contentResolver
        var fileName = "imported_model.gguf"
        
        // Resolve filename from Uri
        contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex >= 0) {
                    val resolvedName = cursor.getString(displayNameIndex)
                    if (!resolvedName.isNullOrEmpty()) {
                        fileName = resolvedName
                    }
                }
            }
        }

        // Sanitize filename to ensure it has .gguf extension
        if (!fileName.lowercase(Locale.US).endsWith(".gguf")) {
            fileName += ".gguf"
        }

        val modelsDir = getModelsDir(context)
        val targetFile = File(modelsDir, fileName)

        Log.i(TAG, "Importing GGUF model from '$sourceUri' to absolute target: '${targetFile.absolutePath}'")

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for Uri: $sourceUri")
                return null
            }

            FileOutputStream(targetFile).use { outputStream ->
                val buffer = ByteArray(1024 * 64) // 64KB chunks
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            inputStream.close()
            Log.i(TAG, "GGUF model imported successfully! Absolute path: ${targetFile.absolutePath}")
            return targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Error importing GGUF model", e)
            return null
        }
    }

    /**
     * Lists all imported .gguf files in filesDir/models/
     */
    fun listImportedModels(context: Context): List<File> {
        val modelsDir = getModelsDir(context)
        return modelsDir.listFiles { file ->
            file.isFile && file.name.lowercase(Locale.US).endsWith(".gguf")
        }?.toList() ?: emptyList()
    }

    /**
     * Formats bytes into human-readable strings (MB, GB).
     */
    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 Bytes"
        val units = arrayOf("Bytes", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(Locale.US, "%.2f %s", sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
