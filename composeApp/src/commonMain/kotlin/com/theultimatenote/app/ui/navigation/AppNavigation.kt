package com.theultimatenote.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.theultimatenote.app.ui.screens.daily.DailyScreen
import com.theultimatenote.app.ui.screens.home.HomeScreen
import com.theultimatenote.app.ui.screens.notebooks.NotebooksScreen
import com.theultimatenote.app.ui.screens.projects.ProjectsScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object ProjectsRoute
@Serializable data object DailyRoute
@Serializable data object NotebooksRoute

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, HomeRoute),
    BottomNavItem("Projects", Icons.Default.Folder, ProjectsRoute),
    BottomNavItem("Daily", Icons.Default.CalendarToday, DailyRoute),
    BottomNavItem("Notebooks", Icons.AutoMirrored.Default.MenuBook, NotebooksRoute),
)

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hasRoute(item.route::class) == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<HomeRoute> { HomeScreen() }
            composable<ProjectsRoute> { ProjectsScreen() }
            composable<DailyRoute> { DailyScreen() }
            composable<NotebooksRoute> { NotebooksScreen() }
        }
    }
}
