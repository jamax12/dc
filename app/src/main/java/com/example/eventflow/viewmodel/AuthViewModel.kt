package com.example.eventflow.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.eventflow.data.AuthRepository
import com.example.eventflow.navigation.ROUTE_LOGIN
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // State flows for loading and error messages
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun signUp(email: String, password: String, name: String, navController: NavController, context: Context) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.signUp(email, password, name)
            _isLoading.value = false
            if (result.isSuccess) {
                Toast.makeText(context, "Sign up successful", Toast.LENGTH_LONG).show()
                navController.navigate("dashboard")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Sign up failed"
            }
        }
    }

    suspend fun login(email: String, password: String, navController: NavController, context: Context) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                Toast.makeText(context, "Login successful", Toast.LENGTH_LONG).show()
                navController.navigate("dashboard")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun logout(navController: NavController, context: Context) {
        repository.logout()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_LONG).show()
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        email: String,
        password: String?
    ): Result<Unit> {
        return try {
            val userId = repository.getCurrentUserId() ?: throw Exception("User not logged in")
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.updateUserProfile(userId, displayName, email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Update failed"
                Result.failure(result.exceptionOrNull() ?: Exception("Update failed"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message ?: "Update failed"
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccount(password: String): Result<Unit> {
        return try {
            val userId = repository.getCurrentUserId() ?: throw Exception("User not logged in")
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.deleteUserAccount(userId, password)
            _isLoading.value = false
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Delete failed"
                Result.failure(result.exceptionOrNull() ?: Exception("Delete failed"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message ?: "Delete failed"
            Result.failure(e)
        }
    }
}