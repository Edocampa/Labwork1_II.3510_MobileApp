package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.SubscribeWithCourseAndTeacher
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * StudentSubscriptionsViewModel - Manages student course enrollments
 *
 * Displays ALL courses the student is enrolled in, regardless of grade status
 * Shows complete enrollment section with grade status for each course
 *
 * Purpose: Show student their complete course list
 *
 * Difference from GradesViewModel:
 * - Subscriptions: All enrollments (score >= 0)
 * - Grades: Only graded courses (score > 0)
 *
 * Use case --> Student wants to see all their courses, including:
 * - Courses with grades assigned (score > 0)
 * - Courses not yet graded (score = 0)
 *
 * This provides a complete overview of academic progress
 *
 * @param repository Database operations
 * @param authRepository Current user information
 */

@HiltViewModel
class StudentSubscriptionsViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _subscriptions = MutableStateFlow<List<SubscribeWithCourseAndTeacher>>(emptyList())
    val subscriptions: StateFlow<List<SubscribeWithCourseAndTeacher>> = _subscriptions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadSubscriptions()
    }

    /**
     * Load all student course subscriptions
     *
     *
     * No filtering applied - returns complete enrollment list:
     * - Courses with grades (score > 0) → Shows grade badge
     * - Courses without grades (score = 0) → Shows "Not graded yet" chip
     *
     * This gives students full visibility of their academic workload
     */

    private fun loadSubscriptions() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val student = repository.getStudentByUserId(currentUser.idUser)
                if (student != null) {
                    _subscriptions.value = repository.getStudentSubscriptionsWithDetails(student.idStudent)
                }
            }
        } catch (e: Exception) {
            _message.value = "Error loading subscriptions: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
