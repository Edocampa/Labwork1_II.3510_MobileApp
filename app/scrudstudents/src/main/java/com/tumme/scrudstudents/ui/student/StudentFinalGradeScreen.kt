package com.tumme.scrudstudents.ui.student

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.SubscribeWithCourseAndTeacher
import com.tumme.scrudstudents.ui.viewmodel.StudentFinalGradeViewModel
import kotlin.math.roundToInt
import com.tumme.scrudstudents.ui.viewmodel.AuthViewModel

/**
 * StudentFinalGradeScreen - Display ECTS-weighted final grade
 *
 * Shows student's final weighted average calculated using ECTS credits
 * Formula: Final Grade = Σ(grade × ECTCS) / Σ(ECTS)
 *
 * Features:
 * - Animated final grade display with color coding
 * - Performance badge (Excellent, Good, etc.)
 * - Per-course breakdown showing grade and ECTS
 * - Only includes graded courses (score > 0)
 * - Empty state for students with no grades
 *
 * @param onBack Callback to navigate back
 * @param viewModel ViewModel managing grade calculation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFinalGradeScreen(
    onBack: () -> Unit,
    viewModel: StudentFinalGradeViewModel = hiltViewModel()
) {
    // State observations from ViewModel
    val gradedCourses by viewModel.gradedCourses.collectAsState()
    val finalGrade by viewModel.finalGrade.collectAsState()
    val totalECTS by viewModel.totalECTS.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

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
                title = { Text("Final Grade") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Only show if there are graded courses
            if (gradedCourses.isNotEmpty()) {
                ExportGradesButton(
                    studentName = "${currentUser?.email?.substringBefore("@") ?: "Student"}",
                    studentLevel = currentUser?.level ?: "Unknown",
                    courses = gradedCourses,
                    finalGrade = finalGrade,
                    totalECTS = totalECTS
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (gradedCourses.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No grades yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Complete courses and get graded to see your final grade",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Final grade display
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Final Grade Card (Big)
                item {
                    FinalGradeCard(
                        finalGrade = finalGrade,
                        totalECTS = totalECTS
                    )
                }

                // Breakdown Header
                item {
                    Text(
                        text = "Grade Breakdown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Course breakdown
                items(gradedCourses) { subscription ->
                    CourseBreakdownCard(subscription)
                }
                }
            }
        }
    }

/**
 * FinalGradeCard - Large animated card showing final weighted grade
 *
 * Features:
 * - Pulse animation on grade number
 * - Color-coded background by performance level
 * - Trophy icon
 * - Performance badge (Excellent, Good, etc.)
 * - Total ECTS count
 *
 * @param finalGrade Calculated weighted average (0-20)
 * @param totalECTS Total credits from all graded courses
 */

@Composable
private fun FinalGradeCard(
    finalGrade: Float,
    totalECTS: Int
) {
    // Pulse animation for grade
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {

                // Color-coded by grade level (0-20)

                finalGrade >= 18 -> MaterialTheme.colorScheme.primaryContainer
                finalGrade >= 16 -> MaterialTheme.colorScheme.secondaryContainer
                finalGrade >= 10 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = when {
                    finalGrade >= 18 -> MaterialTheme.colorScheme.primary
                    finalGrade >= 16 -> MaterialTheme.colorScheme.secondary
                    finalGrade >= 10 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Final Grade",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated grade display
            Text(
                text = String.format("%.1f", finalGrade),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale),
                color = when {
                    finalGrade >= 18 -> MaterialTheme.colorScheme.primary
                    finalGrade >= 16 -> MaterialTheme.colorScheme.secondary
                    finalGrade >= 10 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Text(
                text = "/20",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Total ECTS
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total: $totalECTS ECTS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Performance badge
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = when {
                            finalGrade >= 18 -> "Excellent! "
                            finalGrade >= 16 -> "Very Good! "
                            finalGrade >= 14 -> "Good! "
                            finalGrade >= 12 -> "Fair "
                            finalGrade >= 10 -> "Passing "
                            else -> "Keep Going! "
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}

/**
 * CourseBreakdownCard - Individual course grade display
 *
 * Shows course name, teacher, grade and ECTS for one course
 *
 * @param subscription Course enrollment with grade information
 */

@Composable
private fun CourseBreakdownCard(
    subscription: SubscribeWithCourseAndTeacher
) {
    val course = subscription.courseWithTeacher.course
    val teacher = subscription.courseWithTeacher.teacher
    val score = subscription.subscribe.score
    val ects = course.ectsCourse.toInt()
    val weightedScore = score * course.ectsCourse

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.nameCourse,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${teacher.firstName} ${teacher.lastName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${score.toInt()}/20",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // Showing number of ECTS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$ects ECTS",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

