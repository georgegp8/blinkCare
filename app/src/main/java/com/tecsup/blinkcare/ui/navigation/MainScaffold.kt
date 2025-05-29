package com.tecsup.blinkcare.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.tecsup.blinkcare.blink.presentation.screens.DispositivoCrudView
import com.tecsup.blinkcare.blink.presentation.screens.PerfilScreen
import com.tecsup.blinkcare.blink.presentation.screens.ParpadeoScreen
import com.tecsup.blinkcare.blink.presentation.screens.LoginGoogleScreen
import com.tecsup.blinkcare.blink.presentation.screens.RegisterScreen
import com.tecsup.blinkcare.ui.navigation.model.BottomNavItem
import com.tecsup.blinkcare.ui.navigation.model.bottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BlinkCare") }
            )
        },
        bottomBar = {
            val route = currentDestination?.destination?.route ?: ""
            if (route in bottomNavItems.map { it.route }) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = route == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dispositivos",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dispositivos") { DispositivoCrudView() }
            composable("parpadeos") { ParpadeoScreen() }
            composable("perfil") { PerfilScreen() }
            composable("login") { LoginGoogleScreen(navController, {}) }
            composable("register") { RegisterScreen(navController, {}) }
        }
    }
}