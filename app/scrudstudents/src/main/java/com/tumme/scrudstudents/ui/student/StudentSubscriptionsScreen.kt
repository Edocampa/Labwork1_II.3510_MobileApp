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
import com.tumme.scrudstudents.data.local.model.SubscribeWithCourseAndTeacher
import com.tumme.scrudstudents.ui.viewmodel.StudentSubscriptionsViewModel
import com.tumme.scrudstudents.R
import androidx.compose.ui.platform.LocalContext
import com.tumme.scrudstudents.ui.viewmodel.StudentSubscriptionsMessage

/**
 * StudentSubscriptionsScreen - View enrolled courses with grades
 *
 * Displays all courses the student is enrolled in
 * Shows current grade if assigned or "Not graded yet"
 *
 * Features:
 * - Complete enrollment list (graded and ungraded)
 * - Course details (name, teacher, ECTS, level)
 * - Grade status (assigned or pending)
 * - Color-coded grade badges
 * - Empty state for no enrollments
 *
 *
 * @param onBack Callback to navigate back
 * @param viewModel ViewModel managing subscription data
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSubscriptionsScreen(
    onBack: () -> Unit,
    viewModel: StudentSubscriptionsViewModel = hiltViewModel()
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let { msg ->

            val translatedMessage = when (msg) {
                is StudentSubscriptionsMessage.Dynamic -> {
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
                title = { Text(stringResource(id = R.string.my_subscriptions)) },
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
            if (isLoading && subscriptions.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (subscriptions.isEmpty()) {
                // Empty state
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
                        text = stringResource(R.string.no_enrolled_courses),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.browse_courses_to_enroll),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Subscriptions list
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subscriptions) { subscription ->
                        SubscriptionCard(subscription)
                    }
                }
            }
        }
    }
}

/**
 * SubscriptionCard - Individual enrollment display with grade status
 *
 * Shows complete course information and grade status
 * Displays grade if assigned, or "Not graded yet" chip if pending
 *
 * Card sections:
 * 1. Course name (title)
 * 2. Teacher name (with person icon)
 * 3. ECTS
 * 4. Level
 * 5. Divider
 * 6. Grade status
 *
 * @param subscription Course enrollment with grade information
 */

@Composable
private fun SubscriptionCard(
    subscription: SubscribeWithCourseAndTeacher
) {
    val course = subscription.courseWithTeacher.course
    val teacher = subscription.courseWithTeacher.teacher
    val score = subscription.subscribe.score

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Course name
            Text(
                text = course.nameCourse,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Teacher
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        teacher.firstName,
                        teacher.lastName
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ECTS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.ects, course.ectsCourse),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Level
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.level_course, course.levelCourse),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            // Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.grade),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (score > 0) {
                    // Grade assigned
                    Surface(
                        color = when {
                            score >= 18 -> MaterialTheme.colorScheme.primaryContainer
                            score >= 16 -> MaterialTheme.colorScheme.secondaryContainer
                            score >= 10 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(R.string.score_out_of_20, score.toInt()),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    AssistChip(
                        onClick = { },
                        label = { Text(stringResource(id = R.string.no_grades_yet)) },
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
}

