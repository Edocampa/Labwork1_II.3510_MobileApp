package com.tumme.scrudstudents.ui.subscribe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.R
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.CourseEntity

/**
 * COMPOSABLE SCREEN - Form to create/update a subscribe
 *
 * This form allows:
 * - Selecting a student from a dropdown (shows full name)
 * - Selecting a course from a dropdown (shows course name)
 * - Entering/updating the score
 *
 *
 * @param viewModel SubscribeViewModel injected by Hilt
 * @param onSaved Callback executed after successful save (navigate back)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribeFormScreen(
    viewModel: SubscribeViewModel = hiltViewModel(),
    onSaved: () -> Unit = {}
) {
    /**
     * FORM STATE - Variables for form fields
     *
     * remember { mutableStateOf() }:
     * - Creates state that survives recompositions
     * - Lost on configuration change (screen rotation)
     */
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedCourse by remember { mutableStateOf<CourseEntity?>(null) }
    var scoreText by remember { mutableStateOf("") }

    /**
     * DROPDOWN STATE - Manage open/closed state of dropdowns
     */
    var expandedStudentDropdown by remember { mutableStateOf(false) }
    var expandedCourseDropdown by remember { mutableStateOf(false) }

    /**
     * LOAD DATA - Get lists of students and courses from ViewModel
     *
     * These StateFlows are collected and converted to Compose State
     * When database changes → Flow emits → State updates → UI recomposes
     */
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()

    /**
     * FORM LAYOUT - Column with vertically stacked fields
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        /**
         * SCREEN TITLE
         */
        Text(
            text = stringResource(R.string.new_enrollment),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        /**
         * STUDENT DROPDOWN - Select which student to enroll
         *
         * ExposedDropdownMenuBox:
         * - Material Design 3 standard dropdown component
         * - expanded: Controls if menu is open or closed
         * - onExpandedChange: Called when user clicks to open/close
         *
         */
        Text(
            text = stringResource(R.string.select_student),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expandedStudentDropdown,
            onExpandedChange = { expandedStudentDropdown = it }
        ) {
            /**
             * DROPDOWN TEXT FIELD - Shows selected student
             *
             * .menuAnchor(): Connects this field to the dropdown menu
             * readOnly: Prevents manual typing (only selection allowed)
             *
             */
            OutlinedTextField(
                value = selectedStudent?.let {
                    stringResource(R.string.full_name_student, it.firstName, it.lastName)
                } ?: stringResource(R.string.select_a_student),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.student)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudentDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            /**
             * DROPDOWN MENU - List of all students
             *
             * Shows when expandedStudentDropdown = true
             * Automatically positions below the text field
             */
            ExposedDropdownMenu(
                expanded = expandedStudentDropdown,
                onDismissRequest = { expandedStudentDropdown = false }
            ) {
                /**
                 * MENU ITEMS - One option for each student
                 *
                 * Iterates over all students from database
                 * Creates a clickable menu item for each
                 *
                 */
                students.forEach { student ->
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(id = R.string.full_name_student))
                        },
                        onClick = {
                            selectedStudent = student  // Update selected student
                            expandedStudentDropdown = false  // Close dropdown
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * COURSE DROPDOWN - Select which course to enroll in
         *
         * Same pattern as Student dropdown
         *
         */
        Text(
            text = stringResource(R.string.select_course),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expandedCourseDropdown,
            onExpandedChange = { expandedCourseDropdown = it }
        ) {
            /**
             * DROPDOWN TEXT FIELD - Shows selected course
             *
             */
            OutlinedTextField(
                value = selectedCourse?.let {
                    stringResource(
                        R.string.nameCourse_ects_level,
                        it.nameCourse,
                        it.ectsCourse,
                        it.levelCourse
                    )
                } ?: stringResource(R.string.select_a_course),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.course)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourseDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            /**
             * DROPDOWN MENU - List of all courses
             */
            ExposedDropdownMenu(
                expanded = expandedCourseDropdown,
                onDismissRequest = { expandedCourseDropdown = false }
            ) {
                /**
                 * MENU ITEMS - One option for each course
                 *
                 */
                courses.forEach { course ->
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(id = R.string.nameCourse_ects_level))
                        },
                        onClick = {
                            selectedCourse = course  // Update selected course
                            expandedCourseDropdown = false  // Close dropdown
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * SCORE INPUT FIELD
         *
         *
         * User types score as text
         * Converted to Float on save
         *
         * Validation happens in ViewModel:
         * - Score must be >= 0 and <= 20
         */
        OutlinedTextField(
            value = scoreText,
            onValueChange = { scoreText = it },
            label = { Text(stringResource(R.string.score_0_20)) },
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text(
                    stringResource(R.string.enter_the_student_s_grade_0_to_20),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        /**
         * SAVE BUTTON
         *
         * Process when clicked:
         * 1. Validate that student and course are selected
         * 2. Convert scoreText to Float (default 0.0 if invalid)
         * 3. Create SubscribeEntity with selected IDs and score
         * 4. Call ViewModel to insert (with validation)
         * 5. ViewModel validates score range (0-20)
         * 6. If valid → saves to database
         * 7. Database change → Flow updates → List screen recomposes
         * 8. Execute onSaved() callback → navigate back
         *
         * enabled: Button is only clickable if all fields are filled
         * - selectedStudent != null
         * - selectedCourse != null
         * - scoreText is not blank
         */
        Button(
            onClick = {
                // Get IDs from selected entities
                val studentId = selectedStudent?.idStudent ?: 0
                val courseId = selectedCourse?.idCourse ?: 0

                // Convert score text to Float (default 0.0 if parsing fails)
                val score = scoreText.toFloatOrNull() ?: 0f

                // Create SubscribeEntity
                val subscribe = SubscribeEntity(
                    studentId = studentId,
                    courseId = courseId,
                    score = score
                )

                // Save via ViewModel (includes validation)
                viewModel.insertSubscribe(subscribe)

                // Navigate back to list
                onSaved()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedStudent != null &&
                    selectedCourse != null &&
                    scoreText.isNotBlank()  // Enable only if all fields filled
        ) {
            Text(stringResource(R.string.save_enrollment))
        }

        /**
         * HELPER TEXT - Show validation requirements
         */
        if (selectedStudent == null || selectedCourse == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.please_select_both_student_and_course),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
