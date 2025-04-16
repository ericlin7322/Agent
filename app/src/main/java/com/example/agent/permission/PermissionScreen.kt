package com.example.agent.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.agent.AccessibilityMonitorService
import com.example.agent.R
import com.example.agent.explore_mode.SubAgent
import com.example.gemini.ui.permission.PermissionViewModel

val images = arrayOf(
    // Image generated using Gemini from the prompt "cupcake image"
    R.drawable.baked_goods_1,
    // Image generated using Gemini from the prompt "cookies images"
    R.drawable.baked_goods_2,
    // Image generated using Gemini from the prompt "cake images"
    R.drawable.baked_goods_3,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    permissionViewModel: PermissionViewModel, navController: NavController
) {
    val context = LocalContext.current
    val permissionList by permissionViewModel.permissionList.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                PermissionViewModel.updatePermissionList(context)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
//            item {
//                Button(
//                    onClick = {
//                        val bitmap = BitmapFactory.decodeResource(
//                            context.resources,
//                            images[0]
//                        )
//                        val exploreMode = SubAgent()
//                        exploreMode.getResponse("Find 10 paper about llm", AccessibilityMonitorService.explore_tree, bitmap)
//                    }
//                ) {
//                    Text(text = stringResource(R.string.action_go))
//                }
//            }
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