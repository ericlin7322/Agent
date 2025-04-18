package com.example.agent

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.gemini.ui.permission.PermissionViewModel
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AccessibilityMonitorService : AccessibilityService() {
    companion object {
        var explore_tree  = ""
    }

    private lateinit var logger: Logger
    private lateinit var navfileOutputStream: FileOutputStream
    private var navfileWriter: BufferedWriter? = null
    private val navfile = "ui_tree.txt"
    private lateinit var statsfileOutputStream: FileOutputStream
    private var statsfileWriter: BufferedWriter? = null
    private val statsfile = "stats.txt"
    private val handler = Handler(Looper.getMainLooper())
    private var lastEventRunnable: Runnable? = null
    private var previousRoot: AccessibilityNodeInfo? = null

    private lateinit var runnable: Runnable
    private val interval = 1000L
    private var nav_tree = ""
    private var exp_tree = ""


    override fun onCreate() {
        super.onCreate()
        initializeFileOutput()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        PermissionViewModel.updatePermissionList(this)
        runnable = Runnable {
            saveNodeToFile()
            handler.postDelayed(runnable, interval)
        }
        handler.post(runnable)
    }

    private fun initializeFileOutput() {
        try {
            val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val navFile = File(directory, navfile)
            println(navFile.absolutePath)
            navfileOutputStream = FileOutputStream(navFile, false)
            navfileWriter = BufferedWriter(OutputStreamWriter(navfileOutputStream))
        } catch (e: Exception) {
            Log.e("AccessibilityMonitor", "Error creating file: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        logger = Logger(this)
        val source = event.source ?: return
        val rootNode = rootInActiveWindow ?: return
        Log.d("AccessibilityMonitor", event.toString())
        Log.d("AccessibilityMonitor", rootNode.hashCode().toString())

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            try {
                nav_tree = getNavigateTree(rootNode)
//                exp_tree = getExploreTree(rootNode)
            } catch (e: Exception) {
                Log.e("AccessibilityMonitor", "Error writing to file: ${e.message}")
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val statsFile = File(directory, statsfile)

            val currentDateTime = ZonedDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzzz")
            val formattedDateTime = currentDateTime.format(formatter)

            val json = """
                {
                  "current foregroundApp": "$packageName",
                  "current time": "$formattedDateTime"
                }
            """.trimIndent()

            statsfileOutputStream = FileOutputStream(statsFile, false)
            statsfileWriter = BufferedWriter(OutputStreamWriter(statsfileOutputStream))
            statsfileWriter?.write(json)
            statsfileWriter?.flush()
            Log.d("AppTracker", "Foreground app: $packageName")
        }
    }

    private fun saveNodeToFile() {
        initializeFileOutput()
        try {
            navfileWriter?.write(nav_tree)
            navfileWriter?.flush()
        } finally {
            navfileWriter?.close()
        }
    }

    private fun getNavigateTree(nodeInfo: AccessibilityNodeInfo, depth: Int = 0): String {
        val stringBuilder = StringBuilder()

        val indent = "  ".repeat(depth)
        val className = nodeInfo.className?.toString() ?: "null"
        val simpleClassName = className.substring(className.lastIndexOf(".") + 1)
        val text = nodeInfo.text?.toString() ?: "null"
        val description = nodeInfo.contentDescription?.toString() ?: "null"
        val stateDescription = nodeInfo.stateDescription?.toString() ?: "null"
        val packageName = nodeInfo.packageName?.toString() ?: "null"

        val rect = Rect()
        nodeInfo.getBoundsInScreen(rect)
        val bounds = "[${rect.left}, ${rect.top}][${rect.right}, ${rect.bottom}]"

        val containerTitle = nodeInfo.paneTitle?.toString() ?: "null"

        // Boolean states
        val states = "checkable:${nodeInfo.isCheckable}," +
                "checked:${nodeInfo.isChecked}," +
                "clickable:${nodeInfo.isClickable}," +
                "enabled:${nodeInfo.isEnabled}," +
                "focusable:${nodeInfo.isFocusable}," +
                "focused:${nodeInfo.isFocused}," +
                "scrollable:${nodeInfo.isScrollable}," +
                "longClickable:${nodeInfo.isLongClickable}," +
                "selected:${nodeInfo.isSelected}," +
//                "contextClickable: ${nodeInfo.isContextClickable}," +
                "password:${nodeInfo.isPassword}," +
                "visible:${nodeInfo.isVisibleToUser}"
//                "textSelectable: ${nodeInfo.isTextSelectable()}"

//        val states = "${nodeInfo.isCheckable}," +
//                "${nodeInfo.isChecked}," +
//                "${nodeInfo.isClickable}," +
//                "${nodeInfo.isEnabled}," +
//                "${nodeInfo.isFocusable}," +
//                "${nodeInfo.isFocused}," +
//                "${nodeInfo.isScrollable}," +
//                "${nodeInfo.isLongClickable}," +
//                "${nodeInfo.isSelected}," +
////                "${nodeInfo.isContextClickable}," +
//                "${nodeInfo.isPassword}," +
//                "${nodeInfo.isVisibleToUser},"
//                "${nodeInfo.isTextSelectable()}"

//        Log.d("AccessibilityInfo", "$indent├─ Node: $className, Text: $text, ContentDescription: $description, ${nodeInfo.viewIdResourceName}, ${nodeInfo.uniqueId}, ${nodeInfo.windowId}")

        val navNodeInfoText = "$indent├─ Node(class:$simpleClassName,text:$text,content_desc:$description,bound:$bounds,$states,hashcode:${nodeInfo.hashCode()})\n"
        stringBuilder.append(navNodeInfoText)

        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i) ?: continue
            stringBuilder.append(getNavigateTree(childNode, depth + 1))
        }

        return stringBuilder.toString()
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        try {
            navfileWriter?.close()
            navfileOutputStream.close()
        } catch (e: IOException) {
            Log.e("AccessibilityMonitor", "Error closing file: ${e.message}")
        }
        super.onDestroy()
    }
}