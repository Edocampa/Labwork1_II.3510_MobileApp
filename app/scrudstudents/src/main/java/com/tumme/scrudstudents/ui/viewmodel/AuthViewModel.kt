package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.local.model.UserRole
import com.tumme.scrudstudents.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ViewModel - Manages authentication UI state
 *
 * Handles login and register flows, communicates with AuthRepository
 * Provides UI state (loading, errors, success)
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Current user state (from repository)
    val currentUser: StateFlow<User?> = authRepository.currentUser

    // Loading state for UI (show/hide progress indicator)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // One-time events for UI (errors, success messages)
    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    /**
     * Login user with email and password
     *
     * Validates input, calls repository, emits events for UI
     */
    fun login(email: String, password: String) = viewModelScope.launch {
        // Input validation
        if (email.isBlank() || password.isBlank()) {
            _events.emit(AuthEvent.Error("Email and password required"))
            return@launch
        }

        // Show loading indicator
        _isLoading.value = true

        // Call repository
        val result = authRepository.login(email, password)

        // Hide loading indicator
        _isLoading.value = false

        // Handle result
        if (result.isSuccess) {
            _events.emit(AuthEvent.LoginSuccess(result.getOrNull()!!))
        } else {
            _events.emit(AuthEvent.Error(result.exceptionOrNull()?.message ?: "Login failed"))
        }
    }

    /**
     * Register new user
     *
     * Validates input (email format, password length, required fields),
     * calls repository, emits events for UI
     *
     * @param email User email address
     * @param password User password
     * @param role User role (STUDENT or TEACHER)
     * @param level Study level (required only for STUDENT)
     */
    fun register(
        email: String,
        password: String,
        role: UserRole,
        level: String? = null
    ) = viewModelScope.launch {
        // Input validation
        if (email.isBlank() || password.isBlank()) {
            _events.emit(AuthEvent.Error("Email and password required"))
            return@launch
        }

        // Students must have a level
        if (role == UserRole.STUDENT && level.isNullOrBlank()) {
            _events.emit(AuthEvent.Error("Level required for students"))
            return@launch
        }

        // Email format validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _events.emit(AuthEvent.Error("Invalid email format"))
            return@launch
        }

        // Password length validation
        if (password.length < 4) {
            _events.emit(AuthEvent.Error("Password must be at least 4 characters"))
            return@launch
        }

        // Show loading indicator
        _isLoading.value = true

        // Call repository
        val result = authRepository.register(email, password, role, level)

        // Hide loading indicator
        _isLoading.value = false

        // Handle result
        if (result.isSuccess) {
            _events.emit(AuthEvent.RegisterSuccess)
        } else {
            _events.emit(AuthEvent.Error(result.exceptionOrNull()?.message ?: "Registration failed"))
        }
    }

    /**
     * Logout current user
     * Clears authentication state in repository
     */
    fun logout() {
        authRepository.logout()
    }

    /**
     * Check if a user is currently logged in
     * @return true if user is authenticated
     */
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    /**
     * Get role of current authenticated user
     * @return UserRole (STUDENT or TEACHER) or null if not logged in
     */
    fun getCurrentUserRole(): UserRole? = authRepository.getCurrentUserRole()
}

/**
 * Auth Events - One-time UI events
 *
 * Used for showing messages, navigating after success, etc
 * These are NOT state (they happen once), they're events
 */
sealed class AuthEvent {
    data class LoginSuccess(val user: User) : AuthEvent()
    object RegisterSuccess : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}
