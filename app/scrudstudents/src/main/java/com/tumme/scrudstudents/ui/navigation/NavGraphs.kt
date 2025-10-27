package com.tumme.scrudstudents.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

import com.tumme.scrudstudents.ui.student.StudentListScreen
import com.tumme.scrudstudents.ui.student.StudentFormScreen
import com.tumme.scrudstudents.ui.student.StudentDetailScreen
import com.tumme.scrudstudents.ui.course.CourseListScreen
import com.tumme.scrudstudents.ui.course.CourseFormScreen
import com.tumme.scrudstudents.ui.subscribe.SubscribeFormScreen
import com.tumme.scrudstudents.ui.subscribe.SubscribeListScreen
import com.tumme.scrudstudents.ui.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.tumme.scrudstudents.data.local.model.UserRole
import androidx.compose.runtime.getValue

/**
 * ROUTES - Central definition of all navigation destinations
 *
 *
 */
object Routes {
    /**
     * STUDENT_LIST - Main screen showing all students in a table
     *
     * This is the start destination
     *
     */
    const val STUDENT_LIST = "student_list"

    /**
     * STUDENT_FORM - Screen for creating a new student
     *
     * Create only
     *
     * Navigation: Reached via (+) button on STUDENT_LIST
     */
    const val STUDENT_FORM = "student_form"

    /**
     * STUDENT_DETAIL - Screen showing detailed view of one student
     *
     * Required parameter: studentId (Int)
     * Format: "student_detail/{studentId}"
     *
     *
     * Navigation: Reached by clicking "View" button on a student row
     */
    const val STUDENT_DETAIL = "student_detail/{studentId}"

    const val COURSE_LIST = "course_list"
    const val COURSE_FORM = "course_form"

    const val COURSE_EDIT = "course_form/{courseId}"

    const val SUBSCRIBE_LIST = "subscribe_list"
    const val SUBSCRIBE_FORM = "subscribe_form"

    // AUTH ROUTES
    const val LOGIN = "login"
    const val REGISTER = "register"

    // STUDENT ROUTES
    const val STUDENT_HOME = "student_home"
    const val STUDENT_COURSES = "student_courses"
    const val STUDENT_SUBSCRIPTIONS = "student_subscriptions"
    const val STUDENT_GRADES = "student_grades"
    const val STUDENT_FINAL_GRADE = "student_final_grade"

    // TEACHER ROUTES
    const val TEACHER_HOME = "teacher_home"
    const val TEACHER_COURSES = "teacher_courses"
    const val TEACHER_DECLARE = "teacher_declare_courses"
    const val TEACHER_ENTER_GRADES = "teacher_enter_grades"
    const val TEACHER_STUDENTS = "teacher_students"
}

/**
 * APP NAV HOST - Main navigation component for the entire app
 *
 * This is the root navigation composable that manages all screen transitions
 *
 * Navigation Architecture:
 * - NavController: Manages navigation stack and handles back button
 * - NavHost: Container that displays the current screen
 * - composable(): Defines each destination and its UI
 *
 * How Navigation Works:
 * 1. User interacts with UI (clicks button, swipes, etc.)
 * 2. Screen calls navController.navigate("route")
 * 3. NavController adds route to back stack
 * 4. NavHost switches to the composable for that route
 * 5. Old screen is kept in memory (back stack)
 * 6. User can press back to return to previous screen
 *
 */
