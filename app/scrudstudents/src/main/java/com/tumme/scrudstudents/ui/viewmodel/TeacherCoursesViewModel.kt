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
import com.tumme.scrudstudents.R

sealed class TeacherCoursesMessage {
    data class Simple(val messageId: Int) : TeacherCoursesMessage()

    data class Dynamic(val baseMessageId: Int, val dynamicPart: String) : TeacherCoursesMessage()
}

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

    private val _message = MutableStateFlow<TeacherCoursesMessage?>(null)
    val message: StateFlow<TeacherCoursesMessage?> = _message.asStateFlow()

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
            _message.value = TeacherCoursesMessage.Dynamic(
                R.string.error_loading_courses,
                e.message ?: "Unknown error"
            )
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
            _message.value = TeacherCoursesMessage.Simple(R.string.course_deleted)
        } catch (e: Exception) {
            _message.value = TeacherCoursesMessage.Dynamic(
                R.string.error_deleting_course,
                e.message ?: "Unknown error"
            )
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
