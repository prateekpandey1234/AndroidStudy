package com.prateek.androidstudy.presentation.news

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.prateek.androidstudy.data.remote.newsApi.Article
import com.prateek.androidstudy.data.remote.newsApi.Source
import com.prateek.androidstudy.utils.ListPagination
import com.prateek.androidstudy.viewmodel.News.NewsListUi

@Composable
fun NewsListScreen(
    state: NewsListUi,
    modifier: Modifier = Modifier,
    onArticleClick: (Article) -> Unit = {},
    paginateNews:()->Unit={}
) {
    val listState = rememberLazyListState()
    val shouldPaginate = remember {
        derivedStateOf{
            state.canPaginate && (listState.firstVisibleItemIndex) >= (listState.layoutInfo.totalItemsCount - 6)
        }
    }

    LaunchedEffect(key1 = shouldPaginate.value) {
        Log.d("NewsListScreen", "shouldPaginate: ${shouldPaginate.value} +${state.canPaginate}+ ${state.state}")
        if (shouldPaginate.value && state.state== ListPagination.IDLE) paginateNews()
    }
    LazyColumn(state=listState,
        modifier = modifier.fillMaxSize().testTag("home_Screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.newsList) { article ->
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
            .testTag("news_item")
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
    }
}