@Composable
fun AppNavHost(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    /**
     * NavController - The navigation state holder
     *
     * rememberNavController() creates a NavController that:
     * - Survives recompositions
     * - Maintains navigation state (current screen, back stack)
     * - Provides navigation methods (navigate, popBackStack, etc.)
     *
     * This is passed to child screens via callbacks so they can navigate
     */
    val navController = rememberNavController()

    // Observe current user to determine start destination
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determine start destination based on auth state
    val startDestination = when {
        currentUser == null -> Routes.LOGIN
        currentUser?.role == UserRole.STUDENT -> Routes.STUDENT_HOME
        currentUser?.role == UserRole.TEACHER -> Routes.TEACHER_HOME
        else -> Routes.LOGIN
    }

    /**
     * NavHost - Container that displays the current destination
     *
     * Parameters:
     * - navController: The controller managing navigation state
     * - startDestination: Initial route shown on app launch
     *
     * The NavHost listens to NavController and displays the appropriate
     * composable based on the current route in the back stack
     */
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {


        // AUTH SCREENS

        /**
         * LOGIN SCREEN - Entry point for authentication
         *
         * This is the first screen users see when not authenticated
         *
         * Callbacks:
         * - onNavigateToRegister: User clicks "Don't have an account? Register"
         *   → Navigates to registration screen
         *
         * - onLoginSuccess: Called when login succeeds, receives authenticated User
         *   → Determines destination based on user role (Student or Teacher)
         *   → Navigates to appropriate home screen
         *   → Clears back stack (popUpTo with inclusive=true) so user can't
         *     go back to login screen after logging in
         */
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = { user ->
                    // Role-based navigation: different home screens for different users
                    val destination = when (user.role) {
                        UserRole.STUDENT -> Routes.STUDENT_HOME
                        UserRole.TEACHER -> Routes.TEACHER_HOME
                    }
                    navController.navigate(destination) {
                        // popUpTo removes all screens up to LOGIN from back stack
                        // inclusive = true also removes LOGIN itself
                        // Result: pressing back won't return to login screen
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        /**
         * REGISTER SCREEN - Create new user account
         *
         * Allows users to create account by choosing role (Student/Teacher),
         * entering email, password and level (if Student)
         *
         * Callbacks:
         * - onNavigateBack: User clicks back button or "Already have account?"
         *   → navigateUp() removes current screen, returns to login
         *
         * - onRegisterSuccess: Registration successful
         *   → Returns to login screen so user can login with new credentials
         *   → navigateUp() is simple back navigation (doesn't clear stack)
         */
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateBack = {
                    navController.navigateUp()  // Simple back navigation
                },
                onRegisterSuccess = {
                    navController.navigateUp()  // Return to login after registration
                }
            )
        }

        // STUDENT SCREENS

        /**
         * STUDENT HOME SCREEN - Main dashboard for student users
         *
         * This is the student's landing page after login
         * Provides navigation to all student features
         *
         * Navigation options:
         * - Courses: Browse and enroll in courses
         * - Subscriptions: View enrolled courses
         * - Grades: Check grades for each course
         * - Final Grade: See weighted average (ECTS-based calculation)
         *
         * onLogout:
         * - Clears authentication state (authViewModel.logout())
         * - Navigates back to login screen
         * - popUpTo(0) clears ENTIRE back stack
         * - Result: user can't press back to return to student area
         */
        composable(Routes.STUDENT_HOME) {
            StudentHomeScreen(
                onNavigateToCourses = {
                    navController.navigate(Routes.STUDENT_COURSES)
                },
                onNavigateToSubscriptions = {
                    navController.navigate(Routes.STUDENT_SUBSCRIPTIONS)
                },
                onNavigateToGrades = {
                    navController.navigate(Routes.STUDENT_GRADES)
                },
                onNavigateToFinalGrade = {
                    navController.navigate(Routes.STUDENT_FINAL_GRADE)
                },
                onLogout = {
                    authViewModel.logout()  // Clear user session
                    navController.navigate(Routes.LOGIN) {
                        // popUpTo(0) removes ALL screens from back stack
                        // inclusive = true ensures even the root screen is removed
                        // Fresh start from login
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // TEACHER SCREENS

        /**
         * TEACHER HOME SCREEN - Main dashboard for teacher users
         *
         * This is the teacher's landing page after login
         * Provides navigation to all teacher features
         *
         * Navigation options:
         * - Courses: View courses they teach
         * - Enter Grades: Assign grades to students in their courses
         * - Students: See list of enrolled students per course
         *
         * onLogout: Same logic as student logout
         * - Clears session and returns to login with empty back stack
         */
        composable(Routes.TEACHER_HOME) {
            TeacherHomeScreen(
                onNavigateToCourses = {
                    navController.navigate(Routes.TEACHER_COURSES)
                },
                onNavigateToEnterGrades = {
                    navController.navigate(Routes.TEACHER_ENTER_GRADES)
                },
                onNavigateToStudents = {
                    navController.navigate(Routes.TEACHER_STUDENTS)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }


        /**
         * DESTINATION 1: Student List Screen
         *
         * Route: "student_list"
         * Parameters: None
         *
         * This is the main screen showing all students
         * Users can navigate from here to:
         * - STUDENT_FORM (create new student)
         * - STUDENT_DETAIL (view student details)
         * - COURSE_LIST (view all courses)
         * - SUBSCRIBE_LIST(view all subscribes)
         *
         * Navigation callbacks:
         * - onNavigateToForm: Called when (+) button is clicked
         *   → Navigates to form screen
         * - onNavigateToDetail: Called when "View" button clicked
         *   → Navigates to detail screen with student ID
         *
         * navController.navigate():
         * - Adds new destination to back stack
         * - NavHost switches to that destination
         * - Previous screen remains in back stack
         */
        composable(Routes.STUDENT_LIST) {
            StudentListScreen(
                onNavigateToForm = {
                    navController.navigate(Routes.STUDENT_FORM)
                },
                onNavigateToDetail = { id ->
                    navController.navigate("student_detail/$id")
                },
                onNavigateToCourses = { navController.navigate(Routes.COURSE_LIST) },
                onNavigateToSubscribes = { navController.navigate(Routes.SUBSCRIBE_LIST) }
            )
        }

        /**
         * DESTINATION 2: Student Form Screen
         *
         * Route: "student_form"
         * Parameters: None
         *
         * This screen allows creating a new student
         *
         * Navigation callback:
         * - onSaved: Called after successfully saving a student
         *   → Returns to previous screen (STUDENT_LIST)
         *
         * popBackStack():
         * - Removes current screen from back stack
         * - Returns to previous screen
         *
         * This creates the pattern:
         * 1. User clicks + button on list
         * 2. Form screen appears
         * 3. User fills form and clicks Save
         * 4. Student saved to database
         * 5. Form screen closes, back to list
         * 6. List automatically shows new student
         */
        composable(Routes.STUDENT_FORM) {
            StudentFormScreen(
                onSaved = {
                    // Pop current screen, return to list
                    navController.popBackStack()
                }
            )
        }

        /**
         * DESTINATION 3: Student Detail Screen
         *
         * Route: "student_detail/{studentId}"
         * Parameters: studentId (required, Int)
         *
         * This screen shows detailed information about one student
         *
         *
         * Argument extraction:
         * - backStackEntry: Contains navigation arguments
         * - getInt("studentId"): Extracts the integer parameter
         * - ?: 0: Default value if parsing fails (safety fallback)
         *
         * Flow:
         * 1. User clicks "View" on student with ID x
         * 2. List screen calls onNavigateToDetail(x)
         * 3. Navigation builds route "student_detail/x"
         * 4. NavHost matches this route to this composable
         * 5. Parameter "x" is extracted as studentId
         * 6. StudentDetailScreen receives studentId = x
         * 7. Screen loads and displays that student's data
         */
        composable(
            route = "student_detail/{studentId}",
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            /**
             * Extract studentId from navigation arguments
             *
             * Process:
             * 1. backStackEntry.arguments: Bundle containing all parameters
             * 2. getInt("studentId"): Extracts the integer parameter
             * 3. ?: 0: Null safety - defaults to 0 if extraction fails
             *
             * Set 0 like default because:
             * - Prevents null pointer exceptions
             * - 0 is often used as "invalid ID" in databases
             */
            val id = backStackEntry.arguments?.getInt("studentId") ?: 0

            /**
             * Display the detail screen with the student ID
             *
             * The screen will:
             * 1. Use the ID to fetch student from database
             * 2. Display student information
             * 3. Provide a back button to return to list
             *
             * onBack callback:
             * - Allows user to return to previous screen
             * - Removes detail screen from back stack
             */
            StudentDetailScreen(
                studentId = id,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        // Course routes

        composable(Routes.COURSE_LIST) {
            CourseListScreen(
                onNavigateToForm = {
                    navController.navigate(Routes.COURSE_FORM)
                },
                onNavigateToEdit = { courseId: Int ->
                    navController.navigate("course_form/$courseId")
                },
                onNavigateToStudents = { navController.navigate(Routes.STUDENT_LIST) },
                onNavigateToSubscribes = { navController.navigate(Routes.SUBSCRIBE_LIST) }
            )
        }

        composable(Routes.COURSE_FORM) {
            CourseFormScreen(
                courseId = 0,
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "course_form/{courseId}",
            arguments = listOf(
                navArgument("courseId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
            CourseFormScreen(
                courseId = courseId,
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.SUBSCRIBE_LIST) {
            SubscribeListScreen(
                onNavigateToForm = {
                    navController.navigate(Routes.SUBSCRIBE_FORM)
                },
                onNavigateToStudents = { navController.navigate(Routes.STUDENT_LIST) },
                onNavigateToCourses = { navController.navigate(Routes.COURSE_LIST) }
            )
        }

        /**
         * DESTINATION: Subscribe Form Screen
         * Allows enrolling a student in a course with a score
         */
        composable(Routes.SUBSCRIBE_FORM) {
            SubscribeFormScreen(
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

    }
}

// PLACEHOLDER SCREENS
// I create these to avoid compilation error before implementing UI

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (com.tumme.scrudstudents.data.local.model.User) -> Unit
) {
    // TODO: Implement LoginScreen UI
}

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    // TODO: Implement RegisterScreen UI
}

@Composable
fun StudentHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToFinalGrade: () -> Unit,
    onLogout: () -> Unit
) {
    // TODO: Implement StudentHomeScreen UI
}

@Composable
fun TeacherHomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToEnterGrades: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onLogout: () -> Unit
) {
    // TODO: Implement TeacherHomeScreen UI
}