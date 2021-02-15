package com.utveckla.xdays.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {
    @Query("SELECT * FROM event")
    suspend fun getAllEvents(): List<Event>

    @Insert
    suspend fun addEvent(item: Event)

    @Insert
    suspend fun addMultipleEvents(vararg event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)
}
