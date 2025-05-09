package com.example.eventflow.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eventflow.screens.auth.LoginScreen
import com.example.eventflow.screens.auth.SignUpScreen
import com.example.eventflow.screens.dashboard.DashboardScreen
import com.example.eventflow.screens.event.AddEditEventScreen
import com.example.eventflow.screens.event.EventListScreen
import com.example.eventflow.screens.profile.ProfileScreen
import com.example.eventflow.viewmodel.AuthViewModel
import com.example.eventflow.viewmodel.EventViewModel

@Composable
fun NavGraph(navController: NavHostController, authViewModel: AuthViewModel, eventViewModel: EventViewModel) {

        NavHost(
            navController = navController,
            startDestination = ROUTE_LOGIN,
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(navController, authViewModel)
            }
            composable(ROUTE_SIGNUP) {
                SignUpScreen(navController, authViewModel)
            }
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(navController, authViewModel, eventViewModel)
            }
            composable(ROUTE_EVENT_LIST) {
                EventListScreen(navController, eventViewModel)
            }
            composable("$ROUTE_ADD_EDIT_EVENT?eventId={eventId}") { backStackEntry ->
                AddEditEventScreen(navController, eventViewModel)
            }
            composable(ROUTE_PROFILE) {
                ProfileScreen(navController, authViewModel)
            }
        }
    }


data class NavItem(val label: String, val icon: ImageVector, val route: String)