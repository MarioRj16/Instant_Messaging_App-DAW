package com.example.messagingapp.domain

/*
 Password was initially implemented as a value class, but due to the way Kotlin value classes work,
 they are not compatible with certain reflection-based frameworks like Jdbi. Value classes are compiled
 down to their underlying types, causing issues with frameworks expecting an actual class instance.
 By switching to a data class, we ensure that Jdbi can handle the mapping of the Password object correctly
 without runtime errors related to reflection.
 */
data class Password(
    val value: String,
)
