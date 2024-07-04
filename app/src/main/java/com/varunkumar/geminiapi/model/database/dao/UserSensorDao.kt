package com.varunkumar.geminiapi.model.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.varunkumar.geminiapi.model.database.entities.User
import retrofit2.http.GET

@Dao
interface UserDao {
    @GET
    suspend fun getUser(uid: String): User?

    @Upsert
    suspend fun upsertUser(user: User)
}

@Dao
interface UserSenseDao {

}