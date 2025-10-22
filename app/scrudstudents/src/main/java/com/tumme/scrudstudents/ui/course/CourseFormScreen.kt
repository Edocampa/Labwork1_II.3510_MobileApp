package com.tumme.scrudstudents.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.CourseEntity

/**
 * COURSE FORM SCREEN - Create or update a course
 *
 * Dual-mode screen:
 * - CREATE: courseId = 0 → empty form with random ID
 * - UPDATE: courseId > 0 → pre-populated with existing course data
 *
 * Features:
 * - Text inputs for name and ECTS
 * - Dropdown for academic level selection (P1-P3, B1-B3, A1-A3, MS, PhD)
 * - Validation: ECTS > 0 (Part 2 Challenge)
 * - User feedback via Snackbar on validation errors
 * - OnConflictStrategy.REPLACE handles both insert and update
 *
 * @param courseId 0 for create mode, >0 for edit mode
 * @param viewModel CourseViewModel injected by Hilt
 * @param onSaved Callback executed after successful save (navigates back)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormScreen(
    courseId: Int = 0,  // 0 = CREATE, >0 = UPDATE
    viewModel: CourseViewModel = hiltViewModel(),
    onSaved: () -> Unit = {}
) {
    /**
     * FORM STATE - Remember values across recompositions
     *
     * isEditMode: true if updating existing course
     * id: course ID (random for create, from parameter for edit)
     * nameCourse, ectsText, levelCourse: form field values
     * expandedDropdown: controls dropdown menu visibility
     */
    var isEditMode by remember { mutableStateOf(courseId > 0) }
    var id by remember { mutableStateOf(courseId) }
    var nameCourse by remember { mutableStateOf("") }
    var ectsText by remember { mutableStateOf("") }
    var levelCourse by remember { mutableStateOf("P1") }
    var expandedDropdown by remember { mutableStateOf(false) }

    /**
     * SNACKBAR STATE - For displaying error/success messages
     */
    val snackbarHostState = remember { SnackbarHostState() }

    /**
     * Valid academic levels for dropdown
     * P = Preparatory, B = Bachelor, A = Advanced, MS/PhD = Graduate
     */
    val validLevels = listOf("P1", "P2", "P3", "B1", "B2", "B3",
        "A1", "A2", "A3", "MS", "PhD")

    /**
     * EVENT COLLECTOR - Listens for ViewModel events
     *
     * Collects events from ViewModel (success/error messages)
     * - On "Course saved" → navigates back
     * - On error message → displays snackbar
     *
     * This ensures proper feedback to the user when validation fails
     * or when the save operation succeeds.
     */
    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            if (message.contains("saved", ignoreCase = true)) {
                // Success: navigate back to list
                onSaved()
            } else {
                // Error: show snackbar with error message
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    /**
     * LOAD COURSE DATA - Runs once when screen opens
     *
     * If courseId > 0 (edit mode):
     * - Fetches course from database
     * - Pre-populates form fields
     *
     * If courseId = 0 (create mode):
     * - Generates random ID
     * - Leaves fields empty
     */
    LaunchedEffect(courseId) {
        if (courseId > 0) {
            val course = viewModel.findCourse(courseId)

            if (course != null) {
                id = course.idCourse
                nameCourse = course.nameCourse
                ectsText = course.ectsCourse.toString()
                levelCourse = course.levelCourse
                isEditMode = true
            }
        } else {
            id = (0..10000).random()
            isEditMode = false
        }
    }

    /**
     * SCAFFOLD - Material Design layout with Snackbar support
     *
     * Wraps the form in a Scaffold to provide:
     * - Snackbar host for displaying messages
     * - Proper Material Design structure
     */
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        /**
         * FORM LAYOUT
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            /**
             * SCREEN TITLE - Changes based on mode
             */
            Text(
                text = if (isEditMode) "Update course" else "New course",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            /**
             * COURSE NAME INPUT
             */
            OutlinedTextField(
                value = nameCourse,
                onValueChange = { nameCourse = it },
                label = { Text("Course name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * ECTS INPUT - Float value with validation
             *
             * Validation: Must be > 0 (Part 2 Challenge requirement)
             * Handled in ViewModel.insertCourse()
             * User feedback provided via Snackbar
             */
            OutlinedTextField(
                value = ectsText,
                onValueChange = { ectsText = it },
                label = { Text("ECTS Credits") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        "It must be > 0",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            /**
             * LEVEL DROPDOWN - Academic level selection
             *
             * ExposedDropdownMenuBox: Material Design 3 dropdown component
             * Allows selection from predefined academic levels
             */
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it }
            ) {
                OutlinedTextField(
                    value = levelCourse,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Academic level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                /**
                 * DROPDOWN MENU - List of valid levels
                 */
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.heightIn(max = 200.dp)

                ) {
                    validLevels.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level) },
                            onClick = {
                                levelCourse = level
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            /**
             * SAVE BUTTON
             *
             * Process:
             * 1. Convert ECTS text to Float (default 0 if invalid)
             * 2. Create CourseEntity
             * 3. Call ViewModel to insert/update (REPLACE strategy)
             * 4. ViewModel validates ECTS > 0
             * 5. If validation fails: Snackbar shows error
             * 6. If validation succeeds: Course saved, navigate back
             *
             * User feedback flow:
             * - Validation error → Snackbar displays "Error: ECTS must be > 0"
             * - Success → Automatically navigates back to list
             *
             * Enabled only if name and ECTS are not blank
             */
            Button(
                onClick = {
                    val ects = ectsText.toFloatOrNull() ?: 0f

                    val course = CourseEntity(
                        idCourse = id,
                        nameCourse = nameCourse,
                        ectsCourse = ects,
                        levelCourse = levelCourse
                    )

                    // Call ViewModel - validation happens here
                    // Events flow will handle navigation and user feedback
                    viewModel.insertCourse(course)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nameCourse.isNotBlank() && ectsText.isNotBlank()
            ) {
                Text(if (isEditMode) "Update course" else "Save course")
            }
        }
    }
}