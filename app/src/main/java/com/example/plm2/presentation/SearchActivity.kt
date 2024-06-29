package com.example.plm2.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plm2.R
import com.example.plm2.data.local.SearchHistory
import com.example.plm2.domain.model.Track
import com.example.plm2.presentation.base.BaseActivity
import com.example.plm2.presentation.viewmodel.SearchViewModel
import com.example.plm2.presentation.viewmodel.SearchViewModelFactory
import com.example.plm2.data.network.ApiService
import com.example.plm2.data.repository.TrackRepositoryImpl
import com.example.plm2.domain.interactor.TrackInteractorImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : BaseActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var searchQuery: String
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var searchHistory: SearchHistory
    private lateinit var placeholderImageView: ImageView
    private lateinit var placeholderTextView: TextView
    private lateinit var secondPlaceholderImageView: ImageView
    private lateinit var secondPlaceholderTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar

    private val BASE_URL = "https://itunes.apple.com"
    private var searchJob: Job? = null
    private var debounceJob: Job? = null
    private val debouncePeriod: Long = 2000 // Задержка debounce в миллисекундах

    private var isSearching: Boolean = false

    private val viewModel: SearchViewModel by viewModels {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val trackRepository = TrackRepositoryImpl(apiService)
        val trackInteractor = TrackInteractorImpl(trackRepository)
        SearchViewModelFactory(trackInteractor)
    }

    @SuppressLint("MissingInflatedId", "SuspiciousIndentation", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        viewModel.searchResults.observe(this, Observer { tracks: List<Track> ->
            trackAdapter.setTracks(tracks)
            trackAdapter.notifyDataSetChanged()
            recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
            updatePlaceholderVisibility(tracks)
        })

        // Инициализация переменных для плейсхолдеров
        placeholderImageView = findViewById(R.id.placeholderImageView)
        placeholderTextView = findViewById(R.id.placeholderTextView)
        secondPlaceholderImageView = findViewById(R.id.secondPlaceholderImageView)
        secondPlaceholderTextView = findViewById(R.id.secondPlaceholderTextView)
        progressBar = findViewById(R.id.progress_bar)

        val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)

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

        // Обработчика кнопки "Очистить историю"
        clearButton.setOnClickListener {
            inputEditText.setText("")
            var lastSearchQuery = null
            trackAdapter.setTracks(emptyList())
            recyclerView.visibility = View.GONE
            updatePlaceholderVisibility(emptyList())
            displaySearchHistory()
            hideSearchHistory()
        }

        // Инициализация sharedPreferences
        sharedPreferences = getSharedPreferences("search_history_key", Context.MODE_PRIVATE)
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                debounceJob?.cancel()
                debounceJob = lifecycleScope.launch {
                    delay(debouncePeriod)
                    s?.let {
                        if (it.isNotEmpty()) {
                            performSearch(it.toString())
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработчик события нажатия кнопки "Ввод" на клавиатуре
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(inputEditText.text.toString())
                hideKeyboard()
                true
            } else {
                false
            }
        }

        displaySearchHistory() // Первоначальное отображение истории поиска
    }

    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        } ?: false
    }

    private fun performSearch(query: String) {
        if (isSearching) return // Предотвращаем повторный запуск поиска, если он уже идет

        progressBar.visibility = View.VISIBLE
        isSearching = true
        searchQuery = query
        hideSearchHistory()

        viewModel.searchTracks(query)
    }

    // Метод для отображения истории поиска
    private fun displaySearchHistory() {
        val history = searchHistory.getSearchHistory()
        if (history.isNotEmpty()) {
            historyAdapter.setTracks(history)
            historyAdapter.notifyDataSetChanged()
            historyRecyclerView.visibility = View.VISIBLE
        } else {
            historyRecyclerView.visibility = View.GONE
        }
    }

    // Метод для скрытия истории поиска
    private fun hideSearchHistory() {
        historyRecyclerView.visibility = View.GONE
    }

    // Метод для обновления видимости плейсхолдеров
    private fun updatePlaceholderVisibility(tracks: List<com.example.plm2.domain.model.Track>) {
        if (tracks.isEmpty()) {
            placeholderImageView.visibility = View.VISIBLE
            placeholderTextView.visibility = View.VISIBLE
            secondPlaceholderImageView.visibility = View.VISIBLE
            secondPlaceholderTextView.visibility = View.VISIBLE
        } else {
            placeholderImageView.visibility = View.GONE
            placeholderTextView.visibility = View.GONE
            secondPlaceholderImageView.visibility = View.GONE
            secondPlaceholderTextView.visibility = View.GONE
        }
        progressBar.visibility = View.GONE
        isSearching = false // Сброс флага поиска после завершения обновления UI
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun createTrackList(): List<Track> {
        // Создание и возврат списка треков
        return emptyList()
    }
}
