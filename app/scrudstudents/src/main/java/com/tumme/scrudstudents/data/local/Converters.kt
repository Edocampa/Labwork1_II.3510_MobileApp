package com.tumme.scrudstudents.data.local

import androidx.room.TypeConverter
import com.tumme.scrudstudents.data.local.model.Gender
import com.tumme.scrudstudents.data.local.model.LevelCourse
import java.util.Date
import com.tumme.scrudstudents.data.local.model.UserRole

/**
 * TYPE CONVERTERS - Custom type conversion for Room database
 *
 * This class provides methods to convert custom Kotlin types to SQLite-compatible types (and viceversa)
 * Room uses these converters automatically when storing and retrieving data
 *
 *
 * HOW TYPE CONVERTERS WORK
 *
 * Registration:
 * - Declared in AppDatabase with @TypeConverters(Converters::class)
 * - Applied to all entities and DAOs in the database
 *
 * Automatic Usage:
 * - When inserting: Room calls dateToTimestamp(Date) → stores Long
 * - When querying: Room calls fromTimestamp(Long) → returns Date
 * - There is no need of manual calls
 *
 * Direction:
 * - "To" methods: Kotlin type → SQLite type (for storage)
 * - "From" methods: SQLite type → Kotlin type (for retrieval)
 *
 * NULLABILITY HANDLING
 *
 * All converters support nullable types:
 * - If input is null → returns null (no conversion needed)
 * - If input has value → performs conversion
 *
 * - There is need to manage this aspect because database columns can be NULL
 *
 */
class Converters {

    // DATE CONVERTERS

    /**
     * Converts a timestamp (Long) to a Date object
     *
     * Used by Room when reading from the database
     *
     * Process:
     * 1. Receives Long? value from SQLite INTEGER column
     * 2. If value is null → returns null
     * 3. If value exists → creates Date object from timestamp
     *
     *  @param value Timestamp in milliseconds (from SQLite INTEGER column)
     *  @return Date object representing the timestamp or null if input was null
     *
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    /**
     * Converts a Date object to a timestamp (Long)
     *
     * Used by Room when writing to the database
     *
     * Process:
     * 1. Receives Date? from Kotlin code
     * 2. If date is null → returns null (stores NULL in database)
     * 3. If date exists → extracts timestamp in milliseconds
     *
     *
     * @param date Date object to convert (from Kotlin code)
     * @return Timestamp in milliseconds, or null if input was null
     *
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // GENDER ENUM CONVERTERS

    /**
     * Converts a String to a Gender enum
     *
     * Used by Room when reading from the database
     *
     * Process:
     * 1. Receives String? value from SQLite TEXT column
     * 2. If value is null → returns null
     * 3. If value exists → calls Gender.from(String) to parse enum
     *
     * @param value String representation of gender (from SQLite TEXT column)
     * @return Gender enum value, or null if input was null
     * @see Gender.from for string-to-enum conversion logic
     *
     */
    @TypeConverter
    fun fromGender(value: String?): Gender? = value?.let { Gender.from(it) }

    /**
     * Converts a Gender enum to a String
     *
     * Used by Room when writing to the database
     *
     * Process:
     * 1. Receives Gender? from Kotlin code
     * 2. If gender is null → returns null (stores NULL in database)
     * 3. If gender exists → extracts its string value
     *
     * @param gender Gender enum to convert (from Kotlin code)
     * @return String representation of gender, or null if input was null
     *
     */
    @TypeConverter
    fun genderToString(gender: Gender?): String? = gender?.value

    // LEVEL COURSE ENUM CONVERTERS

    /**
     * Converts a String to a LevelCourse enum
     *
     * Used by Room when reading from the database
     *
     * Process:
     * 1. Receives String? value from SQLite TEXT column
     * 2. If value is null → returns null
     * 3. If value exists → calls LevelCourse.from(String) to parse enum
     *
     * @param value String representation of course level (from SQLite TEXT column)
     * @return LevelCourse enum value, or null if input was null
     * @see LevelCourse.from for string-to-enum conversion logic
     * @see CourseEntity which uses LevelCourse type
     *
     */
    @TypeConverter
    fun fromLevel(value: String?): LevelCourse? = value?.let { LevelCourse.from(it) }

    /**
     * Converts a LevelCourse enum to a String
     *
     * Used by Room when writing to the database
     *
     * Process:
     * 1. Receives LevelCourse? from Kotlin code
     * 2. If level is null → returns null (stores NULL in database)
     * 3. If level exists → extracts its string value
     *
     * @param level LevelCourse enum to convert (from Kotlin code)
     * @return String representation of level, or null if input was null
     *
     */
    @TypeConverter
    fun levelToString(level: LevelCourse?): String? = level?.value
}

@TypeConverter
fun fromUserRole(role: UserRole): String{
    return role.name
}

@TypeConverter
fun toUserRole(value:String): UserRole{
    return UserRole.valueOf(value)
}
