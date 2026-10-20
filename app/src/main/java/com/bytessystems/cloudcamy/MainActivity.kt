package com.bytessystems.cloudcamy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bytessystems.cloudcamy.ui.camera.CameraScreen
import com.bytessystems.cloudcamy.ui.camera.CameraViewModel
import com.bytessystems.cloudcamy.ui.details.TakeDetailsScreen
import com.bytessystems.cloudcamy.ui.details.TakeDetailsViewModel
import com.bytessystems.cloudcamy.ui.history.TakeHistoryScreen
import com.bytessystems.cloudcamy.ui.history.TakeHistoryViewModel
import com.bytessystems.cloudcamy.ui.home.HomeScreen
import com.bytessystems.cloudcamy.ui.home.HomeViewModel
import com.bytessystems.cloudcamy.ui.navigation.Screen
import com.bytessystems.cloudcamy.ui.settings.SettingsScreen
import com.bytessystems.cloudcamy.ui.settings.SettingsViewModel
import com.bytessystems.cloudcamy.ui.theme.CloudCamyTheme
import com.bytessystems.cloudcamy.ui.theme.Slate950

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudCamyTheme {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        screenStack = screenStack + screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        }
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is Screen.Home -> {
                    val homeViewModel: HomeViewModel = viewModel()
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToCamera = { navigateTo(Screen.Camera) },
                        onNavigateToHistory = { navigateTo(Screen.History) },
                        onNavigateToTakeDetails = { takeId -> navigateTo(Screen.TakeDetails(takeId)) },
                        onNavigateToSettings = { navigateTo(Screen.Settings) }
                    )
                }

                is Screen.Camera -> {
                    val cameraViewModel: CameraViewModel = viewModel()
                    CameraScreen(
                        viewModel = cameraViewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }

                is Screen.History -> {
                    val historyViewModel: TakeHistoryViewModel = viewModel()
                    TakeHistoryScreen(
                        viewModel = historyViewModel,
                        onNavigateBack = { navigateBack() },
                        onNavigateToDetails = { takeId -> navigateTo(Screen.TakeDetails(takeId)) }
                    )
                }

                is Screen.TakeDetails -> {
                    val detailsViewModel: TakeDetailsViewModel = viewModel(
                        key = "details_${screen.takeId}",
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return TakeDetailsViewModel(screen.takeId) as T
                            }
                        }
                    )
                    TakeDetailsScreen(
                        viewModel = detailsViewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }

                is Screen.Settings -> {
                    val settingsViewModel: SettingsViewModel = viewModel()
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
            }
        }
    }
}