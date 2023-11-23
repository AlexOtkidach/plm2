package com.example.plm2

import TrackAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var searchQuery: String
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var apiService: MusicApiService
    private lateinit var viewModel: SearchViewModel
    private var lastSearchQuery: String? = null

    private val BASE_URL = "https://itunes.apple.com"

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Инициализируйте переменную в методе onCreate
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)
        val clearButton = findViewById<ImageView>(R.id.seachBarLineImageV)
        val refreshButton = findViewById<Button>(R.id.refreshButton)



        clearButton.setOnClickListener {
            inputEditText.setText("")
            lastSearchQuery = null
            trackAdapter.setTracks(emptyList())
            recyclerView.visibility = View.GONE
            updatePlaceholderVisibility(emptyList())
            hideKeyboard(inputEditText)
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                true
            } else {
                false
            }
        }

        trackAdapter = TrackAdapter(createTrackList())

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.visibility = View.GONE
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchQuery = s.toString()
                trackAdapter.filter(searchQuery)
                recyclerView.visibility =
                    if (s.isNullOrBlank() || trackAdapter.itemCount > 0) View.VISIBLE else View.GONE

                // Проверка для скрытия первого плейсхолдера при стирании текста в строке поиск
                if (s.isNullOrBlank()) {
                    findViewById<ImageView>(R.id.placeholderImageView).visibility = View.GONE
                    findViewById<TextView>(R.id.placeholderTextView).visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(SearchViewModel::class.java)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MusicApiService::class.java)

        searchQuery = ""
        performSearch()

        // Добавление второго изображения для второго плейсхолдера
        val secondPlaceholderImageView = findViewById<ImageView>(R.id.secondPlaceholderImageView)
        val secondPlaceholderTextView = findViewById<TextView>(R.id.secondPlaceholderTextView)

        // Инициализация видимости обоих плейсхолдеров как невидимых
        val placeholderImageView = findViewById<ImageView>(R.id.placeholderImageView)
        val placeholderTextView = findViewById<TextView>(R.id.placeholderTextView)
        placeholderImageView.visibility = View.GONE
        placeholderTextView.visibility = View.GONE
        secondPlaceholderImageView.visibility = View.GONE
        secondPlaceholderTextView.visibility = View.GONE

        // Добавление логики для управления видимостью второго плейсхолдера и кнопки "Обновить"
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Проверка состояния интернет-соединения
        if (!isNetworkAvailable(connectivityManager)) {

            // Если нет подключения к интернету, отобразите второй плейсхолдер и кнопку "Обновить"
            findViewById<ImageView>(R.id.secondPlaceholderImageView).visibility = View.VISIBLE
            findViewById<TextView>(R.id.secondPlaceholderTextView).visibility = View.VISIBLE
            findViewById<Button>(R.id.refreshButton).visibility = View.VISIBLE

            // Скрываем первый плейсхолдер, так как второй уже виден
            findViewById<ImageView>(R.id.placeholderImageView).visibility = View.GONE
            findViewById<TextView>(R.id.placeholderTextView).visibility = View.GONE

            // Добавление обработчика клика на кнопке "Обновить"
            findViewById<Button>(R.id.refreshButton).setOnClickListener {
                performSearch()
            }
        } else {
            // Если есть подключение к интернету, скрываем второй плейсхолдер и кнопку "Обновить"
            findViewById<ImageView>(R.id.secondPlaceholderImageView).visibility = View.GONE
            findViewById<TextView>(R.id.secondPlaceholderTextView).visibility = View.GONE
            findViewById<Button>(R.id.refreshButton).visibility = View.GONE
        }
    }

    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun performSearch() {
        val call = apiService.search(searchQuery)

        call.enqueue(object : Callback<SearchResults> {
            override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
                // Добавьте вывод в лог для кода ответа
                println("Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val songs = response.body()?.results
                    if (songs != null) {
                        for (song in songs) {
                            println("Track Name: ${song.trackName}, Artist Name: ${song.artistName}")
                        }
                    }
                    val tracks: List<Track>? = songs?.map { song ->
                        Track(
                            trackName = song.trackName ?: "",
                            artistName = song.artistName ?: "",
                            trackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(
                                song.trackTimeMillis ?: 0L
                            ),
                            artworkUrl100 = song.artworkUrl100 ?: ""
                        )
                    }
                    trackAdapter.setTracks(tracks)
                    trackAdapter.notifyDataSetChanged()
                    recyclerView.visibility =
                        if (searchQuery.isNotBlank() && !tracks.isNullOrEmpty()) View.VISIBLE else View.GONE
                    updatePlaceholderVisibility(tracks)
                    lastSearchQuery = searchQuery
                    // Проверка наличия интернет-соединения

                    if (isNetworkAvailable(connectivityManager)) {
                        // Если есть интернет, скрываем кнопку "Обновить"
                        findViewById<Button>(R.id.refreshButton).visibility = View.GONE
                    }

                } else {
                    showSearchErrorPlaceholder()
                }
            }

            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                showSearchErrorPlaceholder()
            }
        })
    }

    private fun updatePlaceholderVisibility(tracks: List<Track>?) {
        Handler(mainLooper).post {
            val placeholderImageView = findViewById<ImageView>(R.id.placeholderImageView)
            val placeholderTextView = findViewById<TextView>(R.id.placeholderTextView)
            val secondPlaceholderImageView = findViewById<ImageView>(R.id.secondPlaceholderImageView)
            val secondPlaceholderTextView = findViewById<TextView>(R.id.secondPlaceholderTextView)

            if (tracks.isNullOrEmpty() && searchQuery.isNotBlank()) {
                placeholderImageView.visibility = View.VISIBLE
                placeholderTextView.visibility = View.VISIBLE

                // Скрываем второй плейсхолдер, так как первый уже виден
                secondPlaceholderImageView.visibility = View.GONE
                secondPlaceholderTextView.visibility = View.GONE
            } else if (tracks.isNullOrEmpty() && searchQuery.isBlank()) {
                // Если поисковый запрос пуст, то оба плейсхолдера должны быть скрыты
                placeholderImageView.visibility = View.GONE
                placeholderTextView.visibility = View.GONE
                secondPlaceholderImageView.visibility = View.GONE
                secondPlaceholderTextView.visibility = View.GONE
            } else {
                // Если треки найдены, оба плейсхолдера должны быть скрыты
                placeholderImageView.visibility = View.GONE
                placeholderTextView.visibility = View.GONE

                // Добавление видимости для второго плейсхолдера
                secondPlaceholderImageView.visibility = View.GONE
                secondPlaceholderTextView.visibility = View.GONE
            }
        }
    }

    private fun showSearchErrorPlaceholder() {
        Handler(mainLooper).post {
            val refreshButton = findViewById<Button>(R.id.refreshButton)
            val placeholderImageView = findViewById<ImageView>(R.id.placeholderImageView)
            val placeholderTextView = findViewById<TextView>(R.id.placeholderTextView)
            val secondPlaceholderImageView = findViewById<ImageView>(R.id.secondPlaceholderImageView)
            val secondPlaceholderTextView = findViewById<TextView>(R.id.secondPlaceholderTextView)

            // Устанавливаем видимость второго плейсхолдера и кнопки "Обновить"
            secondPlaceholderImageView.visibility = View.VISIBLE
            secondPlaceholderTextView.visibility = View.VISIBLE
            refreshButton.visibility = View.VISIBLE

            // Скрываем первый плейсхолдер, так как второй уже виден
            placeholderImageView.visibility = View.GONE
            placeholderTextView.visibility = View.GONE

            // Добавление обработчика клика на кнопке "Обновить"
            refreshButton.setOnClickListener {
                performSearch()
            }
        }
    }

    private fun createTrackList(): List<Track> {
        return emptyList()
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}