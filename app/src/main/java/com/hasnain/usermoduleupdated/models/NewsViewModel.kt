package com.hasnain.usermoduleupdated.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hasnain.usermoduleupdated.Constant
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.models.Article
import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse

class NewsViewModel : ViewModel() {

    private val _articles = MutableLiveData<List<Article>>()
    val articles: MutableLiveData<List<Article>> = _articles

    init {
        fetchNewsHeadlines() // Fetch health articles on initialization
    }

    /**
     * Fetches health-related news articles using the News API.
     */
    fun fetchNewsHeadlines() {
        val newsApiClient = NewsApiClient(Constant.newsapikey)

        val request = TopHeadlinesRequest.Builder()
            .language("en") // Set the language to English
            .category("health") // Fetch health-related articles
            .build()

        newsApiClient.getTopHeadlines(request, object : NewsApiClient.ArticlesResponseCallback {
            override fun onSuccess(response: ArticleResponse?) {
                response?.articles?.let {
                    // Filter out articles that do not have an author
                    val filteredArticles = it.filter { article -> !article.author.isNullOrEmpty() }
                    _articles.postValue(filteredArticles) // Pass all the filtered articles
                }
            }

            override fun onFailure(throwable: Throwable?) {
                // Log detailed error message and stack trace for easier debugging
                if (throwable != null) {
                    Log.e("NewsAPI", "Response Failed: ${throwable.localizedMessage}")
                    throwable.printStackTrace() // This will provide more insights into the failure
                } else {
                    Log.e("NewsAPI", "Unknown error occurred while fetching articles.")
                }
            }
        })
    }

}
