package com.prateek.androidstudy.presentation.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.prateek.androidstudy.data.remote.newsApi.Article
import com.prateek.androidstudy.data.remote.newsApi.Source

@Composable
fun NewsListScreen(
    articles: List<Article>,
    modifier: Modifier = Modifier,
    onArticleClick: (Article) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles) { article ->
            ArticleCard(
                article = article,
                onClick = { onArticleClick(article) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(
    article: Article,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Article Image
            article.urlToImage?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Article image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Article Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Article Description
            article.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Source and Author Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source
                Text(
                    text = article.source.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                // Author
                article.author?.let { author ->
                    Text(
                        text = "By $author",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Published Date
            Text(
                text = formatPublishedDate(article.publishedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to format the published date
private fun formatPublishedDate(publishedAt: String): String {
    return try {
        // You can implement proper date formatting here
        // For now, just showing the raw date
        publishedAt.substringBefore('T')
    } catch (e: Exception) {
        publishedAt
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleCardPreview() {
    MaterialTheme {
        ArticleCard(
            article = Article(
                source = Source(id = "1", name = "TechCrunch"),
                author = "John Doe",
                title = "Breaking: New Android Development Framework Released",
                description = "A revolutionary new framework for Android development has been announced, promising to streamline the development process and improve app performance significantly.",
                url = "https://example.com",
                urlToImage = "https://example.com/image.jpg",
                publishedAt = "2024-01-15T10:30:00Z",
                content = "Full article content here..."
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewsListScreenPreview() {
    MaterialTheme {
        NewsListScreen(
            articles = listOf(
                Article(
                    source = Source(id = "1", name = "TechCrunch"),
                    author = "John Doe",
                    title = "Breaking: New Android Development Framework Released",
                    description = "A revolutionary new framework for Android development has been announced.",
                    url = "https://example.com",
                    urlToImage = "https://example.com/image.jpg",
                    publishedAt = "2024-01-15T10:30:00Z",
                    content = "Full article content here..."
                ),
                Article(
                    source = Source(id = "2", name = "Android Authority"),
                    author = "Jane Smith",
                    title = "Top 10 Android Apps of 2024",
                    description = "Discover the most innovative and useful Android apps released this year.",
                    url = "https://example.com",
                    urlToImage = null,
                    publishedAt = "2024-01-14T15:45:00Z",
                    content = "Full article content here..."
                )
            )
        )
    }
}