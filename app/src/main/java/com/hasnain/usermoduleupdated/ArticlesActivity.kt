package com.hasnain.usermoduleupdated

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hasnain.usermoduleupdated.adapters.ArticlesAdapter
import com.hasnain.usermoduleupdated.databinding.ActivityAppointmentBinding
import com.hasnain.usermoduleupdated.databinding.ActivityArticlesBinding
import com.hasnain.usermoduleupdated.models.NewsViewModel

class ArticlesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticlesBinding

    private lateinit var articlesAdapter: ArticlesAdapter
    private lateinit var newsViewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticlesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the NewsViewModel
        newsViewModel = ViewModelProvider(this).get(NewsViewModel::class.java)

        // Initialize the ArticlesAdapter
        articlesAdapter = ArticlesAdapter()

        // Set the adapter to RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.articlesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = articlesAdapter

        // Observe the articles LiveData
        newsViewModel.articles.observe(this) { articles ->
            articlesAdapter.submitList(articles)
        }

        // Fetch news headlines for all articles
        newsViewModel.fetchNewsHeadlines()

        // Handle article click to open in browser
        articlesAdapter.setOnItemClickListener { article ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
            startActivity(intent)
        }
    binding.imgBack.setOnClickListener{
        startActivity(Intent(this,MainActivity::class.java))
    }
    }

}