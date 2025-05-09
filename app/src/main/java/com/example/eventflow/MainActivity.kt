package com.example.eventflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.eventflow.navigation.NavGraph
import com.example.eventflow.ui.theme.EventFlowTheme
import com.example.eventflow.viewmodel.AuthViewModel
import com.example.eventflow.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventFlowApp()
        }
    }
}

@Composable
fun EventFlowApp() {
    EventFlowTheme {
        val navController = rememberNavController()
        // Use viewModel() to create ViewModels with proper lifecycle
        val authViewModel: AuthViewModel = viewModel()
        val eventViewModel: EventViewModel = viewModel()
        NavGraph(navController, authViewModel, eventViewModel)
    }
}