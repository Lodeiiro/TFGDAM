package com.marcolodeiro.gamelog.data.model

data class User(
    val uid: String         = "",
    val displayName: String = "",
    val email: String       = "",
    val photoUrl: String    = "",
    val createdAt: Long     = System.currentTimeMillis()
)
