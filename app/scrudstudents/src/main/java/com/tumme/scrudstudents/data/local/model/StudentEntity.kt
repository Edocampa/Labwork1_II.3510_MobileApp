package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import androidx.room.ForeignKey
import androidx.room.Index


/**
 * This entity represents the "Student" table in the Room database
 *
 * This class defines the structure of student data.
 * Room uses this class to automatically create the SQL table.
 *
 * Data Flow: Entity → DAO → Repository → ViewModel → UI
 */


@Entity(tableName = "students",
foreignKeys = [
    ForeignKey(
        entity = User::class,
        parentColumns = ["idUser"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )
],
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class StudentEntity(

    // These variables represent all the fields that a student has

    @PrimaryKey val idStudent: Int, // Primary key -> unique identifier for each student
    val userId : Int,
    val lastName: String,
    val firstName: String,
    val dateOfBirth: Date,
    val gender: Gender,
    val level: String
)