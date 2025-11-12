package com.tumme.scrudstudents.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.tumme.scrudstudents.ui.viewmodel.AuthViewModel

/**
 * Student Home Screen - Main dashboard for student users
 *
 * Provides navigation to all student features:
 * - Browse and enroll in courses
 * - View subscriptions (enrolled courses)
 * - Check grades for each course
 * - Calculate weighted final grade (ECTS-based)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToFinalGrade: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Get current user info
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.student_dashboard)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Welcome Header

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(id = R.string.welcome),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentUser?.email ?: stringResource(id = R.string.student),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (currentUser?.level != null) {
                        Spacer(modifier = Modifier.height(8.dp))

                        AssistChip(
                            onClick = { },
                            label = { Text(stringResource(
                                id = R.string.user_level,
                                currentUser!!.level!!
                            )) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Menu cards

            Text(
                text = stringResource(R.string.your_actions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Browse Courses Card
            MenuCard(
                title = stringResource(id = R.string.browse_courses),
                description = stringResource(R.string.explore_and_enroll_in_available_courses),
                icon = Icons.Default.LibraryBooks,
                onClick = onNavigateToCourses
            )

            Spacer(modifier = Modifier.height(12.dp))

            // My Subscriptions Card
            MenuCard(
                title = stringResource(R.string.my_subscriptions),
                description = stringResource(R.string.view_courses_you_re_enrolled_in),
                icon = Icons.Default.CheckCircle,
                onClick = onNavigateToSubscriptions
            )

            Spacer(modifier = Modifier.height(12.dp))

            // My Grades Card
            MenuCard(
                title = stringResource(id = R.string.my_grades),
                description = stringResource(R.string.check_your_grades_for_each_course),
                icon = Icons.Default.Assessment,
                onClick = onNavigateToGrades
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Final Grade Card
            MenuCard(
                title = stringResource(id = R.string.final_grade),
                description = stringResource(R.string.view_your_weighted_average_grade),
                icon = Icons.Default.EmojiEvents,
                onClick = onNavigateToFinalGrade,
                highlighted = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.logout))
            }
        }
    }
}

/**
 * Menu Card - Reusable card component for navigation options
 */
@Composable
private fun MenuCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    highlighted: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (highlighted) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                color = if (highlighted) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (highlighted) {
                            MaterialTheme.colorScheme.onTertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
