package com.tumme.scrudstudents.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index


@Entity(
    tableName = "teachers",

    // Only for the moment disabled (for testing)

    /*
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["idUser"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],*/
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