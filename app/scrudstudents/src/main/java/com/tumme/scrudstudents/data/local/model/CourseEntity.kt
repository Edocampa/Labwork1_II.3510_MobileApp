package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey


// This is the entity that represents Course table in DB

@Entity(tableName = "courses")
data class CourseEntity(

    // All the fields that are contained in the Course table

    @PrimaryKey val idCourse: Int,  // Primary key that is unique for each course
    val nameCourse: String,
    val ectsCourse: Float,
    val levelCourse: String
)