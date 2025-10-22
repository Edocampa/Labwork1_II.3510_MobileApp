package com.tumme.scrudstudents.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.CourseEntity

/**
 * COURSE LIST SCREEN - Displays all courses in a scrollable list
 *
 * Main screen for viewing and managing courses
 *
 * Features:
 * - Real-time updates via StateFlow (automatic UI refresh on database changes)
 * - Navigation to other screens via TopAppBar buttons
 * - Floating Action Button for creating new courses
 * - Edit and delete actions for each course item
 *
 *
 * @param viewModel CourseViewModel injected by Hilt
 * @param onNavigateToForm Navigate to create new course
 * @param onNavigateToEdit Navigate to edit existing course (with ID)
 * @param onNavigateToStudents Navigate to Students screen
 * @param onNavigateToSubscribes Navigate to Enrollments screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    viewModel: CourseViewModel = hiltViewModel(),
    onNavigateToForm: () -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {},
    onNavigateToStudents: () -> Unit = {},
    onNavigateToSubscribes: () -> Unit = {}
) {
    /**
     * STATE COLLECTION - Observes courses from ViewModel
     *
     * collectAsState():
     * 1. Subscribes to viewModel.courses StateFlow
     * 2. Converts Flow to Compose State
     * 3. Triggers recomposition when StateFlow emits new value
     * 4. Auto-unsubscribes when screen leaves composition
     *
     * Data flow:
     * Database change → DAO Flow → Repository → ViewModel StateFlow
     * → collectAsState() → UI recomposes → List updates
     *
     */
    val courses by viewModel.courses.collectAsState()

    /**
     * SCAFFOLD - Material Design layout structure
     *
     * Provides standard app structure:
     * - topBar: App bar with title and navigation actions
     * - floatingActionButton: + for primary action (add course)
     * - content: Main scrollable list
     *
     */
    Scaffold(
        topBar = {
            /**
             * TOP APP BAR - Title and navigation actions
             *
             * Title: "Courses" - indicates current screen
             *
             * Actions: Navigation buttons to other screens
             * - STUDENTS: Navigate to student list
             * - SUBSCRIBES: Navigate to enrollments list
             *
             */
            TopAppBar(
                title = { Text("Courses") },
                actions = {
                    TextButton(onClick = onNavigateToStudents) {
                        Text("STUDENTS")
                    }

                    TextButton(onClick = onNavigateToSubscribes) {
                        Text("SUBSCRIBES")
                    }
                }
            )
        },
        floatingActionButton = {
            /**
             * FLOATING ACTION BUTTON - Primary screen action
             *
             * Purpose: Create new course
             * Icon: + (Plus icon)
             *
             * onClick: Navigates to CourseFormScreen in create mode (courseId = 0)
             */
            FloatingActionButton(onClick = onNavigateToForm) {
                Icon(Icons.Default.Add, contentDescription = "Add course")
            }
        }
    ) { padding ->
        /**
         * LAZY COLUMN - Efficient scrollable list
         *
         * LazyColumn characteristics:
         * - Renders only visible items plus buffer (like RecyclerView)
         * - Recycles composables as user scrolls
         * - Handles large datasets efficiently
         * - Supports item animations automatically
         *
         * Padding:
         * - padding (from Scaffold): Avoids overlap with TopAppBar and FAB
         * - padding(16.dp): Additional internal spacing
         *
         * items() function:
         * - Iterates over courses list
         * - Creates CourseItem for each course
         * - Automatically handles list changes (add/remove/reorder)
         * - Uses course objects as keys for efficient recomposition
         *
         * Recomposition behavior:
         * When courses StateFlow emits new list:
         * 1. collectAsState() updates 'courses' variable
         * 2. LazyColumn detects list change
         * 3. Compose calculates diff (what changed)
         * 4. Only affected items recompose
         * 5. Smooth animations for additions/removals
         */
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(courses) { course ->
                CourseItem(
                    course = course,
                    onEdit = { onNavigateToEdit(course.idCourse) },
                    onDelete = { viewModel.deleteCourse(course) }
                )
            }
        }
    }
}

/**
 * COURSE ITEM - Individual course card in the list
 *
 * Displays course information with action buttons
 *
 * Layout:
 * - Card with elevation for visual hierarchy
 * - Row layout: course info on left, action buttons on right
 * - Course name and details in vertical column
 * - Edit and delete icons for actions
 *
 * @param course CourseEntity with course data
 * @param onDelete Callback to delete this course
 * @param onEdit Callback to edit this course
 */
@Composable
fun CourseItem(
    course: CourseEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
) {
    /**
     * CARD - Material Design surface component
     *
     * Card provides:
     * - Elevated surface (4dp elevation)
     * - Rounded corners (default)
     * - Shadow for depth perception
     * - Container for related content
     *
     * Vertical padding (8.dp):
     * - Creates spacing between cards
     * - Improves visual separation
     * - Prevents cards from touching
     */
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        /**
         * ROW LAYOUT - Horizontal arrangement
         *
         * horizontalArrangement = SpaceBetween:
         * - Places course info on far left
         * - Places action buttons on far right
         * - Maximizes space between them
         *
         * verticalAlignment = CenterVertically:
         * - Centers all row content vertically
         * - Ensures icons align with text baseline
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            /**
             * COURSE INFORMATION - Left side of card
             *
             * weight(1f):
             * - Takes all remaining space after action buttons
             * - Pushes action buttons to the right
             * - Allows long course names to wrap if needed
             */
            Column(modifier = Modifier.weight(1f)) {
                /**
                 * COURSE NAME - Primary text
                 */
                Text(
                    text = course.nameCourse,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                /**
                 * COURSE DETAILS - Secondary text
                 *
                 */
                Text(
                    text = "${course.ectsCourse} ECTS - ${course.levelCourse}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            /**
             * ACTION BUTTONS - Right side of card
             *
             */
            Row {
                /**
                 * EDIT BUTTON - Navigate to edit form
                 *
                 * Icon: Pencil (standard edit symbol)
                 *
                 */
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update course",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                /**
                 * DELETE BUTTON - Remove course from database
                 *
                 * Icon: Trash bin (standard delete symbol)
                 */
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete course",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}