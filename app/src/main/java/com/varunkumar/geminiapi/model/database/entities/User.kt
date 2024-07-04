package com.varunkumar.geminiapi.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val username: String?,
    val profilePictureUrl: String?,
    val sex: String?,
    val weight: Float?,
    val height: Float?,
    val age: Int?
)
