package com.tumme.scrudstudents.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * SubscribeWithCourseAndTeacher - Complete subscription info
 *
 * Joins Subscribe with its Course and Teacher information
 * Used to show enrolled courses with all details
 */
data class SubscribeWithCourseAndTeacher(
    @Embedded
    val subscribe: SubscribeEntity,

    @Relation(
        entity = CourseEntity::class,
        parentColumn = "courseId",
        entityColumn = "idCourse"
    )
    val courseWithTeacher: CourseWithTeacher
)