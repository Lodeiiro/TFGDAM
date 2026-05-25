package com.marcolodeiro.gamelog.data.model

import com.google.gson.annotations.SerializedName

// Modelo que representa un juego obtenido de la API de IGDB
data class Game(
    val id: Int = 0,
    val name: String = "",
    val summary: String = "",
    val cover: Cover? = null,
    val genres: List<Genre>? = null,
    val platforms: List<Platform>? = null,
    val rating: Double? = null,
    @SerializedName("first_release_date")
    val firstReleaseDate: Long? = null
)

data class Cover(
    val id: Int = 0,
    @SerializedName("image_id")
    val imageId: String = ""
)

data class Genre(
    val id: Int = 0,
    val name: String = ""
)

data class Platform(
    val id: Int = 0,
    val name: String = ""
)

// Extensión para construir la URL completa de la portada
fun Cover.getImageUrl(): String =
    "https://images.igdb.com/igdb/image/upload/t_cover_big/$imageId.jpg"