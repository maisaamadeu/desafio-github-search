package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var carItemLister: (Repository) -> Unit = {}
    var btnShareLister: (Repository) -> Unit = {}

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repo: Repository = repositories[position]
        val repoName: String = repo.name

        holder.itemView.setOnClickListener {
            carItemLister(repo)
        }

        holder.tvPreco.text = repoName

        holder.ivFavorite.setOnClickListener {
            btnShareLister(repo)
        }

    }

    // Pega a quantidade de repositorios da lista
    override fun getItemCount(): Int = repositories.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFavorite: ImageView
        val tvPreco: TextView

        init {
            view.apply {
                ivFavorite = findViewById(R.id.iv_favorite)
                tvPreco = findViewById(R.id.tv_preco)
            }
        }

    }
}


