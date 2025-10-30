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
 * Shows only courses with grades assigned (score > 0)
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
