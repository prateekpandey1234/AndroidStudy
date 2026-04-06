package com.prateek.stockTradingApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.prateek.androidstudy.ui.theme.AndroidStudyTheme
import com.prateek.stockTradingApp.data.remote.StockEntity
import com.prateek.stockTradingApp.presentation.ui.compose.StockList

class DashBoardPortfolioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mock data for demonstration
        val mockStocks = listOf(
            StockEntity(1, "Apple Inc.", "AAPL", "+1.2%", "$150.00", "$152.00"),
            StockEntity(2, "Tesla Inc.", "TSLA", "-2.5%", "$700.00", "$720.00"),
            StockEntity(3, "Google LLC", "GOOGL", "+0.5%", "$2800.00", "$2810.00"),
            StockEntity(4, "Microsoft Corp.", "MSFT", "+1.8%", "$300.00", "$305.00"),
            StockEntity(5, "Amazon.com Inc.", "AMZN", "-1.2%", "$3300.00", "$3350.00")
        )

        setContent {
            AndroidStudyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold { innerPadding ->
                        StockList(
                            stocks = mockStocks,
                            onStockClick = { stock ->
                                // Handle stock click
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}


