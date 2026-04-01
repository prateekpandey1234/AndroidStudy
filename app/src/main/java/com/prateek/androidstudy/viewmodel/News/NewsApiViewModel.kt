package com.prateek.androidstudy.viewmodel.News

import android.util.Log
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
import com.prateek.androidstudy.data.local.newsApi.toArticle
import com.prateek.androidstudy.data.remote.liveStream.ActiveStream
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
    val paginatedList = mutableStateListOf<List<Article>>()

    var currPage by mutableStateOf(1)
    // to check for errors in if further pagination can be made or there is some sort of error
    var canPaginate by mutableStateOf(false)
    var listState by mutableStateOf(ListPagination.IDLE)

    var _uiState = MutableStateFlow<NewsListUi>(NewsListUi(1,  mutableListOf(),ListPagination.IDLE,true))
    val uiState:StateFlow<NewsListUi> = _uiState

    private var _streamState = MutableStateFlow<StreamList>(StreamList(listOf(),true))
    val streamState:StateFlow<StreamList> = _streamState


    init{


    }
    fun getNews() = viewModelScope.launch {

        if( _uiState.value.canPaginate && _uiState.value.state == ListPagination.IDLE){

            _uiState.value= _uiState.value.copy(state = if(_uiState.value.currPage==1)ListPagination.LOADING else ListPagination.PAGINATING)

            val response = newsApiService.getNews("soccer",_uiState.value.currPage,20)

            if(response.isSuccessful && response.body()!=null && response.body()?.status.equals("ok",true)){
                _uiState.value= _uiState.value.copy(currPage = _uiState.value.currPage+1,canPaginate =   response.body()?.articles?.size == 20)
                handleNewsResponse(response.body()?: NewsDto("",0, listOf()))
            } else {
                _uiState.value = _uiState.value.copy(state = if (_uiState.value.currPage == 1) ListPagination.ERROR else ListPagination.PAGINATION_EXHAUST)
            }

        }
    }

    fun getStreams() = viewModelScope.launch {
        val response = newsApiService.getStreamList()
        if(response.isSuccessful && response.body()!=null){
            _streamState.value = _streamState.value.copy(data = response.body()!!.streams)
        }
    }

    fun handleNewsResponse(response:NewsDto)=viewModelScope.launch{
        val currentTime = System.currentTimeMillis()
        val expiredAt = currentTime + (1000 * 60 )
        try{
            newsDb.withTransaction {// with transaction allows to rollback db and handle db queries if any one of transaction fails
                newsDb.dao.clearCache(currentTime)
                newsDb.dao.upsertAll(response.articles.map { it.toNewsEntity(expiredAt) }.toList())
                _uiState.value.newsList.addAll(newsDb.dao.getAllNews().map { it.toArticle() }.toList())
                _uiState.value = _uiState.value.copy(state = ListPagination.IDLE)
                if (_uiState.value.canPaginate) _uiState.value = _uiState.value.copy(currPage = _uiState.value.currPage + 1)
                Log.d("currPage",currentTime.toString())
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

data class StreamList(
    var data : List<ActiveStream>,
    var success:Boolean
)
