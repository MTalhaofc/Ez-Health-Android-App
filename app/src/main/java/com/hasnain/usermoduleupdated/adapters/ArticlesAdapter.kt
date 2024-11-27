package com.hasnain.usermoduleupdated.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hasnain.usermoduleupdated.R
import com.kwabenaberko.newsapilib.models.Article

 public class ArticlesAdapter : ListAdapter<Article, ArticlesAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

     private var onItemClickListener: ((Article) -> Unit)? = null

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
         val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
         return ArticleViewHolder(view)
     }

     override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
         holder.bind(getItem(position))
     }

     inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         private val titleTextView: TextView = itemView.findViewById(R.id.articleTitle)
         private val imageView: ImageView = itemView.findViewById(R.id.articleImage)
         private val authorTextView: TextView = itemView.findViewById(R.id.articleAuthor)
         private val publishedAtTextView: TextView = itemView.findViewById(R.id.articlePublishedAt)

         fun bind(article: Article) {
             titleTextView.text = article.title
             authorTextView.text = article.author ?: "Unknown Author"
             publishedAtTextView.text = article.publishedAt?.take(10) ?: "Unknown time"

             Glide.with(itemView.context)
                 .load(article.urlToImage)
                 .placeholder(R.drawable.placeholder_image)
                 .error(R.drawable.error_image)
                 .into(imageView)

             // This part registers the click listener
             itemView.setOnClickListener {
                 onItemClickListener?.invoke(article)
             }
         }
     }

     fun setOnItemClickListener(listener: (Article) -> Unit) {
         onItemClickListener = listener
     }

     class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
         override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean = oldItem.url == newItem.url
         @SuppressLint("DiffUtilEquals")
         override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean = oldItem == newItem
     }
 }
