package com.tumme.scrudstudents.ui.admin

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
import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.local.model.UserRole
import com.tumme.scrudstudents.ui.viewmodel.AdminViewModel


/**
 * AdminHomeScreen - Admin dashboard
 *
 * Displays system statistics and user management interface
 * Admin can view all users and delete Student/Teacher accounts
 *
 * Features:
 * - Global statistics (total students, teachers, courses, enrollments)
 * - User list with role indicators
 * - Delete functionality with confirmation dialog
 * - Admin accounts cannot be deleted
 *
 * @param onLogout Callback to logout and return to login screen
 * @param viewModel AdminViewModel for data management
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val users by viewModel.allUsers.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val message by viewModel.message.collectAsState()
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
                title = { Text(stringResource(R.string.admin_dashboard)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.administrator),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.system_management),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Statistics section
            item {
                Text(
                    text = stringResource(R.string.statistics),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.students),
                        value = statistics.totalStudents.toString(),
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.teachers),
                        value = statistics.totalTeachers.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = stringResource(R.string.courses),
                        value = statistics.totalCourses.toString(),
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.subscriptions),
                        value = statistics.totalEnrollments.toString(),
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // User Management
            item {
                Text(
                    text = stringResource(R.string.user_management),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // User list
            items(users) { user ->
                UserCard(
                    user = user,
                    onDelete = { viewModel.deleteUser(user.idUser) }
                )
            }
        }
    }
}

/**
 * StatCard - Display single statistic
 *
 * Shows icon, value and label for system statistics
 *
 * @param title Statistic label
 * @param value Numeric value to display
 * @param icon Icon representing the statistic
 * @param modifier Compose modifier
 */

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * UserCard - Display user with delete option
 *
 * Shows user email, role, level and delete button
 * Admin accounts cannot be deleted
 * Delete requires confirmation via dialog
 *
 * @param user User entity to display
 * @param onDelete Callback when delete is confirmed
 */

@Composable
private fun UserCard(
    user: User,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (user.role) {
                    UserRole.STUDENT -> Icons.Default.School
                    UserRole.TEACHER -> Icons.Default.Person
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = when (user.role) {
                    UserRole.STUDENT -> MaterialTheme.colorScheme.primary
                    UserRole.TEACHER -> MaterialTheme.colorScheme.secondary
                    UserRole.ADMIN -> MaterialTheme.colorScheme.tertiary
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.role.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (user.level != null) {
                        Text(
                            text = stringResource(R.string.user_level, user.level),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Delete button (can't delete admin)
            if (user.role != UserRole.ADMIN) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.delete_user)) },
            text = {
                Text(
                    stringResource(
                        R.string.are_you_sure_you_want_to_delete_this_action_cannot_be_undone_and_will_delete_all_associated_data,
                        user.email
                    ))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
