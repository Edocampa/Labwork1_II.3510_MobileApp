package com.tumme.scrudstudents.data.local.model

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity (tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val idUser: Int = 0,
    val email: String,
    val password: String,
    val role: UserRole,
    val level: String? = null
)

enum class UserRole{
    STUDENT, TEACHER
}
