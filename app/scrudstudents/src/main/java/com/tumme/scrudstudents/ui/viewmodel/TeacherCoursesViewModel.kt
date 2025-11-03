package com.tumme.scrudstudents.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.repository.AuthRepository
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TeacherCoursesViewModel - Manages courses taught by teacher
 *
 * Handles CRUD operations for teacher's courses:
 * - Read: Load courses taught by logged-in teacher
 * - Delete: Remove courses (with cascade to subscriptions)
 *
 * Business Logic:
 * - Filters courses by teacher ID
 * - Auto-reloads list after deletion
 * - Deletion cascades to subscriptions (removes student enrollments)
 *
 * Used by:
 * - TeacherCoursesScreen (view/edit/delete courses)
 * - TeacherCourseFormScreen (indirectly, triggers reload)
 *
 * @param repository Database operations
 * @param authRepository Current user information
 */
@HiltViewModel
class TeacherCoursesViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courses: StateFlow<List<CourseEntity>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadCourses()
    }

    /**
     * Load courses taught by current teacher
     *
     *
     * Filtering:
     * - Only courses where course.teacherId = teacher.idTeacher
     * - Other teachers' courses are not visible
     *
     */

    fun loadCourses() = viewModelScope.launch {
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
     * Delete a course
     *
     *
     * Side effects:
     * - Cascade deletion of subscriptions (students auto-unenrolled)
     * - Defined by Room's @ForeignKey onDelete = CASCADE
     *
     *
     * @param courseId ID of course to delete
     */

    fun deleteCourse(courseId: Int) = viewModelScope.launch {
        try {
            repository.deleteCourse(courseId)
            loadCourses() // Reload list
            _message.value = "Course deleted"
        } catch (e: Exception) {
            _message.value = "Error deleting course: ${e.message}"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
