package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.CourseEntity
import kotlinx.coroutines.flow.Flow
import com.tumme.scrudstudents.data.local.model.CourseWithTeacher

@Dao
interface CourseDao {

    /**
    * Retrieves all courses from the database, ordered by name
    *
    * @return Flow<List<CourseEntity>> - Reactive stream of the course list
    *
    * Flow:
    * - Emits a new list whenever the database changes
    * - The UI observes this Flow and updates automatically
    * - Still active when there are observers
    */

    @Query("SELECT * FROM courses ORDER BY nameCourse")
    fun getAllCourses(): Flow<List<CourseEntity>>

    /**
     * This allows to insert a new course into the database
     *
     * @param course object CourseEntity to insert
     *
     * * OnConflictStrategy.REPLACE works like in the StudentDao:
     *   - If courseId already exists → replaces the old record
     *   - If courseId is new → inserts a new record
      */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity)

    /**
     * Delete a specific course from the database
     *
     * @param course CourseEntity object to delete
     *
     * Room uses the primary key (courseId) to identify which record to delete
     *
     */

    @Delete
    suspend fun delete(course: CourseEntity)


    /**
     * Retrieve a single course throught Id
     *
     * @param id Primary key of the course (courseId)
     * @return CourseEntity? - Found course or null if it doesn't exist
     *
     * LIMIT 1:
     * only one result is expected, null if that courseID doesn't exist
     */

    @Query("SELECT * FROM courses WHERE idCourse = :id LIMIT 1")
    suspend fun getCourseById(id: Int): CourseEntity?

    /**
     * Get courses filtered by level with teacher info
     */
    @Transaction
    @Query("SELECT * FROM courses WHERE levelCourse = :level ORDER BY nameCourse ASC")
    suspend fun getCoursesByLevelWithTeachers(level: String): List<CourseWithTeacher>

    /**
     * Get all courses with their teacher information
     */
    @Transaction
    @Query("SELECT * FROM courses ORDER BY nameCourse ASC")
    suspend fun getCoursesWithTeachers(): List<CourseWithTeacher>

    /**
     * Get course with teacher by ID
     */
    @Transaction
    @Query("SELECT * FROM courses WHERE idCourse = :courseId")
    suspend fun getCourseWithTeacher(courseId: Int): CourseWithTeacher?
}

