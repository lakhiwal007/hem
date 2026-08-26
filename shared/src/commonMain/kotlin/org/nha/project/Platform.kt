package org.nha.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform