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
 * TeacherStudentsViewModel - View enrolled students per course
 *
 * Two-step workflow:
 * 1. Teacher selects a course from their taught courses
 * 2. View students enrolled in that course with statistics
 *
 * Features:
 * - Course selection
 * - Student list with statistics
 * - Course statistics (total, graded, average)
 * - Read-only view (no grade input)
 *
 * Difference from EnterGradesViewModel:
 * - Students: Read-only with stats (overview)
 * - EnterGrades: Editable grades (action)
 *
 * Use case --> Teacher wants to see enrolled students and
 * course performance metrics without editing grades
 *
 * @param repository Database operations
 * @param authRepository Current user information
 *
 */
@HiltViewModel
class TeacherStudentsViewModel @Inject constructor(
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

    // Stats
    private val _totalStudents = MutableStateFlow(0)
    val totalStudents: StateFlow<Int> = _totalStudents.asStateFlow()

    private val _gradedStudents = MutableStateFlow(0)
    val gradedStudents: StateFlow<Int> = _gradedStudents.asStateFlow()

    private val _averageGrade = MutableStateFlow(0f)
    val averageGrade: StateFlow<Float> = _averageGrade.asStateFlow()

    init {
        loadTeacherCourses()
    }

    /**
     * Load courses taught by current teacher
     *
     * Shows list of courses teacher can view students for
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

    fun clearSelection() {
        _selectedCourse.value = null
        _students.value = emptyList()
        _totalStudents.value = 0
        _gradedStudents.value = 0
        _averageGrade.value = 0f
    }

    /**
     * Select a course and load students with statistics
     *
     * Loads student list and calculates course statistics
     *
     * Statistics calculated:
     * 1. Total students: All enrolled students
     * 2. Graded students: Students with score > 0
     * 3. Average grade: Mean of all graded students
     *
     * @param course Course to view students for
     */

    fun selectCourse(course: CourseEntity) = viewModelScope.launch {
        _selectedCourse.value = course
        _isLoading.value = true
        try {
            val studentsList = repository.getStudentsInCourseWithGrades(course.idCourse)
            _students.value = studentsList

            // Calculate stats
            _totalStudents.value = studentsList.size
            val graded = studentsList.filter { it.currentScore > 0 }
            _gradedStudents.value = graded.size

            if (graded.isNotEmpty()) {
                _averageGrade.value = graded.map { it.currentScore }.average().toFloat()
            } else {
                _averageGrade.value = 0f
            }
        } catch (e: Exception) {
            _message.value = "Error loading students: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}