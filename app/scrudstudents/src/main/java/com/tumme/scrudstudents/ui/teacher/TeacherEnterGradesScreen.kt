package com.tumme.scrudstudents.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.R
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentWithGrade
import com.tumme.scrudstudents.ui.viewmodel.TeacherEnterGradesViewModel
import androidx.compose.ui.platform.LocalContext
import com.tumme.scrudstudents.ui.viewmodel.TeacherEnterGradesMessage

/**
 * TeacherEnterGradesScreen - Assign grades to students
 *
 * Two-step flow for entering student grades:
 * 1. Select course from list of taught courses
 * 2. View students enrolled in that course and enter/update grades
 *
 * Features:
 * - Course selection screen
 * - Student list with grade input fields
 * - Real-time validation (0-20)
 * - Individual save button per student
 * - Current grade display with color coding
 * - Two-level back navigation (students → courses → home)
 *
 * @param onBack Callback to navigate back (or to previous step)
 * @param viewModel ViewModel managing course selection and grade updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEnterGradesScreen(
    onBack: () -> Unit,
    viewModel: TeacherEnterGradesViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let { msg ->

            val translatedMessage = when (msg) {
                is TeacherEnterGradesMessage.Simple -> {
                    context.getString(msg.messageId)
                }
                is TeacherEnterGradesMessage.Dynamic -> {
                    context.getString(msg.baseMessageId, msg.dynamicPart)
                }
            }

            snackbarHostState.showSnackbar(translatedMessage)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(selectedCourse?.nameCourse ?: stringResource(R.string.enter_grades))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCourse != null) {
                            viewModel.clearSelection()
                        } else {
                            onBack()
                        }
                    }
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (selectedCourse == null) {
            // Course selection
            CourseSelectionView(
                courses = courses,
                isLoading = isLoading,
                onSelectCourse = { viewModel.selectCourse(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            // Student grades list
            StudentGradesListView(
                students = students,
                isLoading = isLoading,
                onUpdateGrade = { subscribeId, score ->
                    viewModel.updateGrade(subscribeId, score)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

/**
 * CourseSelectionView - Select course
 *
 * Displays list of courses taught by teacher
 * Clicking a course navigates to student list
 *
 * States: Loading, empty, course list
 */

@Composable
private fun CourseSelectionView(
    courses: List<CourseEntity>,
    isLoading: Boolean,
    onSelectCourse: (CourseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (isLoading && courses.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (courses.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryBooks,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_courses_found),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.declare_courses_first_to_enter_grades),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.select_a_course_to_enter_grades),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(courses) { course ->
                    CourseSelectionCard(
                        course = course,
                        onClick = { onSelectCourse(course) }
                    )
                }
            }
        }
    }
}

/**
 * CourseSelectionCard - Clickable course card
 *
 * Shows course name, ECTS, level with arrow indicator
 * Clicking navigates to student list for that course
 */

@Composable
private fun CourseSelectionCard(
    course: CourseEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.nameCourse,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.ects_level,
                        course.ectsCourse.toInt(),
                        course.levelCourse
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * StudentGradesListView - Enter grades
 *
 * Displays students enrolled in selected course
 * Each student has grade input field and save button
 *
 * States: Loading, empty (no students), student list
 */

@Composable
private fun StudentGradesListView(
    students: List<StudentWithGrade>,
    isLoading: Boolean,
    onUpdateGrade: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (isLoading && students.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (students.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.no_students_enrolled),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.students_need_to_enroll_in_this_course),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    StudentGradeCard(
                        student = student,
                        onUpdateGrade = onUpdateGrade
                    )
                }
            }
        }
    }
}

/**
 * StudentGradeCard - Individual student grade entry
 *
 * Card showing student info and grade input
 *
 * Features:
 * - Student name, email, level
 * - Grade input field (0-20 validation)
 * - Save button (enabled when grade changes)
 * - Current grade display (color-coded badge)
 *
 * State management:
 * - gradeText: Local state for input field
 * - isEditing: Tracks if grade was modified
 * - Remember by currentScore: Resets when grade updates
 *
 * @param student Student info with current grade
 * @param onUpdateGrade Callback to save grade (subscribeId, score)
 */

@Composable
private fun StudentGradeCard(
    student: StudentWithGrade,
    onUpdateGrade: (Int, Float) -> Unit
) {
    var gradeText by remember(student.currentScore) {
        mutableStateOf(if (student.currentScore > 0) student.currentScore.toInt().toString() else "")
    }
    var isEditing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Student info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            id = R.string.full_name_student,
                            student.studentFirstName,
                            student.studentLastName

                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = student.studentEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.level_course, student.studentLevel),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Grade input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.toIntOrNull() in 0..20)) {
                            gradeText = it
                            isEditing = true
                        }
                    },
                    label = { Text(stringResource(id = R.string.grade)) },
                    placeholder = { Text(stringResource(id = R.string.score_0_20)) },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val score = gradeText.toFloatOrNull() ?: 0f
                        onUpdateGrade(student.subscribeId, score)
                        isEditing = false
                    },
                    enabled = isEditing && gradeText.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(id = R.string.save))
                }
            }

            // Current grade display
            if (student.currentScore > 0 && !isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = when {
                        student.currentScore >= 18 -> MaterialTheme.colorScheme.primaryContainer
                        student.currentScore >= 16 -> MaterialTheme.colorScheme.secondaryContainer
                        student.currentScore >= 10 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                R.string.current_grade_20,
                                student.currentScore.toInt()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
