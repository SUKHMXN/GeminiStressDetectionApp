package com.varunkumar.geminiapi.model.database.entities

import android.health.connect.datatypes.units.Temperature
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_sense")
data class UserSense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,
    val heartRate: Float?,
    val temperature: Float?,
    val bloodOxygen: Float?,
    val respirationRate: Float?,
    val hoursOfSleep: Float?,
    val stressLevel: Int?
)
