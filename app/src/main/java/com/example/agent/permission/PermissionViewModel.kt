package com.example.gemini.ui.permission

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.example.agent.AccessibilityMonitorService
import com.example.agent.permission.Permission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionViewModel(context: Context) : ViewModel() {
    companion object {
        val _permissionList = MutableStateFlow(emptyList<Permission>())

        fun updatePermissionList(context: Context) {
            _permissionList.value = listOf(
                Permission(Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
                    "Accessibility Service",
                    isAccessibilityServiceEnabled(context, AccessibilityMonitorService::class.java))
            )
        }

        private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
            val componentName = ComponentName(context, service)
            val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            val colonSplitter = enabledServices?.split(":") ?: return false

            for (serv in colonSplitter) {
                if (serv.equals(componentName.flattenToString(), ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }

    init {
        updatePermissionList(context)
    }

    val permissionList: StateFlow<List<Permission>> = _permissionList
}