package com.example.agent.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.gemini.ui.permission.PermissionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    permissionViewModel: PermissionViewModel, navController: NavController
) {
    val context = LocalContext.current
    val permissionList by permissionViewModel.permissionList.collectAsState()

    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Permission")
        })
    }) { paddingValue ->
        LazyColumn(
            modifier = Modifier.padding(paddingValue),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(permissionList) { permission ->
                when (permission.name) {
                    Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> {
                        PermissionItem(permission = permission) {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

class PermissionViewModelFactory(val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PermissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return PermissionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}