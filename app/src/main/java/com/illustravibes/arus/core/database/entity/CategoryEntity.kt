package com.illustravibes.arus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val icon: String = "fa_tags",
    val colorHex: String = "#00A86B",
    val type: String, // "INCOME" or "EXPENSE"
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
