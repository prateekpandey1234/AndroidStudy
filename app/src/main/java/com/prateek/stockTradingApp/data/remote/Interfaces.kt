package com.prateek.stockTradingApp.data.remote

import com.prateek.androidstudy.data.remote.websocket.ConnectionState
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET

interface PortfolioInterFace {

    @GET("/user/portfolio")
    suspend fun getPortfolioData(): Response<PortfolioSummary>

}


interface StockWSSInterface{
    fun connect(url:String)
    fun disconnect()
    fun observeConnectionState(): Flow<ConnectionState>
    fun observeMessages(): Flow<String>

}





