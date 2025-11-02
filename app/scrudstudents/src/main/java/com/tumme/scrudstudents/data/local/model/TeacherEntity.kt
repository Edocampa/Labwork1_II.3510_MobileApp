package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index


/**
 * TeacherEntity - Represents a teacher in the database
 *
 * This entity is linked to a User account through a foreign key relationship
 *
 * Database Table: teachers
 *
 * Relationships:
 * - Many-to-One with User (each teacher has one user account)
 * - One-to-Many with Course (each teacher can teach multiple courses)
 *
 * @property idTeacher Primary key, auto-generated unique identifier
 * @property userId Foreign key linking to User table (required for authentication)
 * @property firstName Teacher's first name
 * @property lastName Teacher's last name
 *
 * Foreign Key Constraints:
 * - References User.idUser
 * - CASCADE delete: when a User is deleted, their Teacher profile is also deleted
 */


@Entity(
    tableName = "teachers",


    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["idUser"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"])
    ]

    )

data class TeacherEntity (
    @PrimaryKey(autoGenerate = true)
    val idTeacher: Int = 0,
    val userId: Int,
    val firstName: String,
    val lastName: String

)