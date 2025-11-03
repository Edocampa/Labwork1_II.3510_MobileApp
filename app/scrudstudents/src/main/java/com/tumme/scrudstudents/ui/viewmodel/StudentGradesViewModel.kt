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
 * StudentGradesViewModel - Manages student grades display
 *
 * Displays only courses where grades have been assigned by teachers
 * Filters out courses with score = 0 (not yet graded)
 *
 * Difference from SubscriptionsViewModel:
 * - Grades: Only score > 0 (graded courses)
 * - Subscriptions: All enrollments (score >= 0)
 *
 * Use case --> Student wants to see only courses with final grades,
 * not pending enrollments
 *
 * @param repository Database operations
 * @param authRepository Current user information
 */
@HiltViewModel
class StudentGradesViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _gradedCourses = MutableStateFlow<List<SubscribeWithCourseAndTeacher>>(emptyList())
    val gradedCourses: StateFlow<List<SubscribeWithCourseAndTeacher>> = _gradedCourses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadGrades()
    }

    /**
     * Load student's graded courses
     *
     * Filtering logic:
     * - score = 0 → Not graded yet (excluded)
     * - score > 0 → Graded (included)
     *
     * This ensures students only see courses where teachers
     * have assigned final grades
     */

    private fun loadGrades() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val student = repository.getStudentByUserId(currentUser.idUser)
                if (student != null) {
                    val allSubscriptions = repository.getStudentSubscriptionsWithDetails(student.idStudent)
                    // Filter only courses with grades (score > 0)
                    _gradedCourses.value = allSubscriptions.filter { it.subscribe.score > 0f }
                }
            }
        } catch (e: Exception) {
            _message.value = "Error loading grades: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
