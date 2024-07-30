package com.example.plm2.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plm2.domain.interactor.TrackInteractor
import com.example.plm2.domain.model.Track
import kotlinx.coroutines.launch

class SearchViewModel(private val trackInteractor: TrackInteractor) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Track>>()
    val searchResults: LiveData<List<Track>> get() = _searchResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _searchHistory = MutableLiveData<List<Track>>()
    val searchHistory: LiveData<List<Track>> get() = _searchHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun searchTracks(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val results = trackInteractor.searchTracks(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch search results: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSearchHistory() {
        _searchHistory.value = trackInteractor.loadSearchHistory()
    }

    fun addTrackToHistory(track: Track) {
        trackInteractor.addTrackToHistory(track)
        loadSearchHistory() // Обновить историю после добавления трека
    }

    fun clearSearchHistory() {
        trackInteractor.clearSearchHistory()
        loadSearchHistory() // Обновить историю после очистки
    }
}
