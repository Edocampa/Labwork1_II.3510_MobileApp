package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.CourseWithTeacher
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet

/**
 * StudentCoursesViewModel - Manages student course browsing and enrollment
 */
@HiltViewModel
class StudentCoursesViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseWithTeacher>>(emptyList())
    val courses: StateFlow<List<CourseWithTeacher>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _enrolledCourseIds = MutableStateFlow<Set<Int>>(emptySet())
    val enrolledCourseIds: StateFlow<Set<Int>> = _enrolledCourseIds.asStateFlow()

    init {
        loadCourses()
        loadEnrolledCourses()
    }

    private fun loadCourses() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value

            if (currentUser?.level != null) {
                _courses.value = repository.getCoursesByLevel(currentUser.level)
            } else {
                _courses.value = repository.getCoursesWithTeachers()
            }
        } catch (e: Exception) {
            _message.value = "Error loading courses: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    private fun loadEnrolledCourses() = viewModelScope.launch {
        val currentUser = authRepository.currentUser.value
        if (currentUser != null) {
            try {
                val student = repository.getStudentByUserId(currentUser.idUser)
                if (student != null) {
                    repository.getSubscribesByStudent(student.idStudent).collect { subscriptions ->
                        val courseIdsList = mutableListOf<Int>()
                        for (subscription in subscriptions) {
                            courseIdsList.add(subscription.courseId)
                        }
                        _enrolledCourseIds.value = courseIdsList.toSet()
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error loading enrollments: ${e.message}"
            }
        }
    }

    fun enrollInCourse(courseId: Int) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val student = repository.getStudentByUserId(currentUser.idUser)
                if (student != null) {
                    // Check if already enrolled
                    val isAlreadyEnrolled = repository.isStudentEnrolled(student.idStudent, courseId)
                    if (isAlreadyEnrolled) {
                        _message.value = "Already enrolled in this course"
                    } else {
                        // Create new subscription
                        repository.enrollStudent(student.idStudent, courseId)
                        _message.value = "Enrolled successfully!"
                    }
                }
            }
        } catch (e: Exception) {
            _message.value = "Error enrolling: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
