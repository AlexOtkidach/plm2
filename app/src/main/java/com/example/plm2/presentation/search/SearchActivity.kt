package com.example.plm2.presentation.search

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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
import com.example.plm2.data.local.SharedPreferencesManager
import com.example.plm2.data.network.ApiService
import com.example.plm2.data.repository.TrackRepositoryImpl
import com.example.plm2.domain.interactor.TrackInteractorImpl
import com.example.plm2.domain.model.Track
import com.example.plm2.presentation.base.BaseActivity
import com.example.plm2.presentation.main.TrackAdapter
import com.example.plm2.presentation.player.AudioPlayerActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : BaseActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var placeholderImageView: ImageView
    private lateinit var placeholderTextView: TextView
    private lateinit var secondPlaceholderImageView: ImageView
    private lateinit var secondPlaceholderTextView: TextView
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var progressBar: ProgressBar

    private var searchJob: Job? = null
    private var debounceJob: Job? = null
    private val debouncePeriod: Long = 2000 // Задержка debounce в миллисекундах

    private var isSearching: Boolean = false

    private lateinit var viewModelFactory: SearchViewModelFactory
    private val viewModel: SearchViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        sharedPreferencesManager = SharedPreferencesManager(this)

        val apiService = Retrofit.Builder()
            .baseUrl(TrackRepositoryImpl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val searchHistory = SearchHistory(sharedPreferencesManager.sharedPreferences)
        val trackRepository = TrackRepositoryImpl(apiService, searchHistory)
        val trackInteractor = TrackInteractorImpl(trackRepository)
        viewModelFactory = SearchViewModelFactory(trackInteractor)

        // Инициализация адаптеров
        trackAdapter = TrackAdapter(emptyList()).apply {
            onTrackClickListener = { track ->
                openPlayerActivity(track)
                viewModel.addTrackToHistory(track)
            }
        }
        historyAdapter = TrackAdapter(emptyList()).apply {
            onTrackClickListener = { track ->
                openPlayerActivity(track)
                viewModel.addTrackToHistory(track)
            }
        }

        setupRecyclerViews()
        setupPlaceholders()
        setupToolbar()
        setupSearchBar()

        viewModel.searchResults.observe(this, Observer { tracks: List<Track> ->
            trackAdapter.setTracks(tracks)
            trackAdapter.notifyDataSetChanged()
            recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
            updatePlaceholderVisibility(tracks)
            isSearching = false
            progressBar.visibility = View.GONE
        })

        viewModel.errorMessage.observe(this, Observer { message: String ->
            Log.e("SearchActivity", "Error: $message")
            isSearching = false
            progressBar.visibility = View.GONE
            // Show error message to the user if necessary
        })

        viewModel.searchHistoryLiveData.observe(this, Observer { history ->
            historyAdapter.setTracks(history)
            historyAdapter.notifyDataSetChanged()
            historyRecyclerView.visibility = if (history.isNotEmpty()) View.VISIBLE else View.GONE
        })

        viewModel.loadSearchHistory() // Первоначальное отображение истории поиска
    }

    private fun setupRecyclerViews() {
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            visibility = View.GONE
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = trackAdapter
        }

        historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = historyAdapter
        }
    }

    private fun setupPlaceholders() {
        placeholderImageView = findViewById(R.id.placeholderImageView)
        placeholderTextView = findViewById(R.id.placeholderTextView)
        secondPlaceholderImageView = findViewById(R.id.secondPlaceholderImageView)
        secondPlaceholderTextView = findViewById(R.id.secondPlaceholderTextView)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val color = ContextCompat.getColor(this, R.color.primary)
        val upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back)
        upArrow?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSearchBar() {
        val inputEditText = findViewById<EditText>(R.id.seachBarLineEditT)
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                debounceJob?.cancel()
                debounceJob = lifecycleScope.launch {
                    delay(debouncePeriod)
                    s?.let {
                        if (it.isNotEmpty()) {
                            if (isNetworkAvailable()) {
                                performSearch(it.toString())
                            } else {
                                Log.e("SearchActivity", "No internet connection")
                                showNoInternetPlaceholder()
                            }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        if (isSearching) return
        progressBar.visibility = View.VISIBLE
        isSearching = true
        viewModel.searchTracks(query)
    }

    private fun updatePlaceholderVisibility(tracks: List<Track>) {
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
    }

    private fun showNoInternetPlaceholder() {
        placeholderImageView.setImageResource(R.drawable.ic_error_communication)
        placeholderTextView.text = getString(R.string.no_connection)
        placeholderImageView.visibility = View.VISIBLE
        placeholderTextView.visibility = View.VISIBLE
        secondPlaceholderImageView.visibility = View.GONE
        secondPlaceholderTextView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun openPlayerActivity(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java).apply {
            putExtra("track", track)
        }
        startActivity(intent)
    }

    private fun isNetworkAvailable(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
