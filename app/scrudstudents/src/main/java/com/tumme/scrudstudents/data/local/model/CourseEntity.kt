package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index


// This is the entity that represents Course table in DB

    @Entity(tableName = "courses",
    foreignKeys = [
    ForeignKey(
        entity = TeacherEntity::class,
        parentColumns = ["idTeacher"],
        childColumns = ["teacherId"],
        onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["teacherId"]),
    ]


)
data class CourseEntity(

    // All the fields that are contained in the Course table

    @PrimaryKey val idCourse: Int,  // Primary key that is unique for each course
    val nameCourse: String,
    val ectsCourse: Float,
    val teacherId: Int,
    val levelCourse: String
)