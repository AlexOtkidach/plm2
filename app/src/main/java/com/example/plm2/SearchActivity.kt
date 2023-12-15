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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

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
    private lateinit var placeholderImageView: ImageView
    private lateinit var placeholderTextView: TextView
    private lateinit var secondPlaceholderImageView: ImageView
    private lateinit var secondPlaceholderTextView: TextView


    private val BASE_URL = "https://itunes.apple.com"
    private var searchJob: Job? = null

    private val gson by lazy { Gson() }
    private val historyType: Type by lazy { object : TypeToken<List<Track>>() {}.type }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация переменных для плейсхолдеров
        placeholderImageView = findViewById(R.id.placeholderImageView)
        placeholderTextView = findViewById(R.id.placeholderTextView)
        secondPlaceholderImageView = findViewById(R.id.secondPlaceholderImageView)
        secondPlaceholderTextView = findViewById(R.id.secondPlaceholderTextView)


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

        // Для проверки доступности сети
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
        historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            historyAdapter = TrackAdapter(emptyList())
            adapter = historyAdapter
        }

        val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)
        val clearButton = findViewById<ImageView>(R.id.seachBarLineImageV)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)

        clearButton.setOnClickListener {
            inputEditText.setText("")
            lastSearchQuery = null
            trackAdapter.setTracks(emptyList())
            recyclerView.visibility = View.GONE
            updatePlaceholderVisibility(emptyList())
            displaySearchHistory()
        }
            // Очистить историю поиска и обновить видимость кнопки
            clearHistoryButton.setOnClickListener {
                // Очистить историю поиска и обновить видимость кнопки
                searchHistory.clearSearchHistory()
                displaySearchHistory()
            }

        refreshButton.setText(R.string.refresh_button)
        refreshButton.transformationMethod = null
        trackAdapter = TrackAdapter(createTrackList())
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.visibility = View.GONE
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

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
        searchHistory = SearchHistory(getSharedPreferences("search_prefs", Context.MODE_PRIVATE))
        displaySearchHistory()  // Первоначальное отображение истории поиска
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
        searchQuery = findViewById<EditText>(R.id.seachBarLineEditT).text.toString()
        hideSearchHistory() // Скрыть историю при начале поиска

        val call = apiService.search(searchQuery)
        call.enqueue(object : Callback<SearchResults> {
            override fun onResponse(call: Call<SearchResults>, response: Response<SearchResults>) {
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
                    recyclerView.visibility = if (!tracks.isNullOrEmpty()) View.VISIBLE else View.GONE
                    updatePlaceholderVisibility(tracks)
                    lastSearchQuery = searchQuery
                    tracks?.forEach { track ->
                        updateSearchHistory(track) // Метод для обновления истории поиска
                    }
                } else {
                    showSearchErrorPlaceholder()
                    updatePlaceholderVisibility(emptyList()) // Обновление видимости плейсхолдеров
                }
                    if (isNetworkAvailable(connectivityManager)) {
                        findViewById<Button>(R.id.refreshButton).visibility = View.GONE
                    }
            }
            override fun onFailure(call: Call<SearchResults>, t: Throwable) {
                showSearchErrorPlaceholder()
                updatePlaceholderVisibility(emptyList())
            }
        })
    }
    // Использование Gson для добавления трека в историю
    private fun updateSearchHistory(track: Track) {
        Log.d("SearchActivity", "Updating search history with track: ${track.trackName}")
        val historyList = getSearchHistory().toMutableList()
        if (historyList.size >= 10) {
            historyList.removeAt(historyList.size - 1)
        }
        if (!historyList.contains(track)) {
            historyList.add(0, track) // Добавляем в начало списка
            saveSearchHistory(historyList)
        }
        saveSearchHistory(historyList)
        displaySearchHistory()// Обновить историю поиска
    }

    // Получение истории поиска из SharedPreferences
    private fun getSearchHistory(): List<Track> {
        val json = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
            .getString("search_history", null)
        return gson.fromJson(json, historyType) ?: emptyList()
    }

    // Сохранение истории поиска в SharedPreferences
    private fun saveSearchHistory(historyList: List<Track>) {
        val json = gson.toJson(historyList)
        getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("search_history", json)
            .apply()
    }
    // Метод для отображения истории поиска
    private fun displaySearchHistory() {
        // Скрыть все плейсхолдеры
        placeholderImageView.visibility = View.GONE
        placeholderTextView.visibility = View.GONE
        secondPlaceholderImageView.visibility = View.GONE
        secondPlaceholderTextView.visibility = View.GONE

        val historyTracks = searchHistory.getSearchHistory()
        historyAdapter.setTracks(historyTracks)
        historyAdapter.notifyDataSetChanged()

        // Установить видимость компонентов истории
        findViewById<Button>(R.id.clearHistoryButton).visibility =
            if (historyTracks.isNotEmpty()) View.VISIBLE else View.GONE

    }
    // Метод для скрытия истории поиска
    private fun hideSearchHistory() {
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
    private fun saveThemePreference(isDarkTheme: Boolean) {
        val editor = getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
        editor.putBoolean("dark_theme", isDarkTheme)
        editor.apply()
    }
    private fun createTrackList(): List<Track> {
        return emptyList()
    }
    private fun hideKeyboard() {
        val view = this.currentFocus
        view?.let {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}