package com.tumme.scrudstudents.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * CourseWithTeacher - Joins Course with its Teacher
 *
 * Used to display course information with teacher details
 */
data class CourseWithTeacher(
    @Embedded val course: CourseEntity,

    @Relation(
        parentColumn = "teacherId",
        entityColumn = "idTeacher"
    )
    val teacher: TeacherEntity
)
