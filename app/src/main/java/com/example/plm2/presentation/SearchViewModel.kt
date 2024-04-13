package com.example.plm2.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.plm2.domain.Track
import com.example.plm2.data.TracksRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val trackRepository = TracksRepositoryImpl(application)

    private val _searchResults = MutableLiveData<List<Track>>()
    val searchResults: LiveData<List<Track>>
        get() = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            val results = performSearch(query)
            withContext(Dispatchers.Main) {
                _searchResults.value = results
            }
        }
    }

    private suspend fun performSearch(query: String): List<Track> {
        return trackRepository.loadTracks()
    }
}