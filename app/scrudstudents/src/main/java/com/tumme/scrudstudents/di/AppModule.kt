package com.tumme.scrudstudents.di

import android.content.Context
import androidx.room.Room
import com.tumme.scrudstudents.data.local.AppDatabase
import com.tumme.scrudstudents.data.local.dao.CourseDao
import com.tumme.scrudstudents.data.local.dao.StudentDao
import com.tumme.scrudstudents.data.local.dao.SubscribeDao
import com.tumme.scrudstudents.data.repository.SCRUDRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tumme.scrudstudents.data.local.dao.UserDao
import com.tumme.scrudstudents.data.local.dao.TeacherDao
import com.tumme.scrudstudents.data.repository.AuthRepository
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * HILT MODULE - Dependency Injection configuration for the entire app
 *
 * This module tells Hilt how to create and provide dependencies throughout the app
 *
 *
 * How Hilt Uses This Module:
 * 1. App starts → Hilt scans all @Module classes
 * 2. ViewModel needs Repository → Hilt calls provideRepository()
 * 3. Repository needs DAOs → Hilt calls all the DAOs
 * 4. DAOs need Database → Hilt calls provideDatabase()
 * 5. Database needs Context → Hilt provides @ApplicationContext automatically
 * 6. Hilt chains these calls automatically based on dependencies
 */
@Module  // Marks this object as a Hilt module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room Database instance
     *
     * @param context Application context injected by Hilt
     * @return AppDatabase - The Room database for the entire app
     *
     *
     * @Provides: Tells Hilt this method provides a dependency
     * @Singleton: Only ONE database instance exists for the entire app
     *
     * @ApplicationContext:
     *   - Qualifier annotation that tells Hilt to inject Application context
     *
     * Room.databaseBuilder() parameters:
     *   - context: Android context for accessing device storage
     *   - AppDatabase::class.java: Database class to instantiate
     *   - "scrud-db": Database file name on device storage
     *   - .build(): Creates the database instance
     *
     * Database behavior:
     *   - If database doesn't exist → creates new database file
     *   - If database exists → opens existing database
     *
     * Lifecycle:
     *   - Created once when first requested
     *   - Kept in memory until app process dies
     *   - Automatically closed when app is destroyed
     *
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "scrud-db"
        )
            .fallbackToDestructiveMigration(true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("""
            INSERT INTO teachers (idTeacher, userId, firstName, lastName) VALUES
            (1, 0, 'John', 'Smith'),
            (2, 0, 'Maria', 'Garcia'),
            (3, 0, 'Robert', 'Johnson')
        """)

                    db.execSQL("""
            INSERT INTO courses (idCourse, nameCourse, ectsCourse, teacherId, levelCourse) VALUES
            (1, 'English Beginner Grammar', 6.0, 1, 'B1'),
            (2, 'English Intermediate Conversation', 9.0, 1, 'B2'),
            (3, 'English Advanced Literature', 12.0, 1, 'B3'),
            (4, 'Spanish Beginner', 6.0, 2, 'B1'),
            (5, 'Spanish Intermediate', 9.0, 2, 'B2'),
            (6, 'French Advanced', 12.0, 3, 'A2')
        """)
                }
            })
            .build()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideTeacherDao(database: AppDatabase): TeacherDao {
        return database.teacherDao()
    }

    /**
     * Provides StudentDao for accessing Student table
     *
     *
     * @param db AppDatabase instance (automatically injected by Hilt)
     * @return StudentDao - Interface for Student CRUD operations
     *
     * How it works:
     *   - Hilt sees this method returns StudentDao
     *   - When StudentDao is needed
     *   - Hilt calls this method
     *   - Hilt automatically provides 'db' parameter
     *   - Method calls db.studentDao() to get the DAO
     *
     * Dependency chain:
     *   ViewModel → Repository → StudentDao → Database
     *
     */
    @Provides
    fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()

    /**
     * Provides CourseDao instance
     *
     * @param db AppDatabase instance (injected by Hilt)
     * @return CourseDao - Interface for Course CRUD operations
     *
     * Extracts CourseDao from the database and makes it available
     * for dependency injection into SCRUDRepository
     *
     *
     * Used by SCRUDRepository to access courses table
     *
     */
    @Provides
    fun provideCourseDao(db: AppDatabase): CourseDao = db.courseDao()

    /**
     * Provides SubscribeDao.kt instance
     *
     * @param db AppDatabase instance (injected by Hilt)
     * @return SubscribeDao.kt - Interface for Subscribe CRUD operations
     *
     * Extracts SubscribeDao.kt from the database and makes it available
     * for dependency injection into SCRUDRepository
     *
     *
     * Used by SCRUDRepository to access subscribes table
     *
     */
    @Provides
    fun provideSubscribeDao(db: AppDatabase): SubscribeDao = db.subscribeDao()


    /**
     * Provides the unified Repository for all entities
     *
     * @param studentDao StudentDao instance (injected by Hilt)
     * @param courseDao CourseDao instance (injected by Hilt)
     * @param subscribeDao SubscribeDao.kt instance (injected by Hilt)
     * @return SCRUDRepository - Repository for all CRUD operations
     *
     *
     * Dependency Injection Magic:
     *   - This method needs 3 DAOs as parameters
     *   - Hilt automatically provides them by calling:
     *     1. provideStudentDao(db)
     *     2. provideCourseDao(db)
     *     3. provideSubscribeDao(db)
     *
     */
    @Provides
    @Singleton
    fun provideRepository(
        studentDao: StudentDao,
        courseDao: CourseDao,
        subscribeDao: SubscribeDao,
        userDao: UserDao,
        teacherDao: TeacherDao
    ): SCRUDRepository =
        SCRUDRepository(studentDao, courseDao, subscribeDao, userDao, teacherDao)


    /**
     * Provides AuthRepository singleton for dependency injection
     *
     * AuthRepository is responsible for:
     * - Managing authentication state
     * - Login and registration logic
     * - Delegating DAO operations to SCRUDRepository
     *
     * @param scrudRepository Repository for database access
     * @return Singleton instance of AuthRepository
     */

    @Provides
    @Singleton
    fun provideAuthRepository(
        scrudRepository: SCRUDRepository
    ): AuthRepository {
        return AuthRepository(scrudRepository)
    }
}



