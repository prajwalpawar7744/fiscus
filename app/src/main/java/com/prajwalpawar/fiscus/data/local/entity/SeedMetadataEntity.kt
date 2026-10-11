package com.prajwalpawar.fiscus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seed_metadata")
data class SeedMetadataEntity (
    @PrimaryKey
    val key: String,
    val version: Int
)