package com.tecsup.blinkcare.ui.navigation.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person


data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("dispositivos", Icons.Default.Home, "Dispositivos"),
    BottomNavItem("parpadeos", Icons.Default.Face ,"Parpadeos"),
    BottomNavItem("perfil", Icons.Default.Person, "Perfil")
)