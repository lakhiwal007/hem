package org.nha.project.core.storage

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "token")
data class TokenEntity(
    @PrimaryKey val id: Int = 0,
    val value: String,
)
