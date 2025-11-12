package com.tumme.scrudstudents.ui.student

import com.tumme.scrudstudents.ui.components.TableHeader
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.R

/**
 * COMPOSABLE SCREEN - Displays the list of all students
 *
 * This is the main screen for viewing students in a table format
 *
 * Compose Architecture:
 * - @Composable functions describe UI declaratively
 * - UI automatically recomposes when state changes
 * - State is observed with collectAsState()
 *
 * @param viewModel StudentListViewModel injected by Hilt (default parameter)
 * @param onNavigateToForm Lambda callback to navigate to student creation form
 * @param onNavigateToDetail Lambda callback to navigate to student detail view
 *
 * Navigation pattern:
 * - Parent composable provides navigation callbacks
 * - This screen calls them when user interaction requires navigation
 * - Separates navigation logic from UI logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel = hiltViewModel(),
    onNavigateToForm: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToSubscribes: () -> Unit = {}
) {
    /**
     * STATE COLLECTION - Converts Flow to Compose State
     *
     * collectAsState() does the following:
     * 1. Subscribes to viewModel.students StateFlow
     * 2. Converts Flow emissions to Compose State
     * 3. When Flow emits new value → students variable updates → UI recomposes
     * 4. Automatically unsubscribes when Composable leaves composition
     *
     * The 'by' keyword:
     * - Delegates property access to State.value
     * - Instead of: students.value --> we can use students directly
     *
     * Recomposition trigger:
     * - Database changes → DAO emits new list → Repository forwards it
     * - ViewModel's StateFlow updates → collectAsState() detects change
     * - Compose schedules recomposition of functions using 'students'
     */
    val students by viewModel.students.collectAsState()

    /**
     * Coroutine scope tied to this Composable's lifecycle
     * Used for launching coroutines that need to survive recompositions
     * but should be cancelled when the Composable is removed
     */
    val coroutineScope = rememberCoroutineScope()

    /**
     * SCAFFOLD - Material Design layout structure
     *
     * Provides standard Material Design components:
     * - topBar: App bar at the top
     * - floatingActionButton: FAB for primary action
     * - content: Main content area
     *
     * Scaffold automatically handles:
     * - Positioning of components
     * - Padding to avoid overlap
     * - Material theming
     */
    Scaffold(
        topBar = {
            /**
             * TOP APP BAR - Title bar at the top of the screen
             * Shows the screen title "Students"
             */
            TopAppBar(
                title = { Text(stringResource(id = R.string.students)) },
                actions = {

                    TextButton(onClick = onNavigateToCourses) {
                        Text(stringResource(id = R.string.courses))
                    }

                    TextButton(onClick = onNavigateToSubscribes) {
                        Text(stringResource(id = R.string.teachers))
                    }
                }
            )
        },
        floatingActionButton = {
            /**
             * FLOATING ACTION BUTTON - Primary action for this screen
             *
             * Purpose: Create a new student
             * When clicked: navigates to the form screen
             *
             */
            FloatingActionButton(onClick = onNavigateToForm) {
                Text(stringResource(R.string.add))
            }
        }
    ) { padding ->
        /**
         * MAIN CONTENT - Column layout for the student list
         *
         * padding parameter:
         * - Provided by Scaffold to avoid overlapping with TopAppBar/FAB
         * - Must be applied to top-level content
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            /**
             * TABLE HEADER - Column titles for the student table
             *
             * Custom component that displays header row
             *
             * Parameters:
             * - cells: List of column titles
             * - weights: Proportional widths
             *
             * Layout:
             * - DOB: 25% width
             * - Last Name: 25% width
             * - First Name: 25% width
             * - Gender: 15% width
             * - Actions: 10% width
             */
            TableHeader(
                cells = listOf("DOB", "Last", "First", "Gender", "Actions"),
                weights = listOf(0.25f, 0.25f, 0.25f, 0.15f, 0.10f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            /**
             * LAZYCOLUMN - Efficient scrollable list
             *
             *
             * items() function:
             * - Iterates over the students list
             * - Creates a StudentRow composable for each student
             * - Automatically handles additions/removals/reorderings
             *
             * When students list changes:
             * 1. collectAsState() updates 'students' variable
             * 2. LazyColumn detects list change
             * 3. Compose calculates diff (what changed)
             * 4. Only affected items recompose
             * 5. Animations applied automatically (with animateItemPlacement)
             */
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(students) { student ->
                    /**
                     * STUDENT ROW - Composable for each student item
                     *
                     * Displays one student's information in a row format
                     * matching the header columns
                     *
                     * Callbacks:
                     * - onEdit: Edit this student
                     * - onDelete: Delete this student from DB
                     * - onView: Navigate to detail view
                     * - onShare: Share student info
                     */
                    StudentRow(
                        student = student,
                        onEdit = {
                            /* TODO: Navigate to form with pre-filled data */
                        },
                        onDelete = {
                            viewModel.deleteStudent(student)
                        },
                        onView = {
                            onNavigateToDetail(student.idStudent)
                        },
                        onShare = {
                            /* TODO: Implement share intent */
                        }
                    )
                }
            }
        }
    }
}