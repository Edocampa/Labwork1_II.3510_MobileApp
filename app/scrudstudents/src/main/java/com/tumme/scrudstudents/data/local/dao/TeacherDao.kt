package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    /**
     * Insert or update teacher
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    /**
     * Update teacher
     */

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    /**
     * Delete teacher
     */

    @Delete
    suspend fun deleteTeacher(teacher: TeacherEntity)

    /**
     * Get teacher by ID
     */

    @Query("SELECT * FROM teachers WHERE idTeacher = :teacherId")
    suspend fun getTeacherById(teacherId: Int): TeacherEntity

    /**
     * Get teacher by userId (link to User account)
     */

    @Query("SELECT * FROM teachers WHERE userId = :userId LIMIT 1")
    suspend fun getTeacherByUserId(userId: Int): TeacherEntity?

    /**
     * Get all teachers
     */

    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>





}