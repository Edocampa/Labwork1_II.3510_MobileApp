package com.tumme.scrudstudents.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.R
import com.tumme.scrudstudents.data.local.model.CourseWithTeacher
import com.tumme.scrudstudents.ui.viewmodel.StudentCoursesViewModel

/**
 * StudentCoursesScreen - Browse and enroll in courses
 *
 * Displays courses filtered by student's level with enrollment functionality
 *
 * Features:
 * - Level-based filtering (only shows matching courses)
 * - Teacher name and course details
 * - One-click enrollment
 * - Empty state for no available courses
 *
 * @param onBack Callback to navigate back to home
 * @param viewModel ViewModel managing course data and enrollment logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCoursesScreen(
    onBack: () -> Unit,
    viewModel: StudentCoursesViewModel = hiltViewModel()
) {
    // State observations from ViewModel
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val enrolledCourseIds by viewModel.enrolledCourseIds.collectAsState()

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Show message in snackbar when it changes
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.browse_courses)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && courses.isEmpty()) {
                // Loading state
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (courses.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_courses_available),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                // Course list
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(courses) { courseWithTeacher ->
                        CourseCard(
                            courseWithTeacher = courseWithTeacher,
                            isEnrolled = enrolledCourseIds.contains(courseWithTeacher.course.idCourse),
                            onEnroll = { viewModel.enrollInCourse(courseWithTeacher.course.idCourse) },
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
    }
}

/**
 * CourseCard - Individual course item with enrollment button
 *
 * Displays course information and allows enrollment if not already enrolled
 *
 * @param courseWithTeacher Course data with teacher information
 * @param isEnrolled Whether student is already enrolled
 * @param onEnroll Callback to enroll in course
 * @param isLoading Whether enrollment operation is in progress
 */

@Composable
private fun CourseCard(
    courseWithTeacher: CourseWithTeacher,
    isEnrolled: Boolean,
    onEnroll: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Course name
            Text(
                text = courseWithTeacher.course.nameCourse,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Teacher info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        R.string.first_name_last_name,
                        courseWithTeacher.teacher.firstName,
                        courseWithTeacher.teacher.lastName
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ECTS info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.ects, courseWithTeacher.course.ectsCourse),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        R.string.level_course,
                        courseWithTeacher.course.levelCourse
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enroll button
            if (isEnrolled) {
                AssistChip(
                    onClick = { },
                    label = { Text(stringResource(R.string.enroll_in_course)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                Button(
                    onClick = onEnroll,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.enroll_in_course))
                }
            }
        }
    }
}
