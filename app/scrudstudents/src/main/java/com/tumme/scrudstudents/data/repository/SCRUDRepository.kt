package com.tumme.scrudstudents.data.repository

import com.tumme.scrudstudents.data.local.dao.CourseDao
import com.tumme.scrudstudents.data.local.dao.StudentDao
import com.tumme.scrudstudents.data.local.dao.SubscribeDao
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import kotlinx.coroutines.flow.Flow
import com.tumme.scrudstudents.data.local.dao.SubscribeWithDetails
import com.tumme.scrudstudents.data.local.dao.UserDao
import com.tumme.scrudstudents.data.local.dao.TeacherDao
import com.tumme.scrudstudents.data.local.model.TeacherEntity
import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.local.model.CourseWithTeacher
import com.tumme.scrudstudents.data.local.model.SubscribeWithCourseAndTeacher
import com.tumme.scrudstudents.data.local.model.StudentWithGrade

/**
 * UNIFIED REPOSITORY - Central data access point for all entities
 *
 * This repository consolidates access to Student, Course and Subscribe data
 *
 * Architecture Pattern:
 * ViewModel → Repository → DAO → Database
 *
 * Constructor Parameters:
 * @param studentDao DAO for Student operations (injected by Hilt)
 * @param courseDao DAO for Course operations (injected by Hilt)
 * @param subscribeDao DAO for Subscribe operations (injected by Hilt)
 *
 *
 */
