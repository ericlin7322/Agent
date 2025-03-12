package com.example.agent

import android.os.Bundle
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
import com.example.agent.permission.PermissionScreen
import com.example.agent.permission.PermissionViewModelFactory
import com.example.agent.ui.theme.AgentTheme
import com.example.gemini.ui.permission.PermissionViewModel

sealed class Screen(val route: String) {
    data object PermissionScreen : Screen("permission_screen")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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