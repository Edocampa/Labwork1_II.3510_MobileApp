package com.tumme.scrudstudents.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tumme.scrudstudents.R
import com.tumme.scrudstudents.data.local.model.UserRole
import com.tumme.scrudstudents.ui.viewmodel.AuthEvent
import com.tumme.scrudstudents.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Register Screen - Create new user account
 *
 * Allows users to register by choosing role (Student/Teacher),
 * entering email, password and level (if Student)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // State

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var selectedLevel by remember { mutableStateOf("A1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var showLevelMenu by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    // Available levels for students
    val levels = listOf("P1", "P2", "P3", "B1", "B2", "B3", "A1", "A2", "A3", "MS", "PhD")

    val passwordsMismatchError = stringResource(R.string.passwords_do_not_match)

    val context = LocalContext.current

    // Event handling

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthEvent.RegisterSuccess -> {
                    onRegisterSuccess()
                }
                is AuthEvent.Error -> {
                    val finalMessage = if (event.dynamicPart == null) {
                        context.getString(event.messageId)
                    } else {
                        context.getString(event.messageId, event.dynamicPart)
                    }
                    errorMessage = finalMessage
                }
                else -> {}
            }
        }
    }

    // UI

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text( text = stringResource(R.string.register)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // Header

            Text(
                text = stringResource(R.string.create_account),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.register_to_get_started),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Role Selector

            ExposedDropdownMenuBox(
                expanded = showRoleMenu,
                onExpandedChange = { showRoleMenu = !showRoleMenu && !isLoading }
            ) {
                OutlinedTextField(
                    value = when (selectedRole) {
                        UserRole.STUDENT -> "Student"
                        UserRole.TEACHER -> "Teacher"
                        UserRole.ADMIN -> "Admin"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.i_am_a)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Role icon"
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRoleMenu)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showRoleMenu,
                    onDismissRequest = { showRoleMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.student)) },
                        onClick = {
                            selectedRole = UserRole.STUDENT
                            showRoleMenu = false
                            errorMessage = null
                        },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.teacher)) },
                        onClick = {
                            selectedRole = UserRole.TEACHER
                            showRoleMenu = false
                            errorMessage = null
                        },
                        leadingIcon = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Level Selector (Only for Students)

            if (selectedRole == UserRole.STUDENT) {
                ExposedDropdownMenuBox(
                    expanded = showLevelMenu,
                    onExpandedChange = { showLevelMenu = !showLevelMenu && !isLoading }
                ) {
                    OutlinedTextField(
                        value = selectedLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.level)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Level icon"
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLevelMenu)
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showLevelMenu,
                        onDismissRequest = { showLevelMenu = false }
                    ) {
                        levels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    selectedLevel = level
                                    showLevelMenu = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.first_name)) },
                placeholder = { Text(stringResource(R.string.mario)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.first_name)
                    )
                },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.last_name)) },
                placeholder = { Text("Rossi") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.last_name)
                    )
                },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.email)) },
                placeholder = { Text(stringResource(R.string.example_university_edu)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email icon"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.password)) },
                placeholder = { Text(stringResource(R.string.at_least_4_characters)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password icon"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Input

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.confirm_password)) },
                placeholder = { Text(stringResource(R.string.re_enter_your_password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm password icon"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error Message

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button

            Button(
                onClick = {
                    // Client-side validation for password match
                    if (password != confirmPassword) {
                        errorMessage = passwordsMismatchError
                        return@Button
                    }

                    // Call ViewModel to register
                    viewModel.register(
                        email = email,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        role = selectedRole,
                        level = if (selectedRole == UserRole.STUDENT) selectedLevel else null
                    )
                },
                enabled = !isLoading &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        firstName.isNotBlank() &&
                        lastName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text =  stringResource(R.string.register),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.already_have_an_account),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateBack,
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(id = R.string.login),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

}
