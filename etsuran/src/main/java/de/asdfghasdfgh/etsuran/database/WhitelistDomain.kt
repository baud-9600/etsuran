package de.asdfghasdfgh.etsuran.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "WHITELIST")
data class WhitelistDomain(
    @PrimaryKey
    val DOMAIN: String,
)
