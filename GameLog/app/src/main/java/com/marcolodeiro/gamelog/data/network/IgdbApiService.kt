package com.marcolodeiro.gamelog.data.network

import com.marcolodeiro.gamelog.data.model.Game
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// Interfaz principal para llamadas a la API de IGDB
interface IgdbApiService {

    @POST("games")
    suspend fun getGames(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") authorization: String,
        @Body query: RequestBody
    ): List<Game>
}
