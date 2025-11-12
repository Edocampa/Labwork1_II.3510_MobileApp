package com.tumme.scrudstudents.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.tumme.scrudstudents.R

sealed class StudentListEvent {
    data class ShowMessage(val messageId: Int) : StudentListEvent()
}

/**
 * VIEWMODEL - Manages UI state and business logic for the Student List screen
 *
 * The ViewModel:
 * - Holds and manages UI-related data (student list)
 * - Survives configuration changes
 * - Communicates with Repo to fetch/modify data
 * - Exposes data to UI using reactive Flows
 * - Handles user actions
 *
 * MVVM Architecture:
 * View (StudentListScreen) → ViewModel → Repository → DAO → Database
 *
 * @param repo SCRUDRepository injected by Hilt for data operations
 *
 * @HiltViewModel tells Hilt to provide this ViewModel to Composables
 * @Inject constructor tells Hilt how to create this ViewModel
 */
@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val repo: SCRUDRepository
) : ViewModel() {

    /**
     * STATEFLOW - Holds the current list of students
     *
     * Flow conversion:
     * 1. repo.getAllStudents() returns Flow<List<StudentEntity>>
     * 2. stateIn() converts it to StateFlow
     * 3. viewModelScope = coroutine scope tied to ViewModel lifecycle
     * 4. SharingStarted.Lazily = starts collecting when first subscriber appears
     * 5. emptyList() = initial value before database data arrives
     *
     * Private _students: Mutable version (only ViewModel can modify)
     */
    private val _students: StateFlow<List<StudentEntity>> =
        repo.getAllStudents().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    /**
     * Public read-only StateFlow for UI observation
     *
     * UI collects this with: val students by viewModel.students.collectAsState()
     * When database changes → Flow emits new list → UI recomposes automatically
     */
    val students: StateFlow<List<StudentEntity>> = _students

    /**
     * SHAREDFLOW - One-time events for UI
     *
     * SharedFlow because:
     * - Events should be consumed once
     * - StateFlow would replay the last event on every recomposition
     * - SharedFlow emits events that are consumed and then forgotten
     *
     * Private _events: Mutable version for emitting events
     */
    private val _events = MutableSharedFlow<StudentListEvent>()

    /**
     * Converts MutableSharedFlow to read-only SharedFlow
     * UI collects this to show messages
     */
    val events = _events.asSharedFlow()

    /**
     * Deletes a student from the database
     *
     * @param student The StudentEntity to delete
     *
     * Process flow:
     * 1. User clicks delete button in UI
     * 2. UI calls viewModel.deleteStudent(student)
     * 3. This function launches a coroutine in viewModelScope
     * 4. Calls repository to delete student
     * 5. Emits an event to show confirmation message
     * 6. Database change triggers Flow update → UI recomposes automatically
     *
     * viewModelScope.launch:
     * - Runs asynchronously without blocking UI
     * - Automatically cancelled when ViewModel is destroyed
     * - Uses Dispatchers.Main by default
     */
    fun deleteStudent(student: StudentEntity) = viewModelScope.launch {
        repo.deleteStudent(student)
        _events.emit(StudentListEvent.ShowMessage(R.string.student_deleted))
    }

    /**
     * Inserts a new student or updates existing one
     *
     * @param student The StudentEntity to insert
     *
     * Process:
     * 1. Repository inserts student into database
     * 2. Room automatically updates the Flow
     * 3. _students StateFlow emits new list
     * 4. UI observing students recomposes with updated list
     */
    fun insertStudent(student: StudentEntity) = viewModelScope.launch {
        repo.insertStudent(student)
        _events.emit(StudentListEvent.ShowMessage(R.string.student_inserted))
    }

    /**
     * Finds a specific student by ID
     *
     * @param id The student's primary key
     * @return StudentEntity? - Found student or null
     *
     * This is a suspend function
     * The caller must launch it in their own coroutine
     */
    suspend fun findStudent(id: Int) = repo.getStudentById(id)
}