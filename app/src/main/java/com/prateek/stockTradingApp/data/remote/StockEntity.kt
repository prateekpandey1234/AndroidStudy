package com.prateek.stockTradingApp.data.remote

data class StockEntity (
    val id:Long,
    val name:String,
    val symbol:String,
    val changePercent:String,
    val currentPrice:String,
    val highPrice:String
)

data class PortfolioStock(
    val stock: StockEntity,
    val quantity:Int,
    val averagePrice:String
)


data class PortfolioSummary(
    val totalValue:String,
    val totalChange:Map<String,String>,// time_span -> value
    val totalChangePercent:Map<String,String>,
    val bestStock:PortfolioStock?=null,
    val worstStock:PortfolioStock?=null
)


data class OrderResponse(
    val data:List<StockOrder>,
)

data class StockOrder(
    val stock: StockEntity,
    val quantity: Long,
    val createdOn:String,
    val price:String,
    val status: String
)

enum class OrderStatus(val status:String){
    PENDING("pending"),
    CONFIRMED("confirmed"),
    REJECTED("rejected"),
    CANCELLED("cancelled");

    companion object {
        fun fromString(str: String): OrderStatus {
            return entries.find { it.status == str } ?: PENDING
    }
}

data class StockWSSResponse(
    val action:String,
    val data: String
)

// if action price changes -> List<StockEntity>
// portfolio data -> PortfolioSummary
// order status -> StockOrder


enum class StockActions(val action:String){
    PRICE_UPDATE("price_change"),
    ORDER_STATUS("order_status"),
    PORTFOLIO_DATA("portfolio_data"),
    UNKNOWN("not_found");

    companion object{
        fun fromString( str:String):StockActions{
            return entries.find { it.action == str } ?: UNKNOWN }
        }
    }
}







