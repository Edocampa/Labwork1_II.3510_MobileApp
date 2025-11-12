package com.tumme.scrudstudents.data.local.model

import androidx.room.PrimaryKey
import androidx.room.Entity

/**
 * User - Authentication entity for all users in the system
 *
 * This is the entity for authentication, it contains login credentials and role information
 *
 * Database Table: users
 *
 * Relationships:
 * - One-to-One with StudentEntity (if role is STUDENT)
 * - One-to-One with TeacherEntity (if role is TEACHER)
 *
 * @property idUser Primary key, auto-generated unique identifier
 * @property email User's email address (used for login, must be unique)
 * @property password User's hashed password (stored securely)
 * @property role User's role in the system (STUDENT or TEACHER)
 * @property level Language proficiency level (only for STUDENT role)
 *               Valid values: P1-P3, B1-B3, A1-A3, MS, PhD
 *               Null for TEACHER role
 *
 * Usage:
 * - Created during registration
 * - Used for authentication (login)
 */

@Entity (tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val idUser: Int = 0,
    val email: String,
    val password: String,
    val role: UserRole,
    val firstName: String,
    val lastName: String,
    val level: String? = null // Only used if role = STUDENT
)

/**
 * UserRole - Enum defining user types in the system
 *
 * STUDENT: Can browse courses, enroll, view grades calculate final grade
 * TEACHER: Can declare courses, enter grades and view enrolled students
 */

enum class UserRole{
    STUDENT, TEACHER, ADMIN
}
