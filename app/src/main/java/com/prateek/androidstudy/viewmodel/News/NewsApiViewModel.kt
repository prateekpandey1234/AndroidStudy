package com.prateek.androidstudy.viewmodel.News

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.prateek.androidstudy.data.local.newsApi.NewsDAO
import com.prateek.androidstudy.data.local.newsApi.NewsDb
import com.prateek.androidstudy.data.local.newsApi.NewsEntity
import com.prateek.androidstudy.data.remote.newsApi.Article
import com.prateek.androidstudy.data.remote.newsApi.NewsApiService
import com.prateek.androidstudy.data.remote.newsApi.NewsDto
import com.prateek.androidstudy.data.remote.newsApi.toNewsEntity
import com.prateek.androidstudy.utils.ListPagination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NewsApiViewModel @Inject constructor(private val newsDb: NewsDb,private val newsApiService: NewsApiService):ViewModel(){
//    val paginatedList = mutableStateListOf<List<Article>>()
//
//    var currPage by mutableStateOf(1)
//    // to check for errors in if further pagination can be made or there is some sort of error
//    var canPaginate by mutableStateOf(false)
//    var listState by mutableStateOf(ListPagination.IDLE)

    var _uiState = MutableStateFlow<NewsListUi>(NewsListUi(0,  mutableListOf(),ListPagination.IDLE,true))
    val uiState:StateFlow<NewsListUi> = _uiState

    init{

    }
    fun getNews() = viewModelScope.launch {
        _uiState.value = NewsListUi(0, mutableListOf(),ListPagination.IDLE,true)
        if(_uiState.value.currPage==1 || (_uiState.value.currPage!=1 && _uiState.value.canPaginate) && _uiState.value.state == ListPagination.IDLE){
            _uiState.value= _uiState.value.copy(state = if(_uiState.value.currPage==1)ListPagination.LOADING else ListPagination.PAGINATING)


            val response = newsApiService.getNews("football",_uiState.value.currPage,20)
            if(response.status.equals("ok",true)){
                _uiState.value= _uiState.value.copy(canPaginate = response.articles.size == 20)

                if (_uiState.value.currPage == 1) {
                    _uiState.value = _uiState.value.copy(newsList = mutableListOf())
                    _uiState.value = _uiState.value.copy(newsList = response.articles.toMutableList())
                } else {
                    _uiState.value.newsList.addAll(response.articles)
                }

                _uiState.value = _uiState.value.copy(state = ListPagination.IDLE)
//                listState = ListState.IDLE

                if (_uiState.value.canPaginate)
                    _uiState.value = _uiState.value.copy(currPage = _uiState.value.currPage + 1)
            } else {
                _uiState.value = _uiState.value.copy(state = if (_uiState.value.currPage == 1) ListPagination.ERROR else ListPagination.PAGINATION_EXHAUST)
            }

        }
    }

    fun handleNewsResponse(response:NewsDto)=viewModelScope.launch{
        val currentTime = System.currentTimeMillis()
        val expiredAt = currentTime + (1000 * 60 * 60 * 24)
        try{
            newsDb.withTransaction {
                newsDb.dao.clearCache(currentTime)
                newsDb.dao.upsertAll(response.articles.map { it.toNewsEntity(expiredAt) }.toList())
//                if(currPage==1){
//                    paginatedList.clear()
//
//                }
            }

        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

}


data class NewsListUi(
    val currPage:Int,
    val newsList:MutableList<Article>,
    val state:ListPagination,
    val canPaginate:Boolean
)