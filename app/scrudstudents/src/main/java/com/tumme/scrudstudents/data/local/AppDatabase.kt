package com.tumme.scrudstudents.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tumme.scrudstudents.data.local.dao.CourseDao
import com.tumme.scrudstudents.data.local.dao.StudentDao
import com.tumme.scrudstudents.data.local.dao.SubscribeDao
import com.tumme.scrudstudents.data.local.dao.UserDao
import com.tumme.scrudstudents.data.local.model.StudentEntity
import com.tumme.scrudstudents.data.local.model.CourseEntity
import com.tumme.scrudstudents.data.local.model.SubscribeEntity
import com.tumme.scrudstudents.data.local.model.TeacherEntity
import com.tumme.scrudstudents.data.local.model.User
import com.tumme.scrudstudents.data.local.dao.TeacherDao

/**
 * APP DATABASE - Room database configuration for the entire application
 *
 * This class serves as the main database holder for the app
 *
 * DATABASE ARCHITECTURE
 *
 * Room Database Structure:
 * - Provides an abstraction layer over SQLite
 * - Handles database creation, versioning and migrations
 * - Ensures compile-time verification of SQL queries
 * - Manages database connections and thread safety
 *
 * ANNOTATION EXPLANATION
 *
 * @Database:
 * - Marks this class as a Room database
 * - entities: List of all Entity classes (database tables)
 * - version: Database schema version (increment when schema changes)
 * - exportSchema: If true, exports database schema to a folder
 *
 * @TypeConverters:
 * - Registers custom type converters for the entire database
 * - Allows Room to convert custom types (Date, Enum) to/from SQLite types
 *
 * ENTITIES (TABLES)
 *
 * The database contains three entities representing three tables:
 *
 * 1. StudentEntity → students table
 *    - Stores student information (name, Date of birth, gender)
 *    - Primary key: idStudent
 *
 * 2. CourseEntity → courses table
 *    - Stores course information (name, ECTS, level)
 *    - Primary key: idCourse
 *
 * 3. SubscribeEntity → subscribes table
 *    - Join table implementing many-to-many relationship
 *    - Links students to courses with a score
 *    - Composite primary key: (idStudent, idCourse)
 *    - Foreign keys with CASCADE delete
 *
 * DAO PROVIDERS
 *
 * Abstract methods that Room implements to provide DAO instances:
 * - studentDao(): Access to Student table operations
 * - courseDao(): Access to Course table operations
 * - subscribeDao(): Access to Subscribe table operations
 *
 * These DAOs are injected by Hilt into Repository classes
 *
 * TYPE CONVERTERS
 *
 * The Converters class handles custom type conversions:
 *
 * Date Conversion:
 * - dateToTimestamp(Date): Converts Date to Long (milliseconds since epoch)
 * - fromTimestamp(Long): Converts Long back to Date
 * - SQLite stores dates as INTEGER
 *
 * Gender Enum Conversion:
 * - genderToString(Gender): Converts Gender enum to String
 * - fromString(String): Converts String back to Gender enum
 * - SQLite stores enums as TEXT
 *
 * Why Type Converters are Needed:
 * - SQLite only supports: NULL, INTEGER, REAL, TEXT, BLOB
 * - Custom types (Date, Enum) must be converted to supported types
 * - Room handles conversion automatically using @TypeConverters
 *
 * DEPENDENCY INJECTION
 *
 * This database is provided by Hilt in AppModule:
 * 1. Room.databaseBuilder() creates the database instance
 * 2. Hilt provides it as a @Singleton (only one instance exists)
 * 3. DAOs are extracted from the database and also provided by Hilt
 * 4. Repository receives DAOs through constructor injection
 * 5. ViewModels receive Repository through constructor injection
 *
 */
@Database(
    entities = [
        StudentEntity::class,
        CourseEntity::class,
        SubscribeEntity::class,
        User:: class,
        TeacherEntity:: class
    ],
    version = 4,
    exportSchema = false         // false means --> Don't export schema JSON
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {


     // Provides access to Student table operations

    abstract fun studentDao(): StudentDao


     // Provides access to Course table operations

    abstract fun courseDao(): CourseDao


     // Provides access to Subscribe table operations
    abstract fun subscribeDao(): SubscribeDao

    // Provides access to User table operations

    abstract fun userDao(): UserDao

    // Provides access to Teacher table operations

    abstract fun teacherDao(): TeacherDao
}
