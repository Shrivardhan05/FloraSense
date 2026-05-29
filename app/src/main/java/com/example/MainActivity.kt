package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GardenDashboardScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.ToxicityCheckerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FloraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FloraViewModel = viewModel()
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Filled.Spa else Icons.Outlined.Spa,
                                        contentDescription = "My Garden"
                                    )
                                },
                                label = { Text("My Garden") },
                                modifier = Modifier.testTag("nav_tab_garden")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                                        contentDescription = "AI Scanner"
                                    )
                                },
                                label = { Text("AI Scanner") },
                                modifier = Modifier.testTag("nav_tab_scanner")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Filled.HealthAndSafety else Icons.Outlined.HealthAndSafety,
                                        contentDescription = "Safety Check"
                                    )
                                },
                                label = { Text("Safety Check") },
                                modifier = Modifier.testTag("nav_tab_safety")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == 3) Icons.Filled.MilitaryTech else Icons.Outlined.MilitaryTech,
                                        contentDescription = "Pro Benefits"
                                    )
                                },
                                label = { Text("Go Pro") },
                                modifier = Modifier.testTag("nav_tab_pro")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> GardenDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToScan = { selectedTab = 1 },
                                onNavigateToPro = { selectedTab = 3 }
                            )
                            1 -> ScannerScreen(
                                viewModel = viewModel,
                                onNavigateBack = { selectedTab = 0 }
                            )
                            2 -> ToxicityCheckerScreen()
                            3 -> PaywallScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
