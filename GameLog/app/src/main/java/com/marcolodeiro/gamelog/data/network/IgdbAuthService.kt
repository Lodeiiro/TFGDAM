package com.marcolodeiro.gamelog.data.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// Respuesta del token de autenticación de Twitch
data class TwitchTokenResponse(
    val access_token: String,  // Token para usar en las llamadas a IGDB
    val expires_in: Int,
    val token_type: String
)

// Interfaz para obtener el token OAuth de Twitch
interface IgdbAuthService {
    @FormUrlEncoded
    @POST("token")
    suspend fun getToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): TwitchTokenResponse
}