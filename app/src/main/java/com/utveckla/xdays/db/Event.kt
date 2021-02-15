package com.utveckla.xdays.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Event (
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    @ColumnInfo var name: String = "",
    @ColumnInfo var date: String = ""
)
