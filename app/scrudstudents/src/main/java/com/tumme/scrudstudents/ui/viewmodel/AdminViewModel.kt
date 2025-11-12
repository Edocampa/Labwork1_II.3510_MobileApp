package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tumme.scrudstudents.R

sealed class AdminMessage {
    data class Simple(val messageId: Int) : AdminMessage()

    data class Dynamic(val baseMessageId: Int, val dynamicPart: String) : AdminMessage()
}

/**
 * AdminViewModel - Manage admin operations
 *
 * Handles system-wide operations:
 * - User management (view all users, delete users)
 * - Global statistics (students, teachers, courses, subscriptions)
 *
 * Admin operations:
 * - Cannot delete other admin accounts
 * - Cascade deletion removes all associated data (courses, grades)
 * - Statistics auto-refresh after user deletion
 */

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: SCRUDRepository
) : ViewModel() {

    val allUsers: StateFlow<List<User>> = MutableStateFlow(emptyList())

    // System statistics
    private val _statistics = MutableStateFlow(
        SCRUDRepository.AdminStatistics(0, 0, 0, 0)
    )
    val statistics: StateFlow<SCRUDRepository.AdminStatistics> = _statistics.asStateFlow()

    // Messages
    private val _message = MutableStateFlow<AdminMessage?>(null)
    val message: StateFlow<AdminMessage?> = _message.asStateFlow()

    init {
        loadUsers()
        loadStatistics()
    }

    /**
     * Load all users from database
     * Collects Flow and updates UI reactively
     */

    private fun loadUsers() = viewModelScope.launch {
        repository.getAllUsers().collect { users ->
            (allUsers as MutableStateFlow).value = users
        }
    }

    /**
    * Load system statistics
    * Calculates totals across all entities
    */

    private fun loadStatistics() = viewModelScope.launch {
        try {
            val stats = repository.getAdminStatistics()
            _statistics.value = stats
        } catch (e: Exception) {
            _message.value = AdminMessage.Dynamic(
                R.string.error_loading_stats,
                e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Delete user from system (Admin action)
     *
     * Deletes user and all associated data via cascade:
     * - User → Student/Teacher profile (CASCADE)
     * - Student → Enrollments (CASCADE)
     * - Teacher → Courses → Enrollments (CASCADE)
     *
     * Refreshes statistics after deletion
     *
     * @param userId ID of user to delete
     */

    fun deleteUser(userId: Int) = viewModelScope.launch {
        try {
            repository.deleteUser(userId)
            _message.value = AdminMessage.Simple(R.string.user_deleted_success)
            loadStatistics()
        } catch (e: Exception) {
            _message.value = AdminMessage.Dynamic(
                R.string.error_deleting_user,
                e.message ?: "Unknown error"
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}