class SCRUDRepository(
    private val studentDao: StudentDao,    // Handles Student database operations
    private val courseDao: CourseDao,      // Handles Course database operations
    private val subscribeDao: SubscribeDao, // Handles Subscribe database operations
    private val userDao: UserDao,
    private val teacherDao: TeacherDao
) {

    // Students

    /**
     * Retrieves all students as a reactive Flow
     *
     * @return Flow<List<StudentEntity>> - Returns updated list when database changes
     *
     * Flow characteristics:
     * - Automatically updates observers when data changes
     * - ViewModel collects this Flow and updates UI
     * - Still to works when there are active collectors
     */
    fun getAllStudents(): Flow<List<StudentEntity>> = studentDao.getAllStudents()

    /**
     * Inserts a new student or replaces existing one
     *
     * @param student StudentEntity to insert
     *
     * suspend = Must be called from a coroutine
     */
    suspend fun insertStudent(student: StudentEntity) = studentDao.insert(student)

    /**
     * Deletes a student from the database
     *
     * @param student StudentEntity to delete
     *
     * This will also affect related Subscribe records
     * depending on foreign key constraints
     */
    suspend fun deleteStudent(student: StudentEntity) = studentDao.delete(student)

    /**
     * Retrieves a specific student by ID
     *
     * @param id Student's primary key
     * @return StudentEntity? - Found student or null
     *
     */
    suspend fun getStudentById(id: Int) = studentDao.getStudentById(id)

    // Courses

    /**
     * Get all courses from database
     *
     * Returns a Flow that is the complete list of courses
     *
     */
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    /**
     * Insert or update a course
     *
     * @param course CourseEntity to insert
     *
     * Uses OnConflictStrategy.REPLACE:
     * - If course with same courseId exists → updates it
     * - If courseId is new → inserts new course
     *
     * Validates in ViewModel before calling:
     * - ECTS must be > 0
     * - Level must be valid (P1-P3, B1-B3, A1-A3, MS, PhD)
     *
     */
    suspend fun insertCourse(course: CourseEntity) = courseDao.insert(course)

    /**
     * Delete a course from database
     *
     * @param course CourseEntity to delete
     *
     * Cascade behavior:
     * - Deletes course from courses table
     * - Automatically deletes all subscribes for this course
     *   due to foreign key CASCADE constraint in SubscribeEntity
     *
     */
    suspend fun deleteCourse(course: CourseEntity) = courseDao.delete(course)

    /**
     * Get a single course by ID
     *
     * @param id Course's primary key
     * @return CourseEntity? - Found course or null
     *
     */
    suspend fun getCourseById(id: Int) = courseDao.getCourseById(id)

    // Subscribes

    /**
     * Get all Subscribes
     *
     * Returns subscription data (student ID, course ID, score)
     */
    fun getAllSubscribes(): Flow<List<SubscribeEntity>> = subscribeDao.getAllSubscribes()

    /**
     * Get all Subscribes for a specific student
     *
     * @param sId Student ID (foreign key)
     *
     * Returns courses the student is enrolled in with their scores
     */
    fun getSubscribesByStudent(studentId: Int): Flow<List<SubscribeEntity>> {
        return subscribeDao.getSubscribesByStudent(studentId)
    }

    /**
     * Get all subscribes for a specific course
     *
     * @param cId Course ID (foreign key)
     *
     * Returns students enrolled in this course with their scores
     */
    fun getSubscribesByCourse(cId: Int): Flow<List<SubscribeEntity>> =
        subscribeDao.getSubscribesByCourse(cId)

    /**
     * Insert or update a subscribe
     *
     * @param subscribe SubscribeEntity containing:
     *      - studentId: Which student is enrolling
     *      - courseId: Which course they're enrolling in
     *      - score: Initial score (can be 0.0 or null)
     *
     * OnConflictStrategy.REPLACE:
     * - If (idStudent, idCourse) exists → updates the score
     * - If (idStudent, idCourse) is new → creates new enrollment
     *
     * Validates in ViewModel before calling:
     * - Score must be between 0 and 20
     * - Student and Course must be selected
     */
    suspend fun insertSubscribe(subscribe: SubscribeEntity) = subscribeDao.insert(subscribe)

    /**
     * Delete a subscribe
     *
     * @param subscribe SubscribeEntity to delete
     *
     * Removes the relationship between a student and a course
     * Does NOT delete the student or course, only the subscribe
     *
     */
    suspend fun deleteSubscribe(subscribe: SubscribeEntity) = subscribeDao.delete(subscribe)

    /**
     * Get enrollments with full details
     *
     * Uses JOIN query to combine data from students, courses and subscribes tables
     * Returns student names and course titles instead of numeric IDs
     *
     */
    fun getSubscribesWithDetails(): Flow<List<SubscribeWithDetails>> =
        subscribeDao.getSubscribesWithDetails()

    // User operations

    suspend fun registerUser(user: User): Long = userDao.insertUser(user)
    suspend fun login(email: String, password: String): User? =
        userDao.login(email,password)
    suspend fun findUserByEmail(email: String): User? = userDao.findByEmail(email)
    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    // Teacher operations

    suspend fun insertTeacher(teacher: TeacherEntity): Long =
        teacherDao.insertTeacher(teacher)
    suspend fun getTeacherByUserId(userId: Int): TeacherEntity? =
        teacherDao.getTeacherByUserId(userId)
    suspend fun getTeacherById(teacherId: Int): TeacherEntity? =
        teacherDao.getTeacherByUserId(teacherId)
    fun getAllTeachers(): Flow<List<TeacherEntity>> = teacherDao.getAllTeachers()

    suspend fun getCoursesByLevel(level: String): List<CourseWithTeacher> {
        return courseDao.getCoursesByLevelWithTeachers(level)
    }

    // COURSE WITH TEACHER METHODS

    suspend fun getCoursesWithTeachers(): List<CourseWithTeacher> {
        return courseDao.getCoursesWithTeachers()
    }

    suspend fun getCourseWithTeacher(courseId: Int): CourseWithTeacher? {
        return courseDao.getCourseWithTeacher(courseId)
    }

// SUBSCRIBE METHODS FOR STUDENT

    /**
     * Check if student is already enrolled in a course
     */
    suspend fun isStudentEnrolled(studentId: Int, courseId: Int): Boolean {
        return subscribeDao.getSubscribeByStudentAndCourse(studentId, courseId) != null
    }

    /**
     * Enroll student in course with initial score 0
     */
    suspend fun enrollStudent(studentId: Int, courseId: Int) {
        val subscribe = SubscribeEntity(
            studentId = studentId,
            courseId = courseId,
            score = 0f
        )
        subscribeDao.insert(subscribe)
    }

    /**
     * Get student by user ID
     */
    suspend fun getStudentByUserId(userId: Int): StudentEntity? {
        return studentDao.getStudentByUserId(userId)
    }

    /**
     * Get student subscriptions with full course and teacher details
     */
    suspend fun getStudentSubscriptionsWithDetails(studentId: Int): List<SubscribeWithCourseAndTeacher> {
        return subscribeDao.getSubscriptionsWithDetails(studentId)
    }

    /**
     * Get courses taught by a teacher
     */
    suspend fun getCoursesByTeacher(teacherId: Int): List<CourseEntity> {
        return courseDao.getCoursesByTeacher(teacherId)
    }

    /**
     * Update existing course
     */
    suspend fun updateCourse(course: CourseEntity) {
        courseDao.update(course)
    }

    /**
     * Delete course
     */
    suspend fun deleteCourse(courseId: Int) {
        courseDao.deleteById(courseId)
    }

    /**
     * Get students enrolled in a course with their grades
     */
    suspend fun getStudentsInCourseWithGrades(courseId: Int): List<StudentWithGrade> {
        val rawData = subscribeDao.getStudentsInCourseWithGrades(courseId)
        return rawData.map { raw ->
            StudentWithGrade(
                subscribeId = raw.subscribeId,
                studentId = raw.studentId,
                studentFirstName = raw.studentFirstName,
                studentLastName = raw.studentLastName,
                studentEmail = raw.studentEmail,
                studentLevel = raw.studentLevel,
                currentScore = raw.currentScore
            )
        }
    }

    /**
     * Update student grade
     */
    suspend fun updateStudentGrade(subscribeId: Int, score: Float) {
        subscribeDao.updateGrade(subscribeId, score)
    }




}