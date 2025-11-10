package com.tumme.scrudstudents.data.local.dao

import androidx.room.*
import com.tumme.scrudstudents.data.local.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /**
     * Register new user
     * If email already exists --> replace
     */


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    /**
     * Find user by email (for login check)
     * Returns null if not found
     */


    @Query("SElECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    /**
     * Login: Find user by email and password
     * Returns null if credentials invalid
     */

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    /**
     * Get user by ID
     */

    @Query("SELECT * FROM users WHERE idUser = :userId")
    suspend fun getUserById(userId: Int): User?

    /**
     * Get user by email
     */

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Get all users
     */

    @Query("SELECT * from users")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Delete user
     */

    @Query("DELETE FROM users WHERE idUser = :userId")
    suspend fun deleteUser(userId: Int)


    /**
     *
     * Get all user, only for admin
     */
    @Query("SELECT * FROM users ORDER BY role, email")
    fun getAllUsersAdmin(): Flow<List<User>>


    @Query("DELETE FROM users WHERE idUser = :userId")
    suspend fun deleteUserById(userId: Int)

    // Count users by role
    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun countUsersByRole(role: String): Int


}