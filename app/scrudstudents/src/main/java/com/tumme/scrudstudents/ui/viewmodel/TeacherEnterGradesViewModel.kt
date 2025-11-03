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
 *
 * Two-step workflow:
 * 1. Teacher selects a course from their taught courses
 * 2. View students enrolled in that course and assign/update grades
 *
 * Features:
 * - Course selection
 * - Student list with grade input
 * - Grade validation (0-20 range)
 * - Real-time grade updates
 * - Two-level navigation (students → courses)
 *
 * Business Logic:
 * - Only teacher's own courses are shown
 * - Grades validated before saving (0-20)
 * - Auto-reloads student list after grade update
 *
 * @param repository Database operations
 * @param authRepository Current user information
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

    /**
     * Load courses taught by current teacher
     *
     * Shows list of courses teacher can assign grades for
     *
     */

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

    /**
     * Select a course and load enrolled students
     *
     * Transitions from course selection to student list
     *
     *
     * Each student shows:
     * - Student info (name, email, level)
     * - Current grade (if assigned)
     * - Input field to update grade
     *
     * @param course Course to assign grades for
     */

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

    /**
     * Clear course selection
     *
     * Navigation: Student list → Course selection
     *
     * Resets:
     * - Selected course to null
     * - Student list to empty
     * - Back to course selection screen
     */

    fun clearSelection() {
        _selectedCourse.value = null
        _students.value = emptyList()
    }

    /**
     * Update a student's grade
     *
     * Validates grade range (0-20) and updates database
     * Reloads student list to show updated grade immediately
     *
     *
     * Validation:
     * - score < 0 → Error message
     * - score > 20 → Error message
     * - 0 ≤ score ≤ 20 → Valid, update database
     *
     * score = 0 is valid (means "not graded yet")
     *
     * @param subscribeId ID of subscription to update
     * @param score New grade (0-20)
     */

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
