package com.example.agent

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Logger(private val context: Context) {
    private val tag = "MyAppTag"
    private val logFile: File by lazy { createLogFile() }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private fun createLogFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "app_log.txt"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return File(directory, fileName)
    }

    fun logToFile(message: String, level: LogLevel = LogLevel.DEBUG) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }

        try {
            val timestamp = dateFormat.format(Date())
            val logEntry = "[$timestamp] [${level.name}] $message\n"

            FileOutputStream(logFile, true).use { fos ->
                OutputStreamWriter(fos).use { osw ->
                    osw.write(logEntry)
                    osw.flush()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error writing to log file: ${e.message}")
        }
    }

    fun getLogContent(): String {
        return try {
            logFile.readText()
        } catch (e: Exception) {
            "Error reading log file: ${e.message}"
        }
    }

    fun clearLogFile() {
        try {
            FileOutputStream(logFile, false).close()
        } catch (e: Exception) {
            Log.e(tag, "Error clearing log file: ${e.message}")
        }
    }

    fun d(message: String) = logToFile(message, LogLevel.DEBUG)
    fun i(message: String) = logToFile(message, LogLevel.INFO)
    fun w(message: String) = logToFile(message, LogLevel.WARN)
    fun e(message: String) = logToFile(message, LogLevel.ERROR)

    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}