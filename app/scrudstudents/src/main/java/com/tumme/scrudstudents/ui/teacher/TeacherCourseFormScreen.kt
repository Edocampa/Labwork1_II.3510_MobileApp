package com.tumme.scrudstudents.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
 * TeacherCourseFormScreen - Create or edit a course
 *
 * Form for declaring new courses or editing existing ones
 * Used in two modes: creation (courseId=0) or edit (courseId>0)
 *
 * Features:
 * - Course name input
 * - ECTS (credits) input with numeric validation
 * - Level dropdown (P1-PhD)
 * - Validation (name required, ECTS > 0)
 * - Save button with loading state
 *
 * @param courseId Course ID (0 for new course, >0 for editing)
 * @param onBack Callback to navigate back
 * @param onSaved Callback after successful save (triggers navigation back)
 * @param viewModel ViewModel managing form state and save operation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCourseFormScreen(
    courseId: Int = 0,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: TeacherCourseFormViewModel = hiltViewModel()
) {
    var courseName by remember { mutableStateOf("") }
    var ects by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("B1") }
    var showLevelMenu by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val levels = listOf("P1", "P2", "P3", "B1", "B2", "B3", "A1", "A2", "A3", "MS", "PhD")

    // Load course if editing
    LaunchedEffect(courseId) {
        if (courseId > 0) {
            viewModel.loadCourse(courseId)
        }
    }

    LaunchedEffect(viewModel.course.collectAsState().value) {
        viewModel.course.value?.let { course ->
            courseName = course.nameCourse
            ects = course.ectsCourse.toInt().toString()
            level = course.levelCourse
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
            if (it.contains("success", ignoreCase = true)) {
                onSaved()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == 0) "New Course" else "Edit Course") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Course name
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                placeholder = { Text("e.g., Advanced English Literature") },
                leadingIcon = {
                    Icon(Icons.Default.LibraryBooks, null)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            // ECTS
            OutlinedTextField(
                value = ects,
                onValueChange = { if (it.all { char -> char.isDigit() }) ects = it },
                label = { Text("ECTS (Credits)") },
                placeholder = { Text("6, 9, or 12") },
                leadingIcon = {
                    Icon(Icons.Default.Star, null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            // Level dropdown
            ExposedDropdownMenuBox(
                expanded = showLevelMenu,
                onExpandedChange = { showLevelMenu = !showLevelMenu && !isLoading }
            ) {
                OutlinedTextField(
                    value = level,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Level") },
                    leadingIcon = {
                        Icon(Icons.Default.School, null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLevelMenu)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showLevelMenu,
                    onDismissRequest = { showLevelMenu = false }
                ) {
                    levels.forEach { lvl ->
                        DropdownMenuItem(
                            text = { Text(lvl) },
                            onClick = {
                                level = lvl
                                showLevelMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    viewModel.saveCourse(
                        courseId = courseId,
                        name = courseName,
                        ects = ects.toIntOrNull() ?: 0,
                        level = level
                    )
                },
                enabled = !isLoading &&
                        courseName.isNotBlank() &&
                        ects.isNotBlank() &&
                        (ects.toIntOrNull() ?: 0) > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (courseId == 0) "Create Course" else "Update Course")
                }
            }
        }
    }
}

/**
 * TeacherCourseFormViewModel - Manages course form state
 *
 * Handles loading existing course data for editing and saving (create/update)
 *
 * Responsibilities:
 * - Load course by ID when editing
 * - Validate input (name required, ECTS > 0)
 * - Create new course or update existing
 * - Link course to current logged-in teacher
 *
 * @param repository Database operations
 * @param authRepository Current user/teacher information
 */

@HiltViewModel
class TeacherCourseFormViewModel @Inject constructor(
    private val repository: SCRUDRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _course = MutableStateFlow<CourseEntity?>(null)
    val course: StateFlow<CourseEntity?> = _course.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /**
     * Load existing course data for editing
     *
     * @param courseId ID of course to load
     */

    fun loadCourse(courseId: Int) = viewModelScope.launch {
        try {
            _course.value = repository.getCourseById(courseId)
        } catch (e: Exception) {
            _message.value = "Error loading course: ${e.message}"
        }
    }

    /**
     * Save course (create new or update existing)
     *
     * Validation:
     * - Name must not be blank
     * - ECTS must be > 0
     *
     * Links course to current teacher's ID
     *
     * @param courseId 0 for new course, >0 for updating
     * @param name Course name
     * @param ects Credit hours (ECTS)
     * @param level Academic level (P1-PhD)
     */

    fun saveCourse(courseId: Int, name: String, ects: Int, level: String) = viewModelScope.launch {
        if (name.isBlank()) {
            _message.value = "Course name is required"
            return@launch
        }
        if (ects <= 0) {
            _message.value = "ECTS must be greater than 0"
            return@launch
        }

        _isLoading.value = true
        try {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                val teacher = repository.getTeacherByUserId(currentUser.idUser)
                if (teacher != null) {
                    val course = CourseEntity(
                        idCourse = courseId,
                        nameCourse = name,
                        ectsCourse = ects.toFloat(),
                        teacherId = teacher.idTeacher,
                        levelCourse = level
                    )

                    if (courseId == 0) {
                        repository.insertCourse(course)
                        _message.value = "Course created successfully"
                    } else {
                        repository.updateCourse(course)
                        _message.value = "Course updated successfully"
                    }
                } else {
                    _message.value = "Teacher profile not found"
                }
            }
        } catch (e: Exception) {
            _message.value = "Error saving course: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
