package com.prateek.androidstudy

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.GsonBuilder
import com.prateek.androidstudy.data.local.newsApi.NewsDb
import com.prateek.androidstudy.data.remote.newsApi.NewsApiService
import com.prateek.androidstudy.viewmodel.News.NewsApiViewModel
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection


class RepoTests {
    // to test the retrofit instance we mock the server itself

    private lateinit var mockServer: MockWebServer
    //retrofit instance
    private lateinit var newsApiService: NewsApiService

    private  var newsDb = mock<NewsDb>()

    private var testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: NewsApiViewModel




    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup(){
        mockServer = MockWebServer()
        mockServer.start()

         newsApiService = Retrofit.Builder().baseUrl(mockServer.url("/"))

            .addConverterFactory(GsonConverterFactory.create(GsonBuilder()
                .create()))
            .build().create(NewsApiService::class.java)

        Dispatchers.setMain(testDispatcher)// sets the dispacther to main thread

        viewModel = NewsApiViewModel(newsDb,newsApiService)



    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun teardown(){
        mockServer.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun `constructs Url and parses data successfully $ testing for api call` () = runTest{

        val mockresponse = """
            {
  "status": "ok",
  "totalResults": 2,
  "articles": [
    {
      "source": {
        "id": "techcrunch",
        "name": "TechCrunch"
      },
      "author": "Jane Doe",
      "title": "Jetpack Compose Performance Tips",
      "description": "Learn how to optimize your UI rendering in Jetpack Compose.",
      "url": "https://techcrunch.com/compose-perf",
      "urlToImage": "https://techcrunch.com/images/compose.png",
      "publishedAt": "2026-03-05T09:00:00Z",
      "content": "Jetpack Compose is a modern toolkit for building native UI..."
    },
    {
      "source": {
        "id": null,
        "name": "Android Weekly"
      },
      "author": null,
      "title": "Mastering MockWebServer",
      "description": null,
      "url": "https://androidweekly.net/mockwebserver",
      "urlToImage": null,
      "publishedAt": "2026-03-04T14:30:00Z",
      "content": "Testing network calls is crucial. Here is how to use MockWebServer effectively..."
    }
  ]
}
        """.trimIndent()
        // queuing up the mock response
        mockServer.enqueue(
            MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockresponse)
        )

        val response = newsApiService.getNews("dummy",1,10)

        assertTrue(response.isSuccessful)
        assertEquals("ok",response.body()?.status)
        assertEquals(2L, response.body()?.totalResults)// you have to even consider the data type too
        assertEquals("Jetpack Compose Performance Tips",response.body()?.articles[0]?.title)


        // this way we can access the url that we ran earlier .....
        val request = mockServer.takeRequest()
        assertEquals("/everything?q=dummy&page=1&pageSize=10",request.path)



    }
    //
    @Test
    fun  `test StateFlow ui State`(){
        // we can use . value way to directly check if the update worked ..


        // 1. Check initial state
        assertEquals("Loading", viewModel.uiState.value)

        // 2. Call the function
        //viewModel.loadData()

        // 3. Check the new state
        assertEquals("Success", viewModel.uiState.value)
    }






}