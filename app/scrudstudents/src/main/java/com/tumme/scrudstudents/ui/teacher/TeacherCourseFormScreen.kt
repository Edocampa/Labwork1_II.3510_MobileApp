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
 * Teacher Course Form Screen - Add/Edit course
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
    var cfu by remember { mutableStateOf("") }
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
            cfu = course.ectsCourse.toInt().toString()
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

            // CFU
            OutlinedTextField(
                value = cfu,
                onValueChange = { if (it.all { char -> char.isDigit() }) cfu = it },
                label = { Text("CFU (Credits)") },
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
                        cfu = cfu.toIntOrNull() ?: 0,
                        level = level
                    )
                },
                enabled = !isLoading &&
                        courseName.isNotBlank() &&
                        cfu.isNotBlank() &&
                        (cfu.toIntOrNull() ?: 0) > 0,
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

    fun loadCourse(courseId: Int) = viewModelScope.launch {
        try {
            _course.value = repository.getCourseById(courseId)
        } catch (e: Exception) {
            _message.value = "Error loading course: ${e.message}"
        }
    }

    fun saveCourse(courseId: Int, name: String, cfu: Int, level: String) = viewModelScope.launch {
        if (name.isBlank()) {
            _message.value = "Course name is required"
            return@launch
        }
        if (cfu <= 0) {
            _message.value = "CFU must be greater than 0"
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
                        ectsCourse = cfu.toFloat(),
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
