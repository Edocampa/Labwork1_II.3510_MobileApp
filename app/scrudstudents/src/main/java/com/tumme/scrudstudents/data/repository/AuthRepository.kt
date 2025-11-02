package com.tumme.scrudstudents.data.repository

import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.local.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.TeacherEntity

/**
 * AuthRepository - Manages authentication and user session state
 *
 * This repository handles all authentication operations including registration,
 * login and logout
 *
 * Architecture Pattern:
 * - Singleton: Only one instance exists throughout the app lifecycle
 * - StateFlow: Reactive state management for current user
 * - Dependency Injection: Injected by Hilt into ViewModels
 *
 * Responsibilities:
 * - User registration (creates User + Student/Teacher profile)
 * - User authentication (login/logout)
 * - Session management (tracks current logged-in user)
 * - Role-based profile creation
 *
 * @param scrudRepository Main repository for database operations
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
        level: String?
    ): Result<Unit> {
        return try {

            // Check if email exists
            if (scrudRepository.getUserByEmail(email) != null) {
                return Result.failure(Exception("Email already exists"))
            }

            // Validate student level
            if (role == UserRole.STUDENT && level.isNullOrBlank()) {
                return Result.failure(Exception("Level required for students"))
            }

            // Create user
            val user = User(
                email = email,
                password = password,
                role = role,
                level = level
            )
            val userId = scrudRepository.registerUser(user).toInt()

            if (role == UserRole.STUDENT && level != null) {
                val student = StudentEntity(
                    userId = userId,
                    firstName = email.substringBefore("@"),
                    lastName = "",
                    level = level
                )
                scrudRepository.insertStudent(student)
            }

            if (role == UserRole.TEACHER) {
                val teacher = TeacherEntity(
                    userId = userId,
                    firstName = email.substringBefore("@"),  // Temporary name
                    lastName = ""
                )
                scrudRepository.insertTeacher(teacher)
            }

            Result.success(Unit)
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
