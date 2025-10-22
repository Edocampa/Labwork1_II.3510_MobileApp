package com.tumme.scrudstudents.ui.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import androidx.compose.material.icons.filled.Edit

/**
 * COMPOSABLE SCREEN - List of all subscribes
 *
 * This screen displays all student enrollments in courses with:
 * - Student full name (firstName + lastName) ← CHALLENGE SOLUTION
 * - Course name ← CHALLENGE SOLUTION
 * - Score
 * - Delete button
 *
 * Data source: subscribesWithDetails (from JOIN query)
 * This gives us student and course names instead of just IDs
 *
 * @param viewModel SubscribeViewModel injected by Hilt
 * @param onNavigateToForm Callback to navigate to enrollment form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeListScreen(
    viewModel: SubscribeViewModel = hiltViewModel(),
    onNavigateToForm: () -> Unit = {},
    onNavigateToStudents: () -> Unit = {},
    onNavigateToCourses: () -> Unit = {}
) {
    /**
     * STATE COLLECTION - Subscribes with full details (CHALLENGE)
     *
     * collectAsState() converts Flow to Compose State:
     * - When database changes → Flow emits new list
     * - collectAsState() updates this variable
     * - Compose recomposes UI with new data
     *
     * subscribesWithDetails contains:
     * - Student name (not just ID)
     * - Course name (not just ID)
     * - Score
     */
    val subscribesWithDetails by viewModel.subscribesWithDetails.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSubscribe by remember { mutableStateOf<com.tumme.scrudstudents.data.local.dao.SubscribeWithDetails?>(null) }

    /**
     * SCAFFOLD - Material Design 3 layout structure
     */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscribes") },
                actions = {
                    TextButton(onClick = onNavigateToStudents) {
                        Text("STUDENTS")
                    }
                    TextButton(onClick = onNavigateToCourses) {
                        Text("COURSES")
                    }
                }

            )
        },
        floatingActionButton = {
            /**
             * + - Create new enrollment
             * Click → navigates to form screen
             */
            FloatingActionButton(onClick = onNavigateToForm) {
                Icon(Icons.Default.Add, contentDescription = "Add Enrollment")
            }
        }
    ) { padding ->
        /**
         * LAZY COLUMN - Efficient scrollable list
         *
         * Renders only visible items + buffer
         * Automatically handles recomposition when list changes
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            /**
             * items() - Creates a composable for each subscription
             *
             * For each SubscribeWithDetails in the list:
             * - Creates a SubscribeItem card
             * - Passes delete callback to remove enrollment
             */
            items(subscribesWithDetails) { subscribeDetail ->
                SubscribeItem(
                    subscribeDetail = subscribeDetail,
                    onEditScore = {
                        selectedSubscribe = subscribeDetail
                        showEditDialog = true
                    },
                    onDelete = {
                        /**
                         * Delete callback - Converts SubscribeWithDetails back to SubscribeEntity
                         *
                         * We display SubscribeWithDetails (with names)
                         * But DAO delete method needs SubscribeEntity (with IDs)
                         * So I create a minimal SubscribeEntity with just the IDs
                         */
                        val entity = SubscribeEntity(
                            studentId = subscribeDetail.studentId,
                            courseId = subscribeDetail.courseId,
                            score = subscribeDetail.score
                        )
                        viewModel.deleteSubscribe(entity)
                    }
                )
            }
        }
        if (showEditDialog && selectedSubscribe != null) {
            EditScoreDialog(
                subscribeDetail = selectedSubscribe!!,
                onDismiss = {
                    showEditDialog = false
                    selectedSubscribe = null
                },
                onConfirm = { newScore ->
                    // Create updated SubscribeEntity with new score
                    val updatedEntity = SubscribeEntity(
                        studentId = selectedSubscribe!!.studentId,
                        courseId = selectedSubscribe!!.courseId,
                        score = newScore
                    )
                    // Update via ViewModel
                    viewModel.insertSubscribe(updatedEntity)  // REPLACE strategy updates score

                    // Close dialog
                    showEditDialog = false
                    selectedSubscribe = null
                }
            )
        }
    }
}

/**
 * COMPOSABLE - Single subscription item in the list
 *
 * Displays one subscribe with:
 * - Student name
 * - Course name
 * - Score with formatting
 * - Delete button
 *
 * @param subscribeDetail SubscribeWithDetails with full student/course info
 * @param onDelete Callback to delete this enrollment
 */
@Composable
fun SubscribeItem(
    subscribeDetail: com.tumme.scrudstudents.data.local.dao.SubscribeWithDetails,
    onDelete: () -> Unit,
    onEditScore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "${subscribeDetail.studentFirstName} ${subscribeDetail.studentLastName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subscribeDetail.courseName,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Score: %.1f / 20".format(subscribeDetail.score),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEditScore) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Score",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Enrollment",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
/**
 * DIALOG - Edit Score
 *
 * Shows a dialog to update the score of an existing subscribe
 *
 * Requirements: "Entering or updating the score"
 *
 * @param subscribeDetail The subscription to edit
 * @param onDismiss Callback when dialog is closed without saving
 * @param onConfirm Callback when user confirms the new score
 */
@Composable
fun EditScoreDialog(
    subscribeDetail: com.tumme.scrudstudents.data.local.dao.SubscribeWithDetails,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    /**
     * STATE - New score value
     * Initialize with current score
     */
    var scoreText by remember { mutableStateOf(subscribeDetail.score.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Score")
        },
        text = {
            Column {
                Text(
                    text = "${subscribeDetail.studentFirstName} ${subscribeDetail.studentLastName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subscribeDetail.courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { scoreText = it },
                    label = { Text("New Score (0-20)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newScore = scoreText.toFloatOrNull() ?: 0f
                    onConfirm(newScore)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
