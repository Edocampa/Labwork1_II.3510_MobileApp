package com.tumme.scrudstudents.ui.subscribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.dao.SubscribeWithDetails
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VIEWMODEL - Manages UI state and business logic for Subscribe screens
 *
 * This ViewModel handles subscriptions between students and courses
 *
 * Key responsibilities:
 * - Load all subscriptions with student/course details (for list screen)
 * - Load all students (for dropdown in form)
 * - Load all courses (for dropdown in form)
 * - Insert/update/delete subscriptions
 * - Validate subscribes data
 *
 * @param repo SCRUDRepository injected by Hilt
 */
@HiltViewModel
class SubscribeViewModel @Inject constructor(
    private val repo: SCRUDRepository
) : ViewModel() {

    /**
     * STATEFLOW - List of subscriptions with full details (for CHALLENGE)
     *
     * Contains student names and course titles instead of just IDs
     * Used by SubscribeListScreen to display user-friendly information
     *
     * Flow conversion:
     * - repo returns Flow from DAO
     * - stateIn() converts to StateFlow (hot flow with current value)
     * - Lazily starts when first subscriber appears
     * - Initial value is empty list
     */
    private val _subscribesWithDetails: StateFlow<List<SubscribeWithDetails>> =
        repo.getSubscribesWithDetails().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    val subscribesWithDetails: StateFlow<List<SubscribeWithDetails>> = _subscribesWithDetails

    /**
     * STATEFLOW - List of all students (for dropdown in form)
     *
     * Used in SubscribeFormScreen to let user select which student to enroll
     */
    private val _students: StateFlow<List<StudentEntity>> =
        repo.getAllStudents().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    val students: StateFlow<List<StudentEntity>> = _students

    /**
     * STATEFLOW - List of all courses (for dropdown in form)
     *
     * Used in SubscribeFormScreen to let user select which course to enroll in
     */
    private val _courses: StateFlow<List<CourseEntity>> =
        repo.getAllCourses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    val courses: StateFlow<List<CourseEntity>> = _courses

    /**
     * SHAREDFLOW - One-time events for UI (messages, errors)
     *
     * Used to show toast messages or snackbars:
     * - "Subscription created"
     * - "Score updated"
     * - "Error: Student already enrolled in this course"
     */
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    /**
     * Deletes a subscription (unenrolls student from course)
     *
     * @param subscribe The SubscribeEntity to delete
     *
     * Process:
     * 1. User clicks delete button
     * 2. ViewModel deletes from database
     * 3. Database change triggers Flow update
     * 4. UI automatically recomposes with updated list
     */
    fun deleteSubscribe(subscribe: SubscribeEntity) = viewModelScope.launch {
        repo.deleteSubscribe(subscribe)
        _events.emit("Enrollment deleted")
    }

    /**
     * Inserts a new subscription or updates existing one
     *
     * @param subscribe The SubscribeEntity to insert/update
     *
     * Validation:
     * - Score must be >= 0 and <= 20 (typical grading scale)
     * - Student and Course IDs must be valid (> 0)
     *
     * OnConflict.REPLACE in DAO means:
     * - If (idStudent, idCourse) exists → updates the score
     * - If (idStudent, idCourse) is new → creates new enrollment
     *
     * This allows updating a student's score for a course
     */
    fun insertSubscribe(subscribe: SubscribeEntity) = viewModelScope.launch {
        // Validation: Check if student ID is valid
        if (subscribe.studentId <= 0) {
            _events.emit("Error: Please select a student")
            return@launch
        }

        // Validation: Check if course ID is valid
        if (subscribe.courseId <= 0) {
            _events.emit("Error: Please select a course")
            return@launch
        }

        // Validation: Score must be between 0 and 20
        if (subscribe.score < 0 || subscribe.score > 20) {
            _events.emit("Error: Score must be between 0 and 20")
            return@launch
        }

        // All validations passed, insert/update subscription
        repo.insertSubscribe(subscribe)
        _events.emit("Enrollment saved")
    }

    /**
     * Gets subscriptions for a specific student
     *
     * @param studentId The student's ID
     * @return Flow<List<SubscribeEntity>> - All courses this student is enrolled in
     *
     * Use case: Show all courses for a specific student
     * Not used in main screens, but available for future features
     */
    fun getSubscriptionsByStudent(studentId: Int): Flow<List<SubscribeEntity>> =
        repo.getSubscribesByStudent(studentId)

    /**
     * Gets subscriptions for a specific course
     *
     * @param courseId The course's ID
     * @return Flow<List<SubscribeEntity>> - All students enrolled in this course
     *
     * Use case: Show all students in a specific course (class roster)
     * Not used in main screens, but available for future features
     */
    fun getSubscriptionsByCourse(courseId: Int): Flow<List<SubscribeEntity>> =
        repo.getSubscribesByCourse(courseId)
}
