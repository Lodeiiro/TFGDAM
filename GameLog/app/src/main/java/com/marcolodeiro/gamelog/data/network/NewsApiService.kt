package com.marcolodeiro.gamelog.data.network

import com.marcolodeiro.gamelog.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

// Interfaz para la API de noticias
interface NewsApiService {

    @GET("v2/everything")
    suspend fun getGamingNews(
        @Query("q") query: String = "videojuegos OR gaming OR PlayStation OR Xbox OR Nintendo",
        @Query("language") language: String = "es",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): NewsResponse
}