package com.prateek.stockTradingApp.presentation.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prateek.stockTradingApp.data.remote.StockEntity

@Composable
fun StockList(
    stocks: List<StockEntity>,
    onStockClick: (StockEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(stocks) { stock ->
            StockCard(
                stock = stock,
                onClick = { onStockClick(stock) }
            )
        }
    }
}

@Composable
fun StockCard(
    modifier: Modifier = Modifier,
    stock: StockEntity,
    onClick: () -> Unit
) {
    val isPositive = !stock.changePercent.startsWith("-")
    val priceColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stock.currentPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = priceColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.changePercent,
                    style = MaterialTheme.typography.bodySmall,
                    color = priceColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
