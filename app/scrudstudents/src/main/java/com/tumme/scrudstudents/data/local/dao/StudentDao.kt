package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) --> Interface for accessing Student data
 *
 * Room automatically generates the implementation of these methods at compile time
 * Each method corresponds to an SQL operation on the database
 *
 * Data Flow:
 * - Repository calls DAO methods
 * - DAO executes SQL queries on the database
 * - Results are returned to repo
 */

@Dao
interface StudentDao {

    /**
     * Retrieves all students from the database, sorted alphabetically
     *
     * @return Flow<List<StudentEntity>> - A reactive stream of the student list
     *
     * Flow characteristics:
     * - Returns a new list whenever the database changes
     * - UI observes this Flow and updates automatically
     * - Works only when there are observers, otherwise doesn't work
     */
    @Query("SELECT * FROM students ORDER BY lastName, firstName")
    fun getAllStudents(): Flow<List<StudentEntity>>

    /**
     * Inserts a new student into the database
     *
     * @param student - StudentEntity object to insert
     *
     * OnConflictStrategy.REPLACE:
     * - If idStudent already exists → replaces the old record
     * - If idStudent is new → inserts a new record
     *
     * suspend:
     * - This function must be called from a coroutine
     * - Runs asynchronously without blocking the main thread
     * - Prevents UI freezing during database operations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    /**
     * Deletes a specific student from the database
     *
     * @param student - StudentEntity object to insert
     *
     * Room uses the primary key (idStudent) to identify which record to delete
     *
     * Like before there is suspend function
     */
    @Delete
    suspend fun delete(student: StudentEntity)

    /**
     * Retrieves a single student by their ID
     *
     * @param id The student's primary key (idStudent)
     * @return StudentEntity? - The found student or null if not found
     *
     * LIMIT 1:
     * - Ensures only one record is returned
     *
     * Return type StudentEntity? (nullable):
     * - Returns null if no student with that ID exists
     * - Allows safe handling of "not found" cases
     *
     * - This returns a single result, not a flow
     * - Use this for one-time queries
     */
    @Query("SELECT * FROM students WHERE idStudent = :id LIMIT 1")
    suspend fun getStudentById(id: Int): StudentEntity?
}