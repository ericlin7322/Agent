package com.example.agent

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agent.explore_mode.SubAgent
import com.example.agent.permission.PermissionScreen
import com.example.agent.permission.PermissionViewModelFactory
import com.example.agent.ui.theme.AgentTheme
import com.example.gemini.ui.permission.PermissionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

sealed class Screen(val route: String) {
    data object PermissionScreen : Screen("permission_screen")
}

val images = arrayOf(
    // Image generated using Gemini from the prompt "cupcake image"
    R.drawable.baked_goods_1,
    // Image generated using Gemini from the prompt "cookies images"
    R.drawable.baked_goods_2,
    // Image generated using Gemini from the prompt "cake images"
    R.drawable.baked_goods_3,
)

class MainActivity : ComponentActivity() {
    private fun writeToFile(fileName: String, content: String) {
        try {
            val directory = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, fileName)
            FileOutputStream(file).use { output ->
                output.write(content.toByteArray())
            }
            println("File saved at: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageManager: PackageManager = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolvedInfos =
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )

        val appList = resolvedInfos.map { resolveInfo ->
            val appName = packageManager.getApplicationLabel(resolveInfo.activityInfo.applicationInfo).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val activityName = resolveInfo.activityInfo.name
            "$appName: $packageName/$activityName"
        }

        val appListString = appList.joinToString("\n")

        writeToFile("app_list.txt", appListString)

        setContent {
            AgentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    val permissionViewModel: PermissionViewModel = viewModel(factory = PermissionViewModelFactory(this))
                    NavHost(
                        navController = navController,
                        startDestination = Screen.PermissionScreen.route,
                    ) {
                        composable(
                            route = Screen.PermissionScreen.route
                        ) {
                            PermissionScreen(
                                permissionViewModel = permissionViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}