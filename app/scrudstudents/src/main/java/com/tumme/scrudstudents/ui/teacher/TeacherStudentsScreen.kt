package com.tumme.scrudstudents.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentWithGrade
import com.tumme.scrudstudents.ui.viewmodel.TeacherStudentsViewModel

/**
 * TeacherStudentsScreen - View enrolled students per course
 *
 * Two-step flow for viewing student information:
 * 1. Select course from list of taught courses
 * 2. View students with statistics and grade information
 *
 * Features:
 * - Course selection screen
 * - Statistics cards (total, graded, average)
 * - Student list with grades
 * - Color-coded grade display
 * - Two-level back navigation (students → courses → home)
 *
 * Difference from EnterGrades:
 * - Students: Read-only view with stats (focus on overview)
 * - EnterGrades: Grade input with save buttons (focus on action)
 *
 * @param onBack Callback to navigate back (or to previous step)
 * @param viewModel ViewModel managing course selection and statistics
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentsScreen(
    onBack: () -> Unit,
    viewModel: TeacherStudentsViewModel = hiltViewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val selectedCourse by viewModel.selectedCourse.collectAsState()
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val totalStudents by viewModel.totalStudents.collectAsState()
    val gradedStudents by viewModel.gradedStudents.collectAsState()
    val averageGrade by viewModel.averageGrade.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(selectedCourse?.nameCourse ?: "My Students")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCourse != null) {
                            viewModel.clearSelection()
                        } else {
                            onBack()
                        }
                    }) {
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
            // Students list with stats
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Stats cards
                if (students.isNotEmpty()) {
                    StatsSection(
                        totalStudents = totalStudents,
                        gradedStudents = gradedStudents,
                        averageGrade = averageGrade,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Students list
                StudentsList(
                    students = students,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * CourseSelectionView - Select course
 *
 * Identical to EnterGradesScreen course selection
 * Shows list of courses taught by teacher
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
                    text = "No courses found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Declare courses to see enrolled students",
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
                        text = "Select a course to view students",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(courses) { course ->
                    CourseCard(
                        course = course,
                        onClick = { onSelectCourse(course) }
                    )
                }
            }
        }
    }
}

/**
 * CourseCard - Clickable course card for selection
 *
 * Shows course name, ECTS, level with arrow indicator
 */

@Composable
private fun CourseCard(
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
                    text = "${course.ectsCourse.toInt()} ECTS • Level ${course.levelCourse}",
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
 * StatsSection - Statistics cards for selected course
 *
 * Displays three metrics in a row:
 * - Total enrolled students
 * - Number of graded students
 * - Average grade (color-coded by performance)
 *
 * @param totalStudents Total number of enrolled students
 * @param gradedStudents Number with grades assigned (score > 0)
 * @param averageGrade Average grade of graded students (0-20)
 */

@Composable
private fun StatsSection(
    totalStudents: Int,
    gradedStudents: Int,
    averageGrade: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Course Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total students
            StatCard(
                icon = Icons.Default.People,
                label = "Students",
                value = totalStudents.toString(),
                modifier = Modifier.weight(1f)
            )

            // Graded students
            StatCard(
                icon = Icons.Default.Assessment,
                label = "Graded",
                value = gradedStudents.toString(),
                modifier = Modifier.weight(1f)
            )

            // Average grade
            StatCard(
                icon = Icons.Default.Star,
                label = "Average",
                value = if (averageGrade > 0) String.format("%.1f", averageGrade) else "-",
                modifier = Modifier.weight(1f),
                color = when {
                    averageGrade >= 18 -> MaterialTheme.colorScheme.primary
                    averageGrade >= 16 -> MaterialTheme.colorScheme.secondary
                    averageGrade >= 10 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * StatCard - Individual statistic card
 *
 * Displays an icon, value and label in a compact card
 * Value color can be customized
 *
 * @param icon Icon to display
 * @param label Label text (e.g., "Students")
 * @param value Value to display (e.g., "24")
 * @param color Color for icon and value (default: primary)
 */

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * StudentsList - List of enrolled students
 *
 * Displays student cards with basic info and grades
 * Read-only view (no grade input)
 *
 * States: Loading, empty, student list
 */

@Composable
private fun StudentsList(
    students: List<StudentWithGrade>,
    isLoading: Boolean,
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
                    text = "No students enrolled",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Students need to enroll in this course",
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
                    StudentInfoCard(student)
                }
            }
        }
    }
}

/**
 * StudentInfoCard - Read-only student information card
 *
 * Displays student details and grade status without input fields
 * Shows grade badge if assigned, or "Not graded" chip if pending
 *
 * Layout:
 * - Left: Student info (name, email, level)
 * - Right: Grade badge or pending chip
 *
 * @param student Student information with current grade
 */

@Composable
private fun StudentInfoCard(
    student: StudentWithGrade
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.studentFirstName} ${student.studentLastName}",
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
                        text = "Level: ${student.studentLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Grade display
            if (student.currentScore > 0) {
                Surface(
                    color = when {
                        student.currentScore >= 18 -> MaterialTheme.colorScheme.primaryContainer
                        student.currentScore >= 16 -> MaterialTheme.colorScheme.secondaryContainer
                        student.currentScore >= 10 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "${student.currentScore.toInt()}/20",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else {
                AssistChip(
                    onClick = { },
                    label = { Text("Not graded") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}
