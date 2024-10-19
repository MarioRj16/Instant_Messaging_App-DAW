package com.example.messagingapp

import kotlin.math.abs
import kotlin.random.Random

fun generateRandomEmail() = "user-${abs(Random.nextLong())}@example.com"

fun generateRandomString() = "user-${abs(Random.nextLong())}"

fun generateRandomMessage() = "HELLO WORLD - ${abs(Random.nextLong())}"
