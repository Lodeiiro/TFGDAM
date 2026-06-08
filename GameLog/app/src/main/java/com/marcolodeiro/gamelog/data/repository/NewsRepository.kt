package com.marcolodeiro.gamelog.data.repository

import com.marcolodeiro.gamelog.BuildConfig
import com.marcolodeiro.gamelog.data.model.NewsArticle
import com.marcolodeiro.gamelog.data.network.NewsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsApiService: NewsApiService
) {
    // Obtiene noticias del mundo gamer
    suspend fun getGamingNews(): List<NewsArticle> {
        return try {
            val response = newsApiService.getGamingNews(
                apiKey = BuildConfig.NEWS_API_KEY
            )
            response.articles.filter {
                it.title.isNotBlank() && it.title != "[Removed]"
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}