package com.mindset

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
