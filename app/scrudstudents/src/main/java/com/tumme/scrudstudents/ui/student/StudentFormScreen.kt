package com.tumme.scrudstudents.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.data.local.model.Gender
import com.tumme.scrudstudents.data.local.model.StudentEntity

/**
 * COMPOSABLE SCREEN - Form for creating a new student
 *
 * This screen allows users to:
 * - Enter student information (name, birth date, gender)
 * - Save the student to the database
 *
 * @param viewModel StudentListViewModel injected by Hilt
 * @param onSaved Callback invoked after successful save (navigate back)
 *
 *
 * State Management:
 * - Uses remember { mutableStateOf() } for form field values
 * - Each field updates independently on user input
 * - State survives recompositions but NOT configuration changes
 */
@Composable
fun StudentFormScreen(
    viewModel: StudentListViewModel = hiltViewModel(),
    onSaved: () -> Unit = {}
) {
    /**
     * FORM STATE - Local state variables for form fields
     *
     * remember { mutableStateOf() }:
     * - Creates state that survives recompositions
     * - Each recomposition preserves these values
     * - Lost on configuration change
     *
     * Initial values:
     * - id: Random number (0-10000)
     * - lastName: Empty string
     * - firstName: Empty string
     * - dobText: Default date "2000-01-01"
     * - gender: Default to NotConcerned
     */
    var id by remember { mutableStateOf((0..10000).random()) }
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var dobText by remember { mutableStateOf("2000-01-01") }
    var gender by remember { mutableStateOf(Gender.NotConcerned) }

    /**
     * DATE FORMATTER - Converts string to Date object
     *
     * Pattern: "yyyy-MM-dd"
     * Locale: Uses device's default locale for parsing
     *
     * Used to parse user input into Date object (for storing in database)
     */
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * FORM LAYOUT - Column with vertically stacked form fields
     *
     * fillMaxSize(): Takes all available screen space
     * padding(16.dp): Adds margin around the form
     */
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        /**
         * LAST NAME FIELD
         *
         * TextField parameters:
         * - value: Current text (from state)
         * - onValueChange: Lambda called when user types
         *   Updates state → triggers recomposition → shows new text
         * - label: Hint text displayed above/inside the field
         *
         */
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") }
        )

        Spacer(Modifier.height(8.dp))

        /**
         * FIRST NAME FIELD
         * Same pattern as lastName field
         */
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") }
        )

        Spacer(Modifier.height(8.dp))

        /**
         * DATE OF BIRTH FIELD
         *
         * Manual text input
         * User must type in format: yyyy-MM-dd
         */
        TextField(
            value = dobText,
            onValueChange = { dobText = it },
            label = { Text("Date of birth (yyyy-MM-dd)") }
        )

        Spacer(Modifier.height(8.dp))

        /**
         * GENDER SELECTOR - Simple button group
         *
         * Displays three buttons for gender selection:
         * - Male
         * - Female
         * - NotConcerned
         *
         */
        Row {
            listOf(Gender.Male, Gender.Female, Gender.NotConcerned).forEach { g ->
                Button(
                    onClick = {
                        gender = g
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(g.value)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        /**
         * SAVE BUTTON - Creates the student in database
         *
         * Process when clicked:
         * 1. Parse dobText string into Date object
         *    - If parsing fails, uses current date
         *
         * 2. Create StudentEntity with form data
         *    - Uses current state values
         *    - idStudent: Random number
         *
         * 3. Call ViewModel to insert student
         *    - viewModel.insertStudent() launches coroutine
         *    - Calls repository → DAO → inserts into database
         *    - Database change triggers Flow update
         *    - StudentListScreen recomposes with new student
         *
         * 4. Execute onSaved callback
         *    - Typically navigates back to list screen
         *    - Parent composable handles navigation
         */
        Button(onClick = {

            val dob = dateFormat.parse(dobText) ?: Date()


            val userId = 0
            val student = StudentEntity(
                idStudent = id,
                lastName = lastName,
                firstName = firstName,
                dateOfBirth = dob,
                gender = gender,
                userId = 0,
                level = "B1" // Default at the moment, only for testing
            )


            viewModel.insertStudent(student)


            onSaved()
        }) {
            Text("Save")
        }
    }
}