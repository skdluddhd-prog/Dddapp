package com.example.engine

import android.content.Context
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class CheckpointState(
    val isPaused: Boolean = false,
    val checkpointFile: File? = null,
    val previewSnippet: String = "",
    val timestamp: Long = 0L
)

/**
 * 5. The Interceptor & Merge Checkpoint:
 * Pauses pipeline data at Home Base into an on-disk temporary file.
 * Requires explicit approval via approveAndRoute() before streaming to Tab B.
 * Includes OOM-safe mergeFiles() utility to combine outputs from multiple workers.
 */
object DataCheckpoint {

    var currentState: CheckpointState = CheckpointState()
        private set

    /**
     * Pauses incoming data at Home Base and writes directly to disk.
     */
    suspend fun pauseAtHomeBase(
        context: Context,
        incomingData: String,
        fileName: String = "checkpoint_output.txt"
    ): File = withContext(Dispatchers.IO) {
        val file = FileStreamHelper.writeCodeToFile(context, incomingData, fileName)
        val preview = incomingData.take(120).trim()

        currentState = CheckpointState(
            isPaused = true,
            checkpointFile = file,
            previewSnippet = preview,
            timestamp = System.currentTimeMillis()
        )
        file
    }

    /**
     * Approves paused checkpoint data and streams it in chunks to Tab B.
     */
    suspend fun approveAndRoute(
        context: Context,
        targetWebView: WebView,
        onComplete: (() -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val file = currentState.checkpointFile
        if (file != null && file.exists()) {
            FileStreamHelper.streamCodeToWebView(targetWebView, file)
            currentState = CheckpointState(isPaused = false, checkpointFile = null)
            withContext(Dispatchers.Main) {
                onComplete?.invoke()
            }
        }
    }

    /**
     * Dismisses or clears the checkpoint without routing.
     */
    fun rejectCheckpoint() {
        currentState.checkpointFile?.delete()
        currentState = CheckpointState(isPaused = false, checkpointFile = null)
    }

    /**
     * Merges outputs from multiple workers into a single file without loading both into RAM.
     * Uses buffered streaming to ensure zero memory exhaustion on 4GB devices.
     */
    suspend fun mergeFiles(
        context: Context,
        file1: File,
        file2: File,
        outputFileName: String = "merged_output.txt"
    ): File = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, outputFileName)

        BufferedWriter(FileWriter(outputFile, false), 8192).use { writer ->
            // Copy file 1 in chunks
            if (file1.exists()) {
                BufferedReader(FileReader(file1), 4096).use { reader ->
                    reader.copyTo(writer)
                }
                writer.write("\n\n/* --- Merged Worker Output Separation --- */\n\n")
            }

            // Copy file 2 in chunks
            if (file2.exists()) {
                BufferedReader(FileReader(file2), 4096).use { reader ->
                    reader.copyTo(writer)
                }
            }
            writer.flush()
        }
        outputFile
    }
}
