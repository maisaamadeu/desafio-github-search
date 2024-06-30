package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var btnRestart : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        showUserName()
        setupRetrofit()
        setupListeners()

        if (nomeUsuario.text.isNotBlank()) {
            getAllReposByUserName(this)
        }
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    private fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        btnRestart = findViewById(R.id.iv_restart)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        btnConfirmar.setOnClickListener {
            with(nomeUsuario) {
                text?.let {
                    if (text.isNotBlank()) {
                        saveUserLocal()
                        getAllReposByUserName(context)
                    } else {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        builder
                            .setMessage("O nome de usuário é obrigatório!")
                            .setTitle("Oops")

                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                }
            }
        }

        btnRestart.setOnClickListener {
            nomeUsuario.text.clear()
            saveUserLocal()
            setupAdapter(listOf())
        }
    }


    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPreferences.edit()) {
            putString(getString(R.string.saved_name), nomeUsuario.text.toString())
            apply()
        }
    }

    private fun showUserName() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return
        val userName = sharedPreferences.getString(getString(R.string.saved_name), "")
        userName?.let {
            if (userName.isNotBlank()) {
                nomeUsuario.setText(userName)
            }
        }

    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    private fun setupRetrofit() {
        val retrofit =
            Retrofit.Builder().baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        githubApi = retrofit.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    private fun getAllReposByUserName(context: Context) {
        githubApi.getAllRepositoriesByUser(nomeUsuario.text.toString())
            .enqueue(object : Callback<List<Repository>> {
                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {
                    try {
                        if (response.isSuccessful) {
                            if (response.body() == null) throw Exception()
                            if (response.body()!!.isEmpty()) throw Exception()

                            response.body()?.let { setupAdapter(it) }
                        } else {
                            throw Exception()
                        }
                    } catch (e: Exception) {
                        showErrorGetAllReposByUserNameDialog(context)
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    showErrorGetAllReposByUserNameDialog(context)
                }

            })
    }

    private fun showErrorGetAllReposByUserNameDialog(context: Context) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("Aconteceu um erro misterioso ao buscar os repositórios desse usuário! Por favor, verique o nome e sua conexão a internet e tente novamente!")
            .setTitle("Oops")

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Metodo responsavel por realizar a configuracao do adapter
    private fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(list)
        adapter.btnShareLister = { repository ->
            shareRepositoryLink(repository.htmlUrl)
        }

        adapter.carItemLister = { repository ->
            openBrowser(repository.htmlUrl)
        }

        listaRepositories.adapter = adapter
    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    private fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio
    private fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }
}