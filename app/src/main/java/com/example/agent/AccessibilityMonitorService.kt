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

class AccessibilityMonitorService : AccessibilityService() {
    private lateinit var logger: Logger
    private lateinit var expfileOutputStream: FileOutputStream
    private var expfileWriter: BufferedWriter? = null
    private val expfile = "exp_log.txt"
    private lateinit var navfileOutputStream: FileOutputStream
    private var navfileWriter: BufferedWriter? = null
    private val navfile = "nav_log.txt"
    private val handler = Handler(Looper.getMainLooper())
    private var lastEventRunnable: Runnable? = null
    private var previousRoot: AccessibilityNodeInfo? = null


    override fun onCreate() {
        super.onCreate()
        initializeFileOutput()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        PermissionViewModel.updatePermissionList(this)
    }

    private fun initializeFileOutput() {
        try {
            val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val expFile = File(directory, expfile)
            println(expFile.absolutePath)
            expfileOutputStream = FileOutputStream(expFile, false)
            expfileWriter = BufferedWriter(OutputStreamWriter(expfileOutputStream))

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
        lastEventRunnable?.let { handler.removeCallbacks(it) }

        lastEventRunnable = Runnable {
            val source = event.source ?: return@Runnable
            val rootNode = rootInActiveWindow ?: return@Runnable
            Log.d("AccessibilityCheck", rootNode.hashCode().toString())

            if (previousRoot != null) {
                val isEqual = previousRoot == rootNode
                Log.d("AccessibilityCheck", "Are they equal? $isEqual")
            }

            previousRoot = rootNode

            try {
                initializeFileOutput()
                expfileWriter?.flush()
                navfileWriter?.flush()
                processNodeInfo(rootNode)
            } catch (e: Exception) {
                Log.e("AccessibilityMonitor", "Error writing to file: ${e.message}")
            }
        }
        handler.postDelayed(lastEventRunnable!!, 100)
    }

    private fun processNodeInfo(nodeInfo: AccessibilityNodeInfo, depth: Int = 0) {
        // Create indentation string
        val indent = "  ".repeat(depth)

        // Get node attributes
        val className = nodeInfo.className?.toString() ?: "null"
        val text = nodeInfo.text?.toString() ?: "null"
        val description = nodeInfo.contentDescription?.toString() ?: "null"
        val stateDescription = nodeInfo.stateDescription?.toString() ?: "null"
        val packageName = nodeInfo.packageName?.toString() ?: "null"

        // Get bounds
        val rect = Rect()
        nodeInfo.getBoundsInScreen(rect)
        val bounds = "[${rect.left}, ${rect.top}][${rect.right}, ${rect.bottom}]"

        // Get container title (window title)
        val containerTitle = nodeInfo.paneTitle?.toString() ?: "null"

        // Boolean states
//        val states = "checkable: ${nodeInfo.isCheckable}," +
//                "checked: ${nodeInfo.isChecked}," +
//                "focusable: ${nodeInfo.isFocusable}," +
//                "focused: ${nodeInfo.isFocused}," +
//                "selected: ${nodeInfo.isSelected}," +
//                "clickable: ${nodeInfo.isClickable}," +
//                "longClickable: ${nodeInfo.isLongClickable}," +
//                "contextClickable: ${nodeInfo.isContextClickable}," +
//                "enabled: ${nodeInfo.isEnabled}," +
//                "password: ${nodeInfo.isPassword}," +
//                "scrollable: ${nodeInfo.isScrollable}," +
//                "visible: ${nodeInfo.isVisibleToUser}," +
//                "textSelectable: ${nodeInfo.isTextSelectable()}"

        val states = "${nodeInfo.isCheckable}," +
                "${nodeInfo.isChecked}," +
                "${nodeInfo.isClickable}," +
                "${nodeInfo.isEnabled}," +
                "${nodeInfo.isFocusable}," +
                "${nodeInfo.isFocused}," +
                "${nodeInfo.isScrollable}," +
                "${nodeInfo.isLongClickable}," +
                "${nodeInfo.isSelected}," +
//                "${nodeInfo.isContextClickable}," +
                "${nodeInfo.isPassword}," +
                "${nodeInfo.isVisibleToUser}," +
//                "${nodeInfo.isTextSelectable()}"

        // Log all information with proper indentation
        Log.d("AccessibilityInfo", "$indent├─ Node: $className, Text: $text, ContentDescription: $description, ${nodeInfo.viewIdResourceName}, ${nodeInfo.uniqueId}, ${nodeInfo.windowId}")
//        Log.d("AccessibilityInfo", "$indent│  Text: $text")
//        Log.d("AccessibilityInfo", "$indent│  ContentDescription: $description")
//        Log.d("AccessibilityInfo", "$indent│  StateDescription: $stateDescription")
//        Log.d("AccessibilityInfo", "$indent│  ContainerTitle: $containerTitle")
//        Log.d("AccessibilityInfo", "$indent│  Bounds: $bounds")
//        Log.d("AccessibilityInfo", "$indent│  States: $states")

//        logger.d("$indent├─ Node: $className")
//        logger.d("$indent│  Text: $text")
//        logger.d("$indent│  ContentDescription: $description")
////        Log.d("AccessibilityInfo", "$indent│  StateDescription: $stateDescription")
////        Log.d("AccessibilityInfo", "$indent│  ContainerTitle: $containerTitle")
//        logger.d("$indent│  Bounds: $bounds")
//        logger.d("$indent│  States: $states")

//        logger.d("$indent├─ Node(class:$className,text:$text,content-desc:$description,bounds:$bounds,$states)")

        val navNodeInfoText = "$indent├─ Node($className,$text,$description,$bounds,$states)\n"
        try {
            navfileWriter?.write(navNodeInfoText)
            navfileWriter?.flush() // Ensure it's written immediately
        } catch (e: IOException) {
            Log.e("AccessibilityMonitor", "Error writing to file: ${e.message}")
        }

        val expNodeInfoText = "$indent├─ Node($className,$text,$description,${nodeInfo.hashCode()})\n"
        try {
            expfileWriter?.write(expNodeInfoText)
            expfileWriter?.flush() // Ensure it's written immediately
        } catch (e: IOException) {
            Log.e("AccessibilityMonitor", "Error writing to file: ${e.message}")
        }

        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i) ?: continue
            processNodeInfo(childNode, depth + 1)
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        try {
            expfileWriter?.close()
            expfileOutputStream.close()
            navfileWriter?.close()
            navfileOutputStream.close()
        } catch (e: IOException) {
            Log.e("AccessibilityMonitor", "Error closing file: ${e.message}")
        }
        super.onDestroy()
    }
}