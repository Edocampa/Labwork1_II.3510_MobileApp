package com.tumme.scrudstudents.data.local.model

/**
 * LEVEL COURSE ENUM - Represents academic course levels
 *
 * This enum provides type-safe course level values for CourseEntity
 * Stored in database as TEXT using Converters.levelToString() / fromLevel()
 *
 * Used in CourseFormScreen dropdown for level selection.
 */
enum class LevelCourse(val value: String) {
    P1("P1"), P2("P2"), P3("P3"),
    B1("B1"), B2("B2"), B3("B3"),
    A1("A1"), A2("A2"), A3("A3"),
    MS("MS"), PhD("PhD");

    companion object {
        /**
         * Converts a string to LevelCourse enum
         *
         * Searches for matching LevelCourse by value property
         * Returns P1 if no match found (default: lowest level)
         *
         * @param value String to convert (case-sensitive)
         * @return Matching LevelCourse enum or P1 as fallback
         *
         */
        fun from(value: String) = LevelCourse.entries.firstOrNull { it.value == value } ?: P1
    }
}