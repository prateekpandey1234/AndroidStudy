package com.prateek.androidstudy.data.local.newsApi

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prateek.androidstudy.data.remote.newsApi.Article
import com.prateek.androidstudy.data.remote.newsApi.NewsDto
import com.prateek.androidstudy.data.remote.newsApi.Source


@Entity(tableName = "news_db")
data class NewsEntity(
    val source: Source,
    val author: String?,
    @PrimaryKey
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String,
    val expiredOn:Long
)

fun NewsEntity.toArticle():Article{
    return Article(
        source = this.source,
        author = this.author,
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        content = this.content,
    )
}