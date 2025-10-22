package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscribeDao {

    // Retrieves all subscribes from the database

    @Query("SELECT * FROM subscribes")
    fun getAllSubscribes(): Flow<List<SubscribeEntity>>

    /**
     * This query allows to insert a new subscribe into the database
     *
     * OnConflictStrategy.REPLACE works like in StudentDao:
     * - If it already exists → replaces the old record
     * - If it's new → inserts a new record
     *
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscribe: SubscribeEntity)


    // This allows to delete a subscribe from the database
    @Delete
    suspend fun delete(subscribe: SubscribeEntity)


    // This allows to update a subscribe by the score that a student obtained from that course
    @Update
    suspend fun update(subscribe: SubscribeEntity)


    // This query allows to return a subscribe by the studentId

    @Query("SELECT * FROM subscribes WHERE studentId = :sId")
    fun getSubscribesByStudent(sId: Int): Flow<List<SubscribeEntity>>

    // This query allows to return a subscribe by the courseId

    @Query("SELECT * FROM subscribes WHERE courseId = :cId")
    fun getSubscribesByCourse(cId: Int): Flow<List<SubscribeEntity>>


/**
 * Challenge: Gets subscriptions with student and course details (no IDs)
 *
 * This query performs a JOIN to get student names and course titles
 *
 * Returns a list of SubscribeWithDetails data class containing:
 * - idStudent
 * - idCourse
 * - score
 * - studentFirstName
 * - studentLastName
 * - courseName
 *
 * SQL JOIN:
 * - INNER JOIN students: Gets student data for each subscription
 * - INNER JOIN courses: Gets course data for each subscription
 * - ON conditions: Match IDs between tables
 *
 * @return Flow<List<SubscribeWithDetails>>: Subscriptions with full details
 */

    @Query("""
            SELECT 
                s.studentId,
                s.courseId,
                s.score,
                st.firstName AS studentFirstName,
                st.lastName AS studentLastName,
                c.nameCourse AS courseName
            FROM subscribes s
            INNER JOIN students st ON s.studentId = st.idStudent
            INNER JOIN courses c ON s.courseId = c.idCourse
            ORDER BY st.lastName, st.firstName, c.nameCourse
        """)
    fun getSubscribesWithDetails(): Flow<List<SubscribeWithDetails>>
}

/**
 * DATA CLASS - Holds subscription data with student and course details
 *
 * This is not an Entity, it's just a data holder for query results
 *
 * Room automatically maps the query result columns to these properties based on matching names
 *
 *
 * @property studentId Student ID
 * @property courseId Course ID
 * @property score Student's grade
 * @property studentFirstName Student's first name (from JOIN)
 * @property studentLastName Student's last name (from JOIN)
 * @property courseName Course name (from JOIN)
 */
data class SubscribeWithDetails(
    val studentId: Int,
    val courseId: Int,
    val score: Float,
    val studentFirstName: String,
    val studentLastName: String,
    val courseName: String
)
