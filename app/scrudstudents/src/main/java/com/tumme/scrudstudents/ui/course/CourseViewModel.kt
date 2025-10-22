package com.tumme.scrudstudents.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * COURSE VIEWMODEL - Manages UI state and business logic for Course screens
 *
 * Key responsabilities:
 * - Exposes course list as StateFlow for reactive UI updates
 * - Handles CRUD operations
 * - Validates business rules (ECTS > 0)
 * - Emits events for user feedback (success/error messages)
 * - Survives configuration changes (screen rotation)
 *
 * Architecture:
 * - Part of MVVM pattern (ViewModel layer)
 * - Communicates with Repository for data access
 * - UI observes StateFlow and SharedFlow for updates
 * - Business logic isolated from UI
 *
 * @param repo SCRUDRepository injected by Hilt
 * @see CourseListScreen observes courses StateFlow
 * @see CourseFormScreen calls insertCourse and observes events
 */
@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repo: SCRUDRepository
) : ViewModel() {

    /**
     * COURSES STATE - Reactive list of all courses
     *
     * StateFlow characteristics:
     * - Hot flow: Always active, maintains current value
     * - Reactive: Automatically emits when database changes
     * - Lifecycle-aware: Tied to viewModelScope
     *
     * stateIn() operator:
     * - Converts cold Flow (from Repo) to hot StateFlow
     * - scope: viewModelScope = cancelled when ViewModel destroyed
     * - started: SharingStarted.Lazily = starts when first collector subscribes
     * - initialValue: emptyList() = default value before first emission
     *
     * Data flow:
     * 1. Database changes (insert/update/delete)
     * 2. DAO emits new list via Flow
     * 3. Repository forwards Flow
     * 4. stateIn() converts to StateFlow
     * 5. UI collects with collectAsState()
     * 6. Compose recomposes with new data
     */
    private val _courses: StateFlow<List<CourseEntity>> =
        repo.getAllCourses().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val courses: StateFlow<List<CourseEntity>> = _courses

    /**
     * EVENTS FLOW - One-time UI events
     *
     * SharedFlow vs StateFlow:
     * - SharedFlow: for events that happen once
     * - StateFlow: for state that persists (list of courses)
     *
     * Why SharedFlow for events:
     * - Events should not be replayed on configuration change
     * - Each event consumed once
     * - No "current value" concept (events are temporal)
     *
     * MutableSharedFlow:
     * - Can emit values with emit() or tryEmit()
     * - No replay by default (new collectors don't receive old events)
     * - Used for side effects, not state
     */
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    /**
     * DELETE COURSE - Removes course from database
     *
     * Process:
     * 1. Launches coroutine in viewModelScope
     * 2. Calls Repository to delete course
     * 3. Repository calls DAO.delete()
     * 4. Room deletes from database
     * 5. Room's Flow detects change and emits updated list
     * 6. StateFlow updates automatically
     * 7. UI recomposes, course disappears from list
     * 8. Emits success event for user feedback
     *
     * Cascade behavior:
     * - Deleting course also deletes related enrollments (subscribes)
     * - Defined in SubscribeEntity foreign key with CASCADE
     *
     * viewModelScope.launch:
     * - Runs on Dispatchers.Main by default
     * - Repository suspend functions switch to Dispatchers.IO
     * - Cancelled automatically when ViewModel destroyed
     *
     * @param course CourseEntity to delete
     */
    fun deleteCourse(course: CourseEntity) = viewModelScope.launch {
        repo.deleteCourse(course)
        _events.emit("Course deleted")
    }

    /**
     * INSERT OR UPDATE COURSE - Saves course to database
     *
     * Dual mode (handled by Room's OnConflictStrategy.REPLACE):
     * - If idCourse exists → UPDATE existing course
     * - If idCourse is new → INSERT new course
     *
     * VALIDATION:
     * 1. ECTS must be > 0 (required by assignment)
     *    - If invalid: emit error event, abort save
     *    - Prevents saving courses with 0 or negative credits
     *
     * 2. Level validation (commented out):
     *    - Could validate against predefined levels
     *    - Not strictly necessary as dropdown enforces valid values
     *    - Kept as comment for reference
     *
     * Process on success:
     * 1. Validation passes
     * 2. Repository.insertCourse() called
     * 3. DAO inserts/updates in database
     * 4. Room's Flow emits updated list
     * 5. StateFlow updates automatically
     * 6. UI recomposes with new/updated course
     * 7. Emit success event
     * 8. UI navigates back (handled in screen)
     *
     * Error handling:
     * - Validation errors emit event but don't crash
     * - UI can collect events to show toast/snackbar
     * - User stays on form to correct errors
     *
     * @param course CourseEntity to insert or update
     */
    fun insertCourse(course: CourseEntity) = viewModelScope.launch {
        // CHALLENGE: ECTS validation (Part 2 requirement)
        if (course.ectsCourse <= 0) {
            _events.emit("Error: ECTS must be > 0")
            return@launch  // Abort save operation
        }

        // Level validation (optional, dropdown already enforces this)
        val validLevels = listOf("P1", "P2", "P3", "B1", "B2", "B3",
            "A1", "A2", "A3", "MS", "PhD")

        // Uncomment to add level validation:
        if (course.levelCourse !in validLevels) {
            _events.emit("Error: Invalid level")
            return@launch
        }


        // Validation passed, save to database
        repo.insertCourse(course)
        _events.emit("Course saved")
    }

    /**
     * FIND COURSE BY ID - Retrieves single course for editing
     *
     * Used by CourseFormScreen in edit mode:
     * 1. User clicks edit button on a course
     * 2. Navigation passes courseId to form
     * 3. Form calls findCourse(id) in LaunchedEffect
     * 4. Returns course data to pre-populate form fields
     *
     * Suspend function:
     * - Must be called from coroutine (LaunchedEffect)
     * - Runs on IO dispatcher (Room handles threading)
     * - Returns CourseEntity? (nullable if not found)
     *
     * Why suspend:
     * - Database access is I/O operation
     * - Should not block main thread
     * - Room requires suspend for single queries
     *
     * @param id Course ID (idCourse)
     * @return CourseEntity if found, null if not found
     */
    suspend fun findCourse(id: Int) = repo.getCourseById(id)
}
