package com.tumme.scrudstudents.data.repository

import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.local.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth Repository - Manages authentication state
 *
 * It manages login, register, logout and current user state
 */
@Singleton
class AuthRepository @Inject constructor(
    private val scrudRepository: SCRUDRepository
) {
    // Current authenticated user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Register new user
     * Returns: Success (userId) or Error message
     */
    suspend fun register(
        email: String,
        password: String,
        role: UserRole,
        level: String? = null
    ): Result<Long> {
        return try {
            // Check if email already exists
            val existing = scrudRepository.findUserByEmail(email)
            if (existing != null) {
                return Result.failure(Exception("Email already registered"))
            }

            // Create user
            val user = User(
                email = email,
                password = password,
                role = role,
                level = if (role == UserRole.STUDENT) level else null
            )

            val userId = scrudRepository.registerUser(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login user
     * Returns: Success (User) or Error message
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = scrudRepository.login(email, password)
            if (user != null) {
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        _currentUser.value = null
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = _currentUser.value != null

    /**
     * Get current user role
     */
    fun getCurrentUserRole(): UserRole? = _currentUser.value?.role
}
