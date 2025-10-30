package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


// This is the entity that represents subscribes, the relationship between student and courses

@Entity(
    tableName = "subscribes",
    primaryKeys = ["studentId", "courseId"],

    // These are the foreign keys that allow to connect the DB tables

    foreignKeys = [
        ForeignKey(entity = StudentEntity::class, parentColumns = ["idStudent"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CourseEntity::class, parentColumns = ["idCourse"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("studentId"), Index("courseId")]
)
data class SubscribeEntity(

    // These are the field contained in the Subscribe table

    val studentId: Int,
    val courseId: Int,
    val score: Float
)