package com.prateek.androidstudy.data.remote.newsApi

import com.prateek.androidstudy.data.local.newsApi.NewsEntity

data class NewsDto(
    val status: String,
    val totalResults: Long,
    val articles: List<Article>,
)

data class Article(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String,
)

fun Article.toNewsEntity(expiredOn:Long):NewsEntity{
    return NewsEntity(
        source = this.source,
        author = this.author,
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content,
        expiredOn = expiredOn
    )
}

data class Source(
    val id: String?,
    val name: String,
)

