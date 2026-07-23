package dev.cadence

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform