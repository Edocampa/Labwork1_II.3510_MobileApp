package com.tumme.scrudstudents.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.ui.viewmodel.AuthViewModel

/**
 * TeacherHomeScreen - Main dashboard for teacher users
 *
 * Landing page after teacher login
 * Provides navigation to all teacher features through menu cards
 *
 * Features:
 * - Welcome header with user email
 * - Three navigation menu cards:
 *   1. My Courses (view/manage courses)
 *   2. Enter Grades (assign grades) - highlighted
 *   3. My Students (view enrollments)
 * - Logout button
 *
 * Design:
 * - Scrollable layout for smaller screens
 * - Card-based navigation
 * - Visual hierarchy (highlighted primary action)
 *
 * @param onNavigateToCourses Navigate to courses management
 * @param onNavigateToEnterGrades Navigate to grade entry
 * @param onNavigateToStudents Navigate to student overview
 * @param onLogout Logout and return to login screen
 * @param viewModel Auth ViewModel for current user info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToEnterGrades: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Get current user info
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Dashboard") },
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

            // Header

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentUser?.email ?: "Teacher",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Menu cards

            Text(
                text = "Your Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // My Courses Card
            MenuCard(
                title = "My Courses",
                description = "View and manage courses you teach",
                icon = Icons.Default.LibraryBooks,
                onClick = onNavigateToCourses
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Enter Grades Card
            MenuCard(
                title = "Enter Grades",
                description = "Assign grades to students in your courses",
                icon = Icons.Default.Edit,
                onClick = onNavigateToEnterGrades,
                highlighted = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // My Students Card
            MenuCard(
                title = "My Students",
                description = "View students enrolled in your courses",
                icon = Icons.Default.People,
                onClick = onNavigateToStudents
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
                Text("Logout")
            }
        }
    }
}

/**
 * MenuCard - Reusable navigation card component
 *
 * Displays a feature with icon, title, description and arrow
 *
 * Layout:
 * - Left: Icon in colored surface
 * - Center: Title and description
 * - Right: Arrow indicator
 *
 * @param title Feature name
 * @param description Brief feature description
 * @param icon Feature icon
 * @param onClick Navigation callback
 * @param highlighted If true, uses tertiary color for emphasis
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
                    MaterialTheme.colorScheme.secondaryContainer
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
                            MaterialTheme.colorScheme.secondary
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
