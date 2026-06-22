package com.theultimatenote.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.theultimatenote.app.ui.screens.auth.AuthViewModel
import com.theultimatenote.app.ui.screens.auth.ForgotPasswordScreen
import com.theultimatenote.app.ui.screens.auth.LoginScreen
import com.theultimatenote.app.ui.screens.auth.SignUpScreen
import com.theultimatenote.app.ui.screens.daily.DailyScreen
import com.theultimatenote.app.ui.screens.home.HomeScreen
import com.theultimatenote.app.ui.screens.notebooks.NotebooksScreen
import com.theultimatenote.app.ui.screens.projects.KanbanBoardScreen
import com.theultimatenote.app.ui.screens.projects.KanbanViewModel
import com.theultimatenote.app.ui.screens.projects.ProjectsScreen
import com.theultimatenote.app.ui.screens.profile.ProfileScreen
import com.theultimatenote.app.ui.screens.chat.ChatScreen
import com.theultimatenote.app.ui.screens.stats.StatsScreen
import kotlinx.serialization.Serializable
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

@Serializable data object LoginRoute
@Serializable data object SignUpRoute
@Serializable data object ForgotPasswordRoute
@Serializable data object HomeRoute
@Serializable data object ProjectsRoute
@Serializable data class KanbanBoardRoute(val projectId: String, val projectName: String, val projectType: String = "REGULAR")
@Serializable data object DailyRoute
@Serializable data object NotebooksRoute
@Serializable data object ProfileRoute
@Serializable data object ChatRoute
@Serializable data object StatsRoute

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
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()

    Crossfade(targetState = authState, animationSpec = tween(300)) { state ->
        when (state) {
            AuthViewModel.AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            AuthViewModel.AuthState.Authenticated -> MainNavigation(authViewModel)
            AuthViewModel.AuthState.Unauthenticated -> AuthNavigation(authViewModel)
        }
    }
}

@Composable
private fun AuthNavigation(authViewModel: AuthViewModel) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        enterTransition = { slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { slideInHorizontally(tween(250)) { -it / 3 } + fadeIn(tween(200)) },
        popExitTransition = { slideOutHorizontally(tween(250)) { it / 3 } + fadeOut(tween(150)) },
    ) {
        composable<LoginRoute> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { authViewModel.clearState(); navController.navigate(SignUpRoute) },
                onNavigateToForgotPassword = { authViewModel.clearState(); navController.navigate(ForgotPasswordRoute) },
            )
        }
        composable<SignUpRoute> {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateBack = { authViewModel.clearState(); navController.popBackStack() },
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { authViewModel.clearState(); navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainNavigation(authViewModel: AuthViewModel) {
    val navController: NavHostController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(150)) { it } + fadeOut(tween(150)),
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surface,
                                unselectedIconColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(150)) },
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onSignOut = { authViewModel.signOut() },
                    onNavigateToProfile = { navController.navigate(ProfileRoute) },
                    onNavigateToChat = { navController.navigate(ChatRoute) },
                    onNavigateToStats = { navController.navigate(StatsRoute) },
                )
            }
            composable<ProjectsRoute> {
                ProjectsScreen(
                    onNavigateToBoard = { projectId, projectName, projectType ->
                        navController.navigate(KanbanBoardRoute(projectId, projectName, projectType))
                    },
                )
            }
            composable<KanbanBoardRoute>(
                enterTransition = { slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(200)) },
                popExitTransition = { slideOutHorizontally(tween(250)) { it / 3 } + fadeOut(tween(150)) },
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<KanbanBoardRoute>()
                val koin = getKoin()
                val kanbanViewModel: KanbanViewModel = remember(route.projectId) {
                    koin.get { org.koin.core.parameter.parametersOf(route.projectId) }
                }
                KanbanBoardScreen(
                    viewModel = kanbanViewModel,
                    projectName = route.projectName,
                    onNavigateBack = { navController.popBackStack() },
                    isDailyProject = route.projectType == "DAILY",
                )
            }
            composable<DailyRoute> { DailyScreen() }
            composable<NotebooksRoute> { NotebooksScreen() }
            composable<ProfileRoute>(
                enterTransition = { slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(200)) },
                popExitTransition = { slideOutHorizontally(tween(250)) { it / 3 } + fadeOut(tween(150)) },
            ) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSignOut = { authViewModel.signOut() },
                )
            }
            composable<ChatRoute>(
                enterTransition = { slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(200)) },
                popExitTransition = { slideOutHorizontally(tween(250)) { it / 3 } + fadeOut(tween(150)) },
            ) {
                ChatScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable<StatsRoute>(
                enterTransition = { slideInHorizontally(tween(250)) { it / 3 } + fadeIn(tween(200)) },
                popExitTransition = { slideOutHorizontally(tween(250)) { it / 3 } + fadeOut(tween(150)) },
            ) {
                StatsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
