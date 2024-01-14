package com.example.plm2

import Track
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class SearchActivity : BaseActivity() {

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
    private lateinit var placeholderImageView: ImageView
    private lateinit var placeholderTextView: TextView
    private lateinit var secondPlaceholderImageView: ImageView
    private lateinit var secondPlaceholderTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var inputEditText: EditText

    private val BASE_URL = "https://itunes.apple.com"
    private var searchJob: Job? = null

    private val debouncer = Observable.create(ObservableOnSubscribe<String> { emitter ->
        inputEditText = findViewById(R.id.seachBarLineEditT)
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { emitter.onNext(it.toString()) }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    })
        .debounce(2000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())

    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация переменных для плейсхолдеров
        placeholderImageView = findViewById(R.id.placeholderImageView)
        placeholderTextView = findViewById(R.id.placeholderTextView)
        secondPlaceholderImageView = findViewById(R.id.secondPlaceholderImageView)
        secondPlaceholderTextView = findViewById(R.id.secondPlaceholderTextView)

        debouncer.subscribe { query ->
            Handler(mainLooper).post {
                performSearch()
            }
        }

        // Инициализация и настройка Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val typedValue = TypedValue()
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

        // Для проверки доступности сети
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager


        // Настройка адаптера и RecyclerView для результатов поиска
        trackAdapter = TrackAdapter(createTrackList())
        trackAdapter.onTrackClickListener = { track: Track ->
            searchHistory.addTrackToHistory(track)
            displaySearchHistory()
            // Intent для перехода на экран "Аудиоплеер"
            val intent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
            intent.putExtra("track", track as Parcelable)
            intent.putExtra("trackImageUrl", track.artworkUrl100)
            startActivity(intent)
        }

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            visibility = View.GONE
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = trackAdapter
        }

        historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            historyAdapter = TrackAdapter(emptyList())
            adapter = historyAdapter
        }

        historyAdapter.onTrackClickListener = { track: Track ->
            // Intent для перехода на экран "Аудиоплеер"
            val intent = Intent(this@SearchActivity, AudioPlayerActivity::class.java)
            intent.putExtra("track", track as Parcelable)
            startActivity(intent)
        }

        val clearButton = findViewById<ImageView>(R.id.seachBarLineImageV)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)


        //Обработчика кнопки "Очистить историю"
        clearButton.setOnClickListener {
            inputEditText.setText("")
            lastSearchQuery = null
            trackAdapter.setTracks(emptyList())
            recyclerView.visibility = View.GONE
            updatePlaceholderVisibility(emptyList())
            displaySearchHistory()
            hideSearchHistory()
        }
        // Инициализация sharedPreferences
        sharedPreferences = getSharedPreferences("search_history_key", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

            // Очистить историю поиска и обновить видимость кнопки
            clearHistoryButton.setOnClickListener {
                searchHistory.clearSearchHistory()
                historyRecyclerView.visibility = View.GONE
                it.visibility = View.GONE
                hideSearchHistory()
            }
        refreshButton.setText(R.string.refresh_button)
        refreshButton.transformationMethod = null

        // Обработка событий в поле ввода текста
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            // При изменении текста
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500) //Задержка перед поиском для предотвращения частых запросов
                    // Отображение или скрытие элементов истории в зависимости от состояния поискового запроса
                    if (s.isNullOrBlank()) {
                        showSearchHistoryComponents() // Показать историю, если поле поиска пустое
                        clearButton.visibility = View.GONE
                        displaySearchHistory()
                    } else {
                        hideSearchHistoryComponents() // Скрыть историю при вводе текста
                        clearButton.visibility = View.VISIBLE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        // Обработчик события нажатия кнопки "Ввод" на клавиатуре
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                hideKeyboard()
                true
            } else {
                false
            }
        }
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
        // Инициализация sharedPreferences
        displaySearchHistory()  // Первоначальное отображение истории поиска
    }

    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities?.let {
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        it.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            } ?: false
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    private fun performSearch() {
        Log.d("SearchActivity", "performSearch called")
        showProgressBar()
        searchQuery = findViewById<EditText>(R.id.seachBarLineEditT).text.toString()
        hideSearchHistory() // Скрыть историю при начале поиска
        Log.d("SearchActivity", "Search query: $searchQuery")
        val call = apiService.search(searchQuery)
        call.enqueue(object : Callback<SearchResults> {
            override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
                Log.d("SearchActivity", "onResponse called")
                if (response.isSuccessful) {
                    val songs = response.body()?.results
                    val tracks: List<Track>? = songs?.map { song ->
                        Track(
                            itemId = song.trackId.toLongOrNull() ?: 0L,
                            trackName = song.trackName ?: "",
                            artistName = song.artistName ?: "",
                            trackTimeMillis = song.trackTimeMillis ?: 0L,
                            artworkUrl100 = song.artworkUrl100 ?: "",
                            collectionName = song.collectionName ?: "", // Добавил значение по умолчанию
                            releaseDate = song.releaseDate ?: "", // Добавил значение по умолчанию
                            primaryGenreName = song.primaryGenreName ?: "", // Добавил значение по умолчанию
                            country = song.country ?: "" // Добавил значение по умолчанию
                        )
                    }
                    trackAdapter.setTracks(tracks)
                    trackAdapter.notifyDataSetChanged()
                    recyclerView.visibility = if (!tracks.isNullOrEmpty()) View.VISIBLE else View.GONE
                    updatePlaceholderVisibility(tracks)
                    lastSearchQuery = searchQuery
                } else {
                    showSearchErrorPlaceholder()
                    updatePlaceholderVisibility(emptyList()) // Обновление видимости плейсхолдеров
                }
                    if (isNetworkAvailable(connectivityManager)) {
                        findViewById<Button>(R.id.refreshButton).visibility = View.GONE
                    }
                hideProgressBar()
            }
            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                Log.e("SearchActivity", "onFailure called", t)
                showSearchErrorPlaceholder()
                updatePlaceholderVisibility(emptyList())
                hideProgressBar()

            }
        })
    }

    private fun showProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }

    // Метод для отображения истории поиска
    private fun displaySearchHistory() {
        Log.d("SearchActivity", "Displaying search history")
        // Скрыть все плейсхолдеры
        placeholderImageView.visibility = View.GONE
        placeholderTextView.visibility = View.GONE
        secondPlaceholderImageView.visibility = View.GONE
        secondPlaceholderTextView.visibility = View.GONE

        val historyTracks = searchHistory.getSearchHistory()
        historyAdapter.setTracks(historyTracks)
        historyAdapter.notifyDataSetChanged()

        // Установить видимость компонентов истории
        val searchHistoryTitle = findViewById<TextView>(R.id.searchHistoryTitle)
        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)

        if (historyTracks.isEmpty()) {
            searchHistoryTitle.visibility = View.GONE
            historyRecyclerView.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
        } else {
            searchHistoryTitle.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
        }
    }
    private fun showSearchHistoryComponents() {
        // Показываем элементы, связанные с историей поиска
        findViewById<TextView>(R.id.searchHistoryTitle).visibility = View.VISIBLE
        historyRecyclerView.visibility = View.VISIBLE
        findViewById<Button>(R.id.clearHistoryButton).visibility = View.VISIBLE

    }
    private fun hideSearchHistoryComponents() {
        // Скрываем элементы, связанные с историей поиска
        findViewById<TextView>(R.id.searchHistoryTitle).visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.clearHistoryButton).visibility = View.GONE

    }
    // Метод для скрытия истории поиска
    private fun hideSearchHistory() {
        Log.d("SearchActivity", "Hiding search history")
        // Скрыть компоненты истории
        findViewById<TextView>(R.id.searchHistoryTitle).visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        findViewById<Button>(R.id.clearHistoryButton).visibility = View.GONE
    }
    private fun updatePlaceholderVisibility(tracks: List<Track>?) {
        Log.d("SearchActivity", "Updating placeholder visibility: tracks is null or empty = ${tracks.isNullOrEmpty()}, searchQuery is blank = ${searchQuery.isBlank()}")
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
    private fun createTrackList(): List<Track> {
        return emptyList()
    }
    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}