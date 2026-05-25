package com.marcolodeiro.gamelog.data.repository

import com.marcolodeiro.gamelog.BuildConfig
import com.marcolodeiro.gamelog.data.model.Game
import com.marcolodeiro.gamelog.data.network.IgdbApiService
import com.marcolodeiro.gamelog.data.network.IgdbAuthService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IgdbRepository @Inject constructor(
    private val authService: IgdbAuthService,
    private val apiService: IgdbApiService
) {
    private val clientId = BuildConfig.IGDB_CLIENT_ID
    private val clientSecret = BuildConfig.IGDB_CLIENT_SECRET
    private var cachedToken: String? = null

    // Tipo de contenido que acepta IGDB
    private val mediaType = "text/plain".toMediaType()

    private suspend fun getToken(): String {
        if (cachedToken != null) return cachedToken!!
        val response = authService.getToken(clientId, clientSecret)
        cachedToken = response.access_token
        return cachedToken!!
    }

    // Convierte el string de query a RequestBody
    private fun buildQuery(query: String) = query.toRequestBody(mediaType)

    suspend fun getPopularGames(): List<Game> {
        val token = getToken()
        val result = apiService.getGames(
            clientId = clientId,
            authorization = "Bearer $token",
            query = buildQuery("fields name,summary,cover.image_id,genres.name,platforms.name,rating,first_release_date; where rating > 80 & rating_count > 100 & cover.image_id != null; sort rating desc; limit 20;")
        )

        return result
    }

    suspend fun searchGames(query: String): List<Game> {
        val token = getToken()
        return apiService.getGames(
            clientId = clientId,
            authorization = "Bearer $token",
            query = buildQuery("search \"$query\"; fields name,summary,cover.image_id,genres.name,platforms.name,rating,first_release_date; limit 100;")
        )
    }
}