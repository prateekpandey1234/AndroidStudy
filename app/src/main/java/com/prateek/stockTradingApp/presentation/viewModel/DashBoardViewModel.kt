package com.prateek.stockTradingApp.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.prateek.stockTradingApp.data.remote.PortfolioStock
import com.prateek.stockTradingApp.data.remote.StockOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel
class DashBoardViewModel : ViewModel() {
    private var _uiState = MutableStateFlow<DashBoardUIState>(DashBoardUIState("",""))
    val uiState: StateFlow<DashBoardUIState> = _uiState

    init{

    }

    fun getDashBoardData(){

    }

}


data class DashBoardUIState(
    var totalValue:String,
    var totalChange:String,
    var topStock: PortfolioStock?=null,
    var bottomStock: PortfolioStock?=null,
    var recentOrders:List<StockOrder>?=null
)

