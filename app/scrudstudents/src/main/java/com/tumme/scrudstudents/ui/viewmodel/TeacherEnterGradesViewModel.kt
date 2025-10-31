package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentWithGrade
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TeacherEnterGradesViewModel - Manages grade entry for students
 */
@HiltViewModel
class TeacherEnterGradesViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courses: StateFlow<List<CourseEntity>> = _courses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseEntity?>(null)
    val selectedCourse: StateFlow<CourseEntity?> = _selectedCourse.asStateFlow()

    private val _students = MutableStateFlow<List<StudentWithGrade>>(emptyList())
    val students: StateFlow<List<StudentWithGrade>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadTeacherCourses()
    }

    private fun loadTeacherCourses() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val teacher = repository.getTeacherByUserId(currentUser.idUser)
                if (teacher != null) {
                    _courses.value = repository.getCoursesByTeacher(teacher.idTeacher)
                }
            }
        } catch (e: Exception) {
            _message.value = "Error loading courses: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun selectCourse(course: CourseEntity) = viewModelScope.launch {
        _selectedCourse.value = course
        _isLoading.value = true
        try {
            _students.value = repository.getStudentsInCourseWithGrades(course.idCourse)
        } catch (e: Exception) {
            _message.value = "Error loading students: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun updateGrade(subscribeId: Int, score: Float) = viewModelScope.launch {
        if (score < 0 || score > 20) {
            _message.value = "Grade must be between 0 and 20"
            return@launch
        }

        try {
            repository.updateStudentGrade(subscribeId, score)
            // Reload students to show updated grade
            _selectedCourse.value?.let { course ->
                _students.value = repository.getStudentsInCourseWithGrades(course.idCourse)
            }
            _message.value = "Grade updated successfully"
        } catch (e: Exception) {
            _message.value = "Error updating grade: ${e.message}"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
