package com.example.plm2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var searchQuery: String
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var apiService: MusicApiService
    private lateinit var viewModel: SearchViewModel
    private var lastSearchQuery: String? = null
    private lateinit var searchHistory: SearchHistory

    private val BASE_URL = "https://itunes.apple.com"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация и настройка Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val color = ContextCompat.getColor(this, typedValue.resourceId)
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)
        upArrow?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


        // Настройка адаптера и RecyclerView для результатов поиска
        trackAdapter = TrackAdapter(createTrackList()).apply {
            onTrackClickListener = { track ->
                updateSearchHistory(track)
                displaySearchHistory()  // Показать историю поиска после выбора трека
            }
        }
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            visibility = View.GONE
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = trackAdapter
        }

        // Настройка RecyclerView и адаптера для истории поиска
        historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            historyAdapter = TrackAdapter(emptyList())
            adapter = historyAdapter
        }

        val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)
        val clearButton = findViewById<ImageView>(R.id.seachBarLineImageV)
        val refreshButton = findViewById<Button>(R.id.refreshButton)

        refreshButton.setText(R.string.refresh_button)
        refreshButton.transformationMethod = null

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
                hideKeyboard(inputEditText)
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
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchQuery = s.toString()
                trackAdapter.filter(searchQuery)
                recyclerView.visibility =
                    if (s.isNullOrBlank() || trackAdapter.itemCount > 0) View.VISIBLE else View.GONE

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

        val secondPlaceholderImageView =
            findViewById<ImageView>(R.id.secondPlaceholderImageView)
        val secondPlaceholderTextView = findViewById<TextView>(R.id.secondPlaceholderTextView)

        val placeholderImageView = findViewById<ImageView>(R.id.placeholderImageView)
        val placeholderTextView = findViewById<TextView>(R.id.placeholderTextView)
        val searchHistoryTitle = findViewById<TextView>(R.id.searchHistoryTitle)

        if (!isNetworkAvailable(connectivityManager)) {
            secondPlaceholderImageView.visibility = View.VISIBLE
            secondPlaceholderTextView.visibility = View.VISIBLE
            refreshButton.visibility = View.VISIBLE

            placeholderImageView.visibility = View.GONE
            placeholderTextView.visibility = View.GONE

            refreshButton.setOnClickListener {
                performSearch()
            }
        } else {
            secondPlaceholderImageView.visibility = View.GONE
            secondPlaceholderTextView.visibility = View.GONE
            refreshButton.visibility = View.GONE
        }

        inputEditText.clearFocus()

        refreshButton.setOnClickListener {
            performSearch()
            if (lastSearchQuery != null) {
                searchQuery = lastSearchQuery!!
                performSearch()
            } else {
                performSearch()
            }

            ViewCompat.animate(refreshButton)
                .setDuration(200)
                .alpha(0.5f)
                .withEndAction {
                    ViewCompat.animate(refreshButton)
                        .setDuration(200)
                        .alpha(1.0f)
                        .start()
                }
                .start()
        }

        searchHistory = SearchHistory(getSharedPreferences("search_prefs", Context.MODE_PRIVATE))
        displaySearchHistory()  // Первоначальное отображение истории поиска
    }

    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                )
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || hasTransport(
                    NetworkCapabilities.TRANSPORT_BLUETOOTH
                )
            } ?: false
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
        searchHistory =
            SearchHistory(getSharedPreferences("search_prefs", Context.MODE_PRIVATE))
    }

    private fun performSearch() {
        val call = apiService.search(searchQuery)

        call.enqueue(object : Callback<SearchResults> {
            override fun onResponse(
                call: Call<SearchResults>,
                response: Response<SearchResults>
            ) {
                if (response.isSuccessful) {
                    val songs = response.body()?.results
                    val tracks: List<Track>? = songs?.map { song ->
                        Track(
                            trackName = song.trackName ?: "",
                            artistName = song.artistName ?: "",
                            trackTimeMillis = song.trackTimeMillis ?: 0L,
                            artworkUrl100 = song.artworkUrl100 ?: "",
                            trackId = song.trackId.toLongOrNull() ?: 0L // Преобразование строки в Long
                        )
                    }
                    trackAdapter.setTracks(tracks)
                    trackAdapter.notifyDataSetChanged()
                    recyclerView.visibility =
                        if (searchQuery.isNotBlank() && !tracks.isNullOrEmpty()) View.VISIBLE else View.GONE
                    updatePlaceholderVisibility(tracks)
                    lastSearchQuery = searchQuery

                    tracks?.forEach { track ->
                        updateSearchHistory(track) // Метод для обновления истории поиска
                    }

                    if (isNetworkAvailable(connectivityManager)) {
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

    // Обновление истории поиска
    private fun updateSearchHistory(track: Track) {
        searchHistory.addTrackToHistory(track)
        displaySearchHistory() // Обновить историю поиска
    }

    // Отображение истории поиска
    private fun displaySearchHistory() {
        val historyTracks = searchHistory.getSearchHistory()
        historyAdapter.setTracks(historyTracks)
        historyAdapter.notifyDataSetChanged()
        historyRecyclerView.visibility = if (historyTracks.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun updatePlaceholderVisibility(tracks: List<Track>?) {
        Handler(mainLooper).post {
            val placeholderImageView = findViewById<ImageView>(R.id.placeholderImageView)
            val placeholderTextView = findViewById<TextView>(R.id.placeholderTextView)
            val secondPlaceholderImageView =
                findViewById<ImageView>(R.id.secondPlaceholderImageView)
            val secondPlaceholderTextView =
                findViewById<TextView>(R.id.secondPlaceholderTextView)
            val refreshButton = findViewById<Button>(R.id.refreshButton)
            val searchHistoryTitle = findViewById<TextView>(R.id.searchHistoryTitle)

            if (tracks.isNullOrEmpty() && searchQuery.isNotBlank()) {
                placeholderImageView.visibility = View.VISIBLE
                placeholderTextView.visibility = View.VISIBLE

                secondPlaceholderImageView.visibility = View.GONE
                secondPlaceholderTextView.visibility = View.GONE
                refreshButton.visibility = View.GONE
            } else if (tracks.isNullOrEmpty() && searchQuery.isBlank()) {
                placeholderImageView.visibility = View.GONE
                placeholderTextView.visibility = View.GONE

                secondPlaceholderImageView.visibility = View.GONE
                secondPlaceholderTextView.visibility = View.GONE
                refreshButton.visibility = View.GONE
            } else {
                placeholderImageView.visibility = View.GONE
                placeholderTextView.visibility = View.GONE

                secondPlaceholderImageView.visibility = View.GONE
                secondPlaceholderTextView.visibility = View.GONE
                refreshButton.visibility = View.GONE
            }

            // Обновление видимости заголовка истории поиска
            if (searchHistory.getSearchHistory().isNotEmpty()) {
                searchHistoryTitle.visibility = View.VISIBLE
            } else {
                searchHistoryTitle.visibility = View.GONE
            }
        }
    }

    private fun showSearchErrorPlaceholder() {
        Handler(mainLooper).post {
            val refreshButton = findViewById<Button>(R.id.refreshButton)
            val placeholderImageView = findViewById<ImageView>(R.id.placeholderImageView)
            val placeholderTextView = findViewById<TextView>(R.id.placeholderTextView)
            val secondPlaceholderImageView =
                findViewById<ImageView>(R.id.secondPlaceholderImageView)
            val secondPlaceholderTextView =
                findViewById<TextView>(R.id.secondPlaceholderTextView)

            secondPlaceholderImageView.visibility = View.VISIBLE
            secondPlaceholderTextView.visibility = View.VISIBLE
            refreshButton.visibility = View.VISIBLE

            placeholderImageView.visibility = View.GONE
            placeholderTextView.visibility = View.GONE

            refreshButton.setOnClickListener {
                performSearch()
            }
        }
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        val editor = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
        editor.putBoolean("dark_theme", isDarkTheme)
        editor.apply()
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