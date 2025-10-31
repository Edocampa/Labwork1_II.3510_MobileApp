package com.tumme.scrudstudents.data.local.model

/**
 * StudentWithGrade - Student info with their current grade
 *
 * Used in TeacherEnterGradesScreen to display and edit grades
 */
data class StudentWithGrade(
    val subscribeId: Int,
    val studentId: Int,
    val studentFirstName: String,
    val studentLastName: String,
    val studentEmail: String,
    val studentLevel: String,
    val currentScore: Float
)