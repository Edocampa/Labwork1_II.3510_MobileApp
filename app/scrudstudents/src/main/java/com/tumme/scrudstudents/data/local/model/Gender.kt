package com.tumme.scrudstudents.data.local.model

/**
 * GENDER ENUM - Represents student gender options
 *
 * This enum provides type-safe gender values for StudentEntity
 * Stored in database as TEXT using Converters.genderToString() / fromGender()
 *
 * Values:
 * - Male
 * - Female
 * - NotConcerned (other)
 */
enum class Gender(val value: String) {
    Male("Male"),
    Female("Female"),
    NotConcerned("Not concerned");

    companion object {
        /**
         * Converts a string to Gender enum
         *
         * Searches for matching Gender by value property
         * Returns NotConcerned if no match found
         *
         * @param value String to convert (case-sensitive)
         * @return Matching Gender enum or NotConcerned as fallback
         *
         */
        fun from(value: String) = Gender.entries.firstOrNull { it.value == value } ?: NotConcerned
    }
}