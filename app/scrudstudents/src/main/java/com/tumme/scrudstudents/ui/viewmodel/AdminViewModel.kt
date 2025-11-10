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
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadUsers()
        loadStatistics()
    }

    private fun loadUsers() = viewModelScope.launch {
        repository.getAllUsers().collect { users ->
            (allUsers as MutableStateFlow).value = users
        }
    }

    private fun loadStatistics() = viewModelScope.launch {
        try {
            val stats = repository.getAdminStatistics()
            _statistics.value = stats
        } catch (e: Exception) {
            _message.value = "Error loading statistics: ${e.message}"
        }
    }

    fun deleteUser(userId: Int) = viewModelScope.launch {
        try {
            repository.deleteUser(userId)
            _message.value = "User deleted successfully"
            loadStatistics()
        } catch (e: Exception) {
            _message.value = "Error deleting user: ${e.message}"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}