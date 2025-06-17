package org.weekendware.basil

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform