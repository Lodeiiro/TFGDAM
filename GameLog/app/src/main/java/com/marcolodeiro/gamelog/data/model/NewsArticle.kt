package com.marcolodeiro.gamelog.data.model

// Modelo de artículo de noticias
data class NewsArticle(
    val title: String = "",
    val description: String = "",
    val url: String = "",
    val urlToImage: String = "",
    val publishedAt: String = "",
    val source: NewsSource = NewsSource()
)

data class NewsSource(
    val name: String = ""
)

// Respuesta de la API de noticias
data class NewsResponse(
    val articles: List<NewsArticle> = emptyList(),
    val status: String = "",
    val totalResults: Int = 0